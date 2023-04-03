package com.gonggan.source.dailywork

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityDailyWorkWatchPostedPhotoBinding
import com.gonggan.API.GetUserInfoService
import com.gonggan.Adapter.*
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.ApiUtilities
import com.gonggan.objects.CodeList
import kotlinx.android.synthetic.main.item_comment_child.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

//사진 파일 조회
private const val TAG = "DailyWorkWatchPostedPhoto"

private var ImageInfoData = arrayListOf<ImageOutData>()
private lateinit var ImageInfoInputData : ImageOutData

private var userInfo: UserInfoDTO? = null

class DailyWorkWatchPostedPhoto : AppCompatActivity() {
    private lateinit var binding: ActivityDailyWorkWatchPostedPhotoBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var consCode : String
    private lateinit var sysDocNum : String
    private lateinit var workLogConsCode : String
    private lateinit var consDate : String
    private lateinit var consTypeCd : String
    private lateinit var imageGetData : ConsWorkInputList
    private lateinit var title : String
    private lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyWorkWatchPostedPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.title = getString(R.string.posted_photo_watch)

        binding.imageRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DailyWorkPostAdapter(ImageInfoData,  userToken, consCode, sysDocNum)
        }
        //수정버튼
        buttonVisible()
        //등록된 사진 조회
        callPicture()

        binding.bottomModifyBtn.setOnClickListener {
            val intent = Intent(this, WorkEditPhoto::class.java)
            intent.putExtra("sysDocCode", sysDocNum)
            intent.putExtra("code", consCode)
            intent.putExtra("work_log_cons_code", workLogConsCode)
            intent.putExtra("cons_type_cd", consTypeCd)
            intent.putExtra("cons_date", consDate)
            intent.putExtra("title", title)
            intent.putExtra("data", imageGetData)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish()
        }

        binding.bottomCancelBtn.setOnClickListener {
            val intent = Intent(this@DailyWorkWatchPostedPhoto, DailyWorkDocument::class.java)
            intent.putExtra("code", consCode)
            intent.putExtra("sysDocNum", sysDocNum)
            intent.putExtra("userId", userId)
            startActivity(intent)
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
        imageGetData = intent.getSerializableExtra("data") as ConsWorkInputList
        title = intent.getStringExtra("title")!!
        userId = intent.getStringExtra("userId")!!
    }

    //접속한 사용자와 업로드한 사람이 같으면 수정 가능 버튼
    private fun buttonVisible(){
        val retrofitInfo = ApiUtilities.callRetrofit("${CodeList.portNum}/userManage/getMyInfo/").create(GetUserInfoService::class.java)
        retrofitInfo.requestUserInfo(userToken, CodeList.sysCd).enqueue(object :
            Callback<UserInfoDTO> {
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                userInfo = response.body()
                if(userId == userInfo?.value?.id.toString()) {
                    binding.bottomModifyBtn.visibility = VISIBLE
                }else if(userId != userInfo?.value?.id.toString()){
                    binding.bottomModifyBtn.visibility = GONE
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun callPicture(){
        val insideList = ArrayList<String>()
        if(imageGetData.imageList.size != 0){
            ImageInfoData.clear()
            for (i in 0 until imageGetData.imageList.size) {
                if (!insideList.contains(imageGetData.imageList[i].title)) {
                    insideList.add(imageGetData.imageList[i].title)
                    val division = arrayListOf<DailyImageData>()
                    ImageInfoInputData = ImageOutData(imageGetData.imageList[i].title, division)
                    ImageInfoData.add(ImageInfoInputData)
                }
            }
        }
        for (m in 0 until ImageInfoData.size) {
            for (j in 0 until imageGetData.imageList.size) {
                if (ImageInfoData[m].title == imageGetData.imageList[j].title) {
                    ImageInfoData[m].GalleryList.add(DailyImageData(imageGetData.imageList[j].change_name, imageGetData.imageList[j].cons_date, imageGetData.imageList[j].cons_type_cd, imageGetData.imageList[j].cons_type_nm, imageGetData.imageList[j].cons_type_explain, imageGetData.imageList[j].file_index, imageGetData.imageList[j].origin_name, imageGetData.imageList[j].file_path, imageGetData.imageList[j].title, imageGetData.imageList[j].upload_date, j))
                }
            }
        }
        binding.imageRecycler.adapter?.notifyDataSetChanged()
    }
}