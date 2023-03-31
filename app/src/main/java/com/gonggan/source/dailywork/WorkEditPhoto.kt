package com.gonggan.source.dailywork

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityWorkEditPhotoBinding
import com.gonggan.API.DeleteGallery
import com.gonggan.API.ModifyGallery
import com.gonggan.API.PostGallery
import com.gonggan.Adapter.*
import com.gonggan.DTO.*
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import com.gonggan.objects.UriPathUtils
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "WorkEditPhoto"

private var positions = 0
private var photoURI : Uri? = null
private var post : PostGalleryDTO? = null
private val usiPathUtil = UriPathUtils()

class WorkEditPhoto : AppCompatActivity() {
    private lateinit var binding: ActivityWorkEditPhotoBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var consCode : String
    private lateinit var sysDocNum : String
    private lateinit var workLogConsCode : String
    private lateinit var consDate : String
    private lateinit var consTypeCd : String
    private lateinit var defaultTitle : String
    private lateinit var imageGetData : ConsWorkInputList
    private lateinit var userId : String

    private val imageOutsideData = ArrayList<OutSideImageInfo>()
    private val imageOriginData = ArrayList<OutsideDivision>()

    private lateinit var imageOutsideInputData : OutSideImageInfo

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWorkEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        //이미지 조회================================================================================================
        binding.imageRecycler.apply {
            layoutManager = LinearLayoutManager(this@WorkEditPhoto)
            adapter = OutsideAdapter(this@WorkEditPhoto, userToken, sysDocNum, consCode, imageOutsideData, {deleteBtn(it)}, {pickPicture(it)})
        }

        //원본 데이터 저장
        saveOriginImgList()

        //아이템 추가 버튼
        binding.addItemBtn.setOnClickListener {
            addItem()
        }

        //등록 버튼
        binding.bottomPostBtn.setOnClickListener {
        Toast.makeText(this@WorkEditPhoto, "사진이 등록중입니다.", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
        compareList(imageOriginData, imageOutsideData)
        delay(2000)
        val intent = Intent(this@WorkEditPhoto, DailyWorkDocument::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("sysDocNum", sysDocNum)
        intent.putExtra("code", consCode)
        startActivity(intent)
        finish() }
        }

        binding.bottomCancelBtn.setOnClickListener {
        finish()
        }
    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        consCode = intent.getStringExtra("code")!!
        sysDocNum = intent.getStringExtra("sysDocCode")!!
        workLogConsCode = intent.getStringExtra("work_log_cons_code")!!
        consDate = intent.getStringExtra("cons_date")!!
        consTypeCd = intent.getStringExtra("cons_type_cd")!!
        defaultTitle = intent.getStringExtra("title")!!
        imageGetData = intent.getSerializableExtra("data") as ConsWorkInputList
        userId = intent.getStringExtra("userId")!!
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun saveOriginImgList(){
        val insideList = ArrayList<String>()
        if(imageGetData.imageList.size != 0){
            imageOutsideData.clear()
            for (i in 0 until imageGetData.imageList.size) {
                if (!insideList.contains(imageGetData.imageList[i].title)) {
                    insideList.add(imageGetData.imageList[i].title)
                    val division = arrayListOf<InsideImageInfoList>()
                    val divisionOrigin = arrayListOf<Images>()
                    val outsideImageInfo = OutSideImageInfo(imageGetData.imageList[i].title, division)
                    imageOutsideData.add(outsideImageInfo)
                    imageOriginData.add(OutsideDivision(imageGetData.imageList[i].title, divisionOrigin))
                }
            }

            for (m in 0 until imageOutsideData.size) {
                for (j in 0 until imageGetData.imageList.size) {
                    if (imageOutsideData[m].parentTitle == imageGetData.imageList[j].title) {
                        imageOutsideData[m].fileList.add(InsideImageInfoList(imageGetData.imageList[j].file_index, imageGetData.imageList[j].origin_name, imageGetData.imageList[j].change_name, imageGetData.imageList[j].file_path, imageGetData.imageList[j].title, null))
                        imageOriginData[m].fileList.add(Images(imageGetData.imageList[j].file_index, imageGetData.imageList[j].origin_name, imageGetData.imageList[j].file_path, imageGetData.imageList[j].title, null))
                    }
                }
            }
            binding.imageRecycler.adapter?.notifyDataSetChanged()
        }
    }

    //아이템 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addItem() {
        val division = arrayListOf<InsideImageInfoList>()
        imageOutsideInputData = OutSideImageInfo(defaultTitle, division)
        imageOutsideData.add(imageOutsideInputData)
        binding.imageRecycler.adapter?.notifyDataSetChanged()
    }

