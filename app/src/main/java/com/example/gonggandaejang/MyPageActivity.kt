package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.UserInfoDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.example.gonggandaejang.API.GetUserInfoService
import com.example.gonggandaejang.databinding.ActivityDailyWorkDocumentBinding
import com.example.gonggandaejang.databinding.ActivityMyPageBinding
import com.example.gonggandaejang.objects.CodeList
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var getUserInfo: UserInfoDTO? = null
private lateinit var backAuthState : String


class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPageBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        supportActionBar?.title = getString(R.string.my_page_eng)

        //사용자 정보 표시==================================================================================================================================
        val retrofitUserInfo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/")
        val userInfoService: GetUserInfoService = retrofitUserInfo.create(GetUserInfoService::class.java)

        userInfoService.requestUserInfo(userToken, CodeList.sysCd).enqueue(object : Callback<UserInfoDTO> {
            val dialog = AlertDialog.Builder(this@MyPageActivity)
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
                        binding.sectorsText.text = getUserInfo?.value?.co_type
                        binding.coLicenseText.text = getUserInfo?.value?.co_regisnum
                        binding.authorityText.text = getUserInfo?.value?.authority_name

                }
            }
        })

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
        when (backAuthState) {
            CodeList.Buyer -> {
                val intent =
                    Intent(this@MyPageActivity, DashboardEnterprise::class.java)
                startActivity(intent)
                finish()
            }
            CodeList.design, CodeList.company, CodeList.work -> {
                val intent =
                    Intent(this@MyPageActivity, DashboardUsers::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                Toast.makeText(
                    this@MyPageActivity,
                    getUserInfo?.msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}