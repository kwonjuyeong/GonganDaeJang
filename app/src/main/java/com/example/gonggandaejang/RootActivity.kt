package com.example.gonggandaejang

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.allscapeservice.a22allscape_app.DTO.UserInfoDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.example.gonggandaejang.API.GetUserInfoService
import com.example.gonggandaejang.databinding.ActivityRootBinding
import com.example.gonggandaejang.objects.CodeList
import com.example.gonggandaejang.objects.startCloseLogoutCustom
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//대시보드

class RootActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRootBinding
    private lateinit var context: Context
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var consCode : String

    private var getUserInfo: UserInfoDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        initNavigationBar()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        supportActionBar?.title = "프로젝트 대시보드"
        setDataAtFragment(DailyWatchFragment())


        val retrofitInfo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/")
        val getUserInfoService: GetUserInfoService = retrofitInfo.create(GetUserInfoService::class.java)
        getUserInfoService.requestUserInfo(userToken, CodeList.sysCd).enqueue(object :
            Callback<UserInfoDTO> {
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {}
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                getUserInfo = response.body()
                val gson = Gson()
                Log.d("login_info", gson.toJson(getUserInfo?.value))

            }
        })

        binding.logoutBtn.setOnClickListener {
            //로그아웃 버튼
            startCloseLogoutCustom(this@RootActivity, userToken,sharedPreference)
        }
    }

    private fun setDataAtFragment(fragment: Fragment)
    {
        val bundle = Bundle()
        val transaction = supportFragmentManager.beginTransaction()
        bundle.putString("code", consCode)
        bundle.putString("token", userToken)
        fragment.arguments = bundle
        transaction.replace(R.id.root_frame, fragment).commit()
    }

    //top_navi
    private fun initNavigationBar() {
        binding.topNavi.run {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.daily_work_menu -> {
                        setDataAtFragment(DailyWatchFragment())
                    }
                    R.id.photo_gallery_menu -> {
                        setDataAtFragment(PhotoGalleryFragment() )
                    }
                }
                true
            }
        }
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
        when (getUserInfo?.value?.authority_code) {
            CodeList.Buyer -> {
                val intent = Intent(this@RootActivity, DashboardEnterprise::class.java)
                startActivity(intent)
                finish()
            }
            CodeList.design, CodeList.company, CodeList.work -> {
                val intent = Intent(this@RootActivity, DashboardUsers::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                Toast.makeText(this@RootActivity, getUserInfo?.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init(){
        context = this
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        consCode = intent.getStringExtra("code")!!
    }
}