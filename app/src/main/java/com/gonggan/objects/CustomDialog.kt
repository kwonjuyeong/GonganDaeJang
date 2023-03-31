package com.gonggan.objects

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.example.gonggan.R
import com.gonggan.API.DeleteQADoc
import com.gonggan.Adapter.GalleryListData
import com.gonggan.Adapter.InsideImageInfoList
import com.gonggan.DTO.PostQADTO
import com.gonggan.source.mypage.ModifyActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.sqrt


internal enum class TOUCHMODE {
    NONE,
    SINGLE,
    MULTI
}
private var touchMode: TOUCHMODE? = null
private var matrix : Matrix? = null
private var savedMatrix : Matrix? = null
private var startPoint: PointF? = null
private var midPoint : PointF? = null
private var oldDistance = 0f

@SuppressLint("StaticFieldLeak")
private lateinit var images : ImageView

@SuppressLint("ClickableViewAccessibility")
fun customDetailGallery(context: Context, token: String, data : GalleryListData){
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_galley_detail_watch)

    images = dialog.findViewById(R.id.detail_gallery_imageview)
    val titleName =dialog.findViewById<TextView>(R.id.detail_gallery_title)
    val titles =dialog.findViewById<TextView>(R.id.detail_title)
    val consDate = dialog.findViewById<TextView>(R.id.detail_cons_date)
    val pcName = dialog.findViewById<TextView>(R.id.detail_pc_name)
    val product = dialog.findViewById<TextView>(R.id.detail_product)
    val ok = dialog.findViewById<Button>(R.id.ok_btn)
    val uploadDate = dialog.findViewById<TextView>(R.id.detail_upload_date)

    //상단 내용
    titleName.text = "상세정보"
    //사진 제목
    titles.text = data.title
    //작업일 날짜
    consDate.text = convertDateFormat(data.cons_date)
    //사진 업로드 날짜
    uploadDate.text = convertDateFormat(data.upload_date)
    //공종
    pcName.text = data.pc_name
    //품목
    product.text = data.product

    //이미지 처리
    loadFile(token, images, DocFileDownLoadDTO(data.cons_code, "", data.path, data.origin_name, data.change_name))
    matrix = Matrix()
    savedMatrix = Matrix()
    images.setOnTouchListener(onTouch)
    images.scaleType = ImageView.ScaleType.MATRIX

    //확인 버튼
    ok.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}



@SuppressLint("ClickableViewAccessibility")
fun customDetailFile(context: Context, token: String, consCode: String, data : InsideImageInfoList){
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_galley_detail_watch)

    images = dialog.findViewById(R.id.detail_gallery_imageview)
    val titleName =dialog.findViewById<TextView>(R.id.detail_gallery_title)
    val titles =dialog.findViewById<TextView>(R.id.detail_title)
    val ok = dialog.findViewById<Button>(R.id.ok_btn)
    val consDate = dialog.findViewById<LinearLayout>(R.id.cons_date_layout)
    val uploadDate = dialog.findViewById<LinearLayout>(R.id.upload_date_layout)
    val pcName = dialog.findViewById<LinearLayout>(R.id.pc_name_layout)
    val product = dialog.findViewById<LinearLayout>(R.id.product_layout)

    //상단 내용
    titleName.text = "상세정보"
    //사진 제목
    titles.text = data.title

    consDate.visibility = GONE
    uploadDate.visibility = GONE
    pcName.visibility  = GONE
    product.visibility = GONE

    if(data.file != null){
    val myBitmap = BitmapFactory.decodeFile(data.file.absolutePath)
    images.setImageBitmap(myBitmap)
    }else{
        loadFile(token, images, DocFileDownLoadDTO(consCode,"",data.file_path,data.origin_name,data.change_name))
    }

    //확인 버튼
    ok.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}


