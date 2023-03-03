package com.gonggan.source.detailhome

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityRootBinding
import com.gonggan.API.GetUserInfoService
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.CodeList
import com.gonggan.objects.callRetrofit
import com.gonggan.objects.moveToDash
import com.gonggan.objects.startCloseLogoutCustom
import com.gonggan.source.dailywork.DailyWatchFragment
import com.gonggan.source.dashboard.DashboardEnterprise
import com.gonggan.source.dashboard.DashboardUsers
import com.gonggan.source.photogallery.PhotoGalleryFragment
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
        moveToDash(this@RootActivity, getUserInfo?.value?.co_code.toString(), getUserInfo?.value?.authority_code.toString(), getUserInfo?.msg.toString())
    }

    private fun init(){
        context = this
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        consCode = intent.getStringExtra("code")!!
    }
}