    //사진 리스트 삭제(Outside)
    @SuppressLint("NotifyDataSetChanged")
    private fun deleteBtn(data : OutSideImageInfo){
        imageOutsideData.remove(data)
        binding.imageRecycler.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            //사진 - 파일 탐색기에서 가져오기
            CodeList.Album -> {
                data ?: return
                val clipData = data.clipData

                Log.d("position", positions.toString())

                if (clipData == null || clipData.itemCount == 1) {
                    // Single photo selected
                    val uri = if (clipData != null)
                    clipData.getItemAt(0).uri
                    else data.data

                    val filename = getName(uri)
                    val imgPath = usiPathUtil.getRealPathFromURI(this, uri!!)
                    if (filename != "" && imgPath != null) {
                        val `in` = contentResolver.openInputStream(uri)
                        val file = File(applicationContext.filesDir, filename)
                        if (`in` != null) {
                            try {
                                val out: OutputStream = FileOutputStream(file)
                                try {
                                    val buf = ByteArray(4096)
                                    var len: Int
                                    while (`in`.read(buf).also { len = it } > 0) {
                                        out.write(buf, 0, len)
                                    }
                                    val title = imageOutsideData[positions].parentTitle
                                    imageOutsideData[positions].fileList.add(InsideImageInfoList(imageOutsideData[positions].fileList.size, filename, "", file.path, title,file))
                                    binding.imageRecycler.adapter?.notifyDataSetChanged()
                                } finally {
                                    out.close()
                                }
                            } finally {
                                `in`.close()
                            }
                        }
                    }
                } else {
                    // Multiple photos selected
                    for (i in 0 until clipData.itemCount) {
                         val uri = clipData.getItemAt(i).uri

                        Log.d("position", positions.toString())

                        val filename = getName(uri)
                        val imgPath = usiPathUtil.getRealPathFromURI(this, uri)
                        if (filename != "" && imgPath != null) {
                            val `in` = contentResolver.openInputStream(uri)
                            val file = File(applicationContext.filesDir, filename)
                            if (`in` != null) {
                                try {
                                    val out: OutputStream = FileOutputStream(file)
                                    try {
                                        val buf = ByteArray(4096)
                                        var len: Int
                                        while (`in`.read(buf).also { len = it } > 0) {
                                            out.write(buf, 0, len)
                                        }
                                        val title = imageOutsideData[positions].parentTitle
                                        imageOutsideData[positions].fileList.add(InsideImageInfoList(imageOutsideData[positions].fileList.size, filename, "", file.path, title, file))
                                        binding.imageRecycler.adapter?.notifyDataSetChanged()
                                    } finally {
                                        out.close()
                                    }
                                } finally {
                                    `in`.close()
                                }
                            }
                        }
                    }
                }
            }
            //사진 - 카메라에서 가져오기
            CodeList.Camera -> {
                if( photoURI != null)
                {
                    val filename = getName(photoURI)
                    val imgPath =  usiPathUtil.getRealPathFromURI(this, photoURI as Uri)
                    if (filename != "" && imgPath != null) {
                        Log.d("position", positions.toString())
                        val `in` = contentResolver.openInputStream(photoURI as Uri) //src
                        val file = File(applicationContext.filesDir, filename)
                        if (`in` != null) {
                            try {
                                val out: OutputStream = FileOutputStream(file) //dst
                                try {
                                    // Transfer bytes from in to out
                                    val buf = ByteArray(4096)
                                    var len: Int
                                    while (`in`.read(buf).also { len = it } > 0) {
                                        out.write(buf, 0, len)
                                    }
                                    val title = imageOutsideData[positions].parentTitle
                                    imageOutsideData[positions].fileList.add(InsideImageInfoList(imageOutsideData[positions].fileList.size ,filename , "",file.path, title, file))
                                    binding.imageRecycler.adapter?.notifyDataSetChanged()

                                } finally {
                                    out.close()
                                }
                            } finally {
                                `in`.close()
                            }
                        }
                    }
                }
            }
        }
    }

    //Uri to 파일명 추출 함수
    private fun getName(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val cursor = managedQuery(uri, projection, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    //카메라 호출 함수
    @SuppressLint("SimpleDateFormat")
    private fun dispatchTakePictureIntentEx(position: Int)
    {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri : Uri? = createImageUri("JPEG_${timeStamp}")
        photoURI = uri
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        positions = position
        startActivityForResult(takePictureIntent, CodeList.Camera)
    }

    //카메라 사진 저장 함수
    private fun createImageUri(filename:String): Uri?{
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    //사진 선택 다이어로그
    private fun pickPicture(position : Int){
        val dialog = Dialog(this@WorkEditPhoto)
        dialog.setContentView(R.layout.custom_dialog_camera)

        val text =dialog.findViewById<TextView>(R.id.camera_title)
        val camera = dialog.findViewById<Button>(R.id.camera_btn)
        val album = dialog.findViewById<Button>(R.id.album_btn)

        text.text = "사진 등록"

        camera.setOnClickListener {
            dispatchTakePictureIntentEx(position)
            dialog.dismiss()
        }

        album.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            positions = position
            Log.d("position_value", position.toString())
            startActivityForResult(intent, CodeList.Album)
            dialog.dismiss()
        }

        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
    }




    private fun compareList(origImgList : ArrayList<OutsideDivision>, chaImgList : ArrayList<OutSideImageInfo>){
        val convertImgList: MutableList<ConvertedImg> = mutableListOf()
        val convertOrigList: MutableList<ConvertedImg> = mutableListOf()
        var index = 0

        Log.d("original_convert", Gson().toJson(origImgList))
        Log.d("original_convert", Gson().toJson(chaImgList))

        for(i in 0 until chaImgList.size){
            for (j in 0 until chaImgList[i].fileList.size){
                convertImgList.add(ConvertedImg(title = chaImgList[i].fileList[j].title, chaImgList[i].fileList[j].origin_name, chaImgList[i].fileList[j].file, chaImgList[i].fileList[j].file_index))
            }
        }
        convertImgList.sortBy { it.file_index }

        for(i in 0 until origImgList.size){
            for (j in 0 until origImgList[i].fileList.size){
                convertOrigList.add(ConvertedImg(title = origImgList[i].fileList[j].title, origImgList[i].fileList[j].origin_name, origImgList[i].fileList[j].file, origImgList[i].fileList[j].file_index))
            }
        }
        convertOrigList.sortBy { it.file_index }

        Log.d("original_convert", Gson().toJson(convertOrigList))
        Log.d("change_convert", Gson().toJson(convertImgList))

        if(convertImgList.size != 0){
            index = convertImgList[convertImgList.size -1].file_index + 1
        }

        //삭제, 수정
        for(i in 0 until convertOrigList.size) {
            var hasFile = false
            for (j in 0 until convertImgList.size) {
                if(convertOrigList[i].origName == convertImgList[j].origName){
                hasFile = true

                if(convertOrigList[i].title != convertImgList[j].title){
                putImg(convertImgList[j].title, convertImgList[j].file_index)
                }
            }
        }
        if(!hasFile){
            delImg(convertOrigList[i].file_index)
            }
        }
        //등록
        for(i in 0 until convertImgList.size){
            var hasFile = false
            for(j in 0 until convertOrigList.size){
            if(convertImgList[i].origName == convertOrigList[j].origName){
                hasFile = true
            }
        }
        if(!hasFile){
            addImg(index, convertImgList[i].title, convertImgList[i].file)
            index++
        }
    }
 }

    private fun delImg(fileIndex : Int){
        val photoDelete = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{work_log_cons_code}/{file_index}/").create(DeleteGallery::class.java)

        photoDelete.requestDeleteGallery(consCode, sysDocNum, workLogConsCode , fileIndex ,CodeList.sysCd, userToken).enqueue(object :
            Callback<PostGalleryDTO> {
            override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) {Log.d("retro", t.toString())}
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                post = response.body()
                Log.d("delete_code", post?.code.toString())
                Log.d("delete_code", post?.msg.toString())
                Log.d("delete_code", post?.value.toString())
            }
        })
    }

    private fun putImg(title : String, fileIndex : Int){
        val photoModify = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{work_log_cons_code}/{file_index}/").create(ModifyGallery::class.java)
        photoModify.requestModifyGallery(consCode, sysDocNum, workLogConsCode , fileIndex ,CodeList.sysCd, userToken, ModifyGalleryDTO(title)).enqueue(object :
            Callback<PostGalleryDTO> {
            override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) {Log.d("retro", t.toString())}
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                post = response.body()
                Log.d("modify_code", post?.code.toString())
                Log.d("modify_code", post?.msg.toString())
                Log.d("modify_code", post?.value.toString())
            }
        })
    }

    private fun addImg(fileIndex: Int, title : String, file: File?){
        val photoPost = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/WorkDLImage/{cons_code}/{sys_doc_num}/{file_index}/").create(PostGallery::class.java)
        val fileBody = file.let { it1 -> RequestBody.create(MediaType.parse("multipart/form-data"), it1) }.let { it1 -> MultipartBody.Part.createFormData("f_image" , file?.name, it1) }
        val jsonObject = JSONObject("{\"cons_date\":\"${consDate}\", \"work_log_cons_code\":\"${workLogConsCode}\",\"cons_type_cd\":\"${consTypeCd}\", \"title\":\"${title}\"}").toString()
        val jsonBody = RequestBody.create(MediaType.parse("application/json"), jsonObject)

        //인덱스, 데이터, 파일
        photoPost.requestPostGallery(consCode, sysDocNum, fileIndex ,CodeList.sysCd, userToken, jsonBody, fileBody).enqueue(object :
            Callback<PostGalleryDTO> {
            override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) {Log.d("retro", t.toString())}
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                post = response.body()
                Log.d("post_code", post?.code.toString())
                Log.d("post_code", post?.msg.toString())
                Log.d("post_code", post?.value.toString())
            }
        })
    }



}