package com.gonggan.source.mypage

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityMyPageBinding
import com.gonggan.API.GetUserInfoService
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.CodeList
import com.gonggan.objects.callRetrofit
import com.gonggan.objects.modifyInfo
import com.gonggan.objects.moveToDash
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var getUserInfo: UserInfoDTO? = null
private lateinit var backAuthState : String

class MyPageActivity : AppCompatActivity() {
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val binding: ActivityMyPageBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_page)
        val binding = ActivityMyPageBinding.inflate(layoutInflater)
       setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            title = getString(R.string.my_page_eng)
        }

        //사용자 정보 표시==================================================================================================================================
        val retrofitUserInfo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/")
        val userInfoService: GetUserInfoService = retrofitUserInfo.create(GetUserInfoService::class.java)

        userInfoService.requestUserInfo(userToken, CodeList.sysCd).enqueue(object : Callback<UserInfoDTO> {
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                getUserInfo = response.body()
                backAuthState = getUserInfo?.value?.authority_code.toString()

                Log.d("ddddd", Gson().toJson(getUserInfo?.value))

                //Response code 200 : 통신성공
                if (getUserInfo?.code == 200) {
                        binding.userIdText.text = getUserInfo?.value?.id
                        binding.userNameText.text = getUserInfo?.value?.user_name
                        binding.positionText.text = getUserInfo?.value?.user_position
                        binding.contactText.text = getUserInfo?.value?.user_contact
                        binding.eMailText.text = getUserInfo?.value?.user_email
                        binding.coNameText.text = getUserInfo?.value?.co_name
                        binding.coCeoText.text = getUserInfo?.value?.co_ceo
                        binding.coLocationText.text = getUserInfo?.value?.co_address
                        binding.coContactText.text = getUserInfo?.value?.co_contact
                        binding.coTypeText.text = getUserInfo?.value?.co_type
                        binding.coRegisnumText.text = getUserInfo?.value?.co_regisnum
                        binding.authorityText.text = getUserInfo?.value?.authority_name
                }
            }
        })

        binding.modifyBtn.setOnClickListener {
            Log.d("password", getUserInfo?.value?.password.toString())
            modifyInfo(this@MyPageActivity, getUserInfo?.value?.password.toString())
        }

        binding.myPageBtn.setOnClickListener {
            onBackPressed()
        }

    }
    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveToDash(this@MyPageActivity, getUserInfo?.value?.co_code.toString(),  backAuthState, getUserInfo?.msg.toString())
    }
}