@SuppressLint("ClickableViewAccessibility")
private val onTouch = View.OnTouchListener { v, event ->
    if (v == images) {
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                touchMode = TOUCHMODE.SINGLE
                downSingleEvent(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount == 2) { // 두손가락 터치를 했을 때
                touchMode = TOUCHMODE.MULTI
                downMultiEvent(event)
            }
            MotionEvent.ACTION_MOVE -> if (touchMode === TOUCHMODE.SINGLE) {
                moveSingleEvent(event)
            } else if (touchMode === TOUCHMODE.MULTI) {
                moveMultiEvent(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> touchMode = TOUCHMODE.NONE
        }
    }
    true
}

//두 손가락의 중간점
private fun getMidPoint(e: MotionEvent): PointF {
    val x = (e.getX(0) + e.getX(1)) / 2
    val y = (e.getY(0) + e.getY(1)) / 2
    return PointF(x, y)
}

//두 손가락 간 거리
private fun getDistance(e: MotionEvent): Float {
    val x = e.getX(0) - e.getX(1)
    val y = e.getY(0) - e.getY(1)
    return sqrt((x * x + y * y).toDouble()).toFloat()
}

//한 손가락 시작점 구하는 함수
private fun downSingleEvent(event: MotionEvent) {
    savedMatrix!!.set(matrix)
    startPoint = PointF(event.x, event.y)
}

//한 손가락 이동
private fun moveSingleEvent(event: MotionEvent) {
    matrix!!.set(savedMatrix)
    matrix!!.postTranslate(event.x - startPoint!!.x, event.y - startPoint!!.y)
    images.imageMatrix = matrix
}

//두 손가락 시작점 구하는 함수
private fun downMultiEvent(event: MotionEvent) {
    oldDistance = getDistance(event)
    if (oldDistance > 5f) {
        savedMatrix!!.set(matrix)
        midPoint = getMidPoint(event)
    }
}

//두 손가락 이동
private fun moveMultiEvent(event: MotionEvent) {
    val newDistance = getDistance(event)
    if (newDistance > 5f) {
        matrix!!.set(savedMatrix)
        val scale = newDistance / oldDistance
        matrix!!.postScale(scale, scale, midPoint!!.x, midPoint!!.y)
        matrix!!.postRotate(0.0.toFloat(), midPoint!!.x, midPoint!!.y)
        images.imageMatrix = matrix
    }
}


//회원정보 수정 페이지 접속 비밀번호 확인
fun modifyInfo(context: Context, userPassword : String){
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_modify_user)

    val text =dialog.findViewById<TextView>(R.id.modify_text)
    val modifyTitle = dialog.findViewById<TextView>(R.id.modify_title)
    val modifyBtn = dialog.findViewById<Button>(R.id.modify_btn)
    val modifyNoBtn = dialog.findViewById<Button>(R.id.modify_cancel_btn)
    val modifyPw = dialog.findViewById<EditText>(R.id.modify_password)
    val modifyAnimation = dialog.findViewById<LottieAnimationView>(R.id.modify_animation)

    text.text = "회원정보를 수정하시려면 비밀번호를 인증해주세요."
    modifyTitle.text = "비밀번호 확인"

    modifyAnimation.playAnimation()
    //비밀번호 정규식 체크(영문 소문자, 대문자, 특수문자, 숫자 최소 8글자)
    modifyPw.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(checkPW( modifyPw.text.toString().trim())){
                modifyPw.setTextColor(R.color.black.toInt())
            }else{
                modifyPw.setTextColor(-65536)
            }
        }
    })


    modifyBtn.setOnClickListener {
        val passwd = modifyPw.text.toString().trim()
        val passWd = getSHA512(passwd)
        if(passWd== userPassword){
            Toast.makeText(context, "비밀번호가 확인되었습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(context , ModifyActivity::class.java)
            context.startActivity(intent)
            (context as Activity).finish()
            dialog.dismiss()
        }else{
            Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    modifyNoBtn.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}


private var deleteDoc : PostQADTO?= null

data class deleteDocData(
      val consCode: String,
      val token : String,
      val uuid: String
)
//문서삭제
fun deleteDocCustom(context: Context, data : deleteDocData){
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.custom_dialog_delete_doc)

    val deleteText = dialog.findViewById<TextView>(R.id.delete_doc_text)
    val deleteBtn = dialog.findViewById<Button>(R.id.doc_delete_btn)
    val deleteNoBtn = dialog.findViewById<Button>(R.id.doc_delete_no_btn)

    val lottyAnimation = dialog.findViewById<LottieAnimationView>(R.id.delete_doc_lottie)

    deleteText.text = "게시글을 삭제하시겠습니까?"

    lottyAnimation.playAnimation()

    deleteBtn.setOnClickListener {
        val retroDeleteQaList = ApiUtilities.callRetrofit("http://211.107.220.103:${CodeList.portNum}/projMessageBoardManage/MessageBoard/{cons_code}/").create(DeleteQADoc::class.java)
        retroDeleteQaList.requestDeleteQa(data.consCode,CodeList.sysCd, data.token , data.uuid).enqueue(object :
            Callback<PostQADTO> {
            override fun onFailure(call: Call<PostQADTO>, t: Throwable) { Log.d("QAWatchDoc_error", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<PostQADTO>, response: Response<PostQADTO>) {
                deleteDoc = response.body()

                if(deleteDoc?.code == 200){
                    Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show()
                    (context as Activity).finish()
                }else{
                    Toast.makeText(context, "삭제 실패 ${deleteDoc?.msg}", Toast.LENGTH_SHORT).show()
                    (context as Activity).finish()
                }
            }
        })

    }
    deleteNoBtn.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    dialog.setCanceledOnTouchOutside(true)
    dialog.setCancelable(true)
    dialog.show()
}