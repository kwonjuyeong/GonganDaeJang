package com.gonggan.source.detailhome

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityRootBinding
import com.gonggan.API.GetUserInfoService
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import com.gonggan.objects.moveToDash
import com.gonggan.objects.startCloseLogoutCustom
import com.gonggan.source.dailywork.DailyWatchFragment
import com.gonggan.source.photogallery.PhotoGalleryFragment
import com.gonggan.source.qa.QAFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//대시보드
private const val TAG = "DetailDash"
class RootActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRootBinding
    private lateinit var context: Context
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var consCode : String
    private lateinit var activityTag : String

    private var getUserInfo: UserInfoDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        initNavigationBar()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.apply {
            title = "프로젝트 대시보드"
        }


        val getUserInfoService = callRetrofit("${CodeList.portNum}/userManage/getMyInfo/").create(GetUserInfoService::class.java)
        getUserInfoService.requestUserInfo(userToken, CodeList.sysCd).enqueue(object :
            Callback<UserInfoDTO> {
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {}
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                getUserInfo = response.body()
            }
        })

        if(activityTag == ""){
            setDataAtFragment(DailyWatchFragment())
        }else if(activityTag == "QA"){
            setDataAtFragment(QAFragment())
        }

        context
        //로그아웃
        binding.logoutBtn.setOnClickListener {
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
                        setDataAtFragment(PhotoGalleryFragment())
                    }
                    R.id.q_and_a_menu -> {
                        setDataAtFragment(QAFragment())
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
        activityTag = intent.getStringExtra("TAG")!!
    }
}