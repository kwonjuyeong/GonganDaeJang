package com.gonggan.source.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityDashNormalBinding
import com.gonggan.API.*
import com.gonggan.Adapter.CoHistory
import com.gonggan.Adapter.CoHistoryAdapter
import com.gonggan.Adapter.DashBoardNoCompanyProjectList
import com.gonggan.Adapter.DashBoardNoCompanyProjectListAdapter
import com.gonggan.DTO.*
import com.gonggan.objects.*
import com.gonggan.source.mypage.MyPageActivity
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var curTime: GetCurTimeInfoDTO? = null
private var weather: GetWeatherInfoDTO? = null
private var userInfo: UserInfoDTO? = null
private var history : CoHistoryDTO? = null
private var projectList : ProjectListDTO? = null
private var projHistory : ProjHistoryDTO? = null
private var getCo: GetCoListDTO? = null
private var job : Job?= null

class DashNormal : AppCompatActivity() {
    private lateinit var binding: ActivityDashNormalBinding
    private lateinit var drawer : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var sharedPreference : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var userToken : String

    private val projectListData = arrayListOf<DashBoardNoCompanyProjectList>()
    private lateinit var projectGoData: DashBoardNoCompanyProjectList

    private val historyData = arrayListOf<CoHistory>()
    private lateinit var historyInputData : CoHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashNormalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        openDrawer()
        navigationItemClick()
        requestMultiplePermissions(this)

        //날씨정보 표시 =============================================================================================================================================
        val retrofitWeather = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/getWeatherInfo/")
        val getWeatherService: GetWeatherService = retrofitWeather.create(GetWeatherService::class.java)

        getWeatherService.requestWeather(CodeList.sysCd, userToken).enqueue(object :
            Callback<GetWeatherInfoDTO> { override fun onFailure(call: Call<GetWeatherInfoDTO>, t: Throwable) { Log.d("retrofit_weather", t.toString()) }
            override fun onResponse(call: Call<GetWeatherInfoDTO>, response: Response<GetWeatherInfoDTO>) {
                weather = response.body()

                val ptyResult = weather?.value?.ptyResult
                val skyResult = weather?.value?.skyResult
                val t1kResult = weather?.value?.t1hResult

                if (weather?.code == 200) {
                    val weather = weatherCode(ptyResult, skyResult, binding.weatherIcon)
                    val ptyReturn = weather["pty"]
                    val skyReturn = weather["sky"]
                    if(ptyReturn == "")
                    {binding.weatherSet.text = "$skyReturn"}else{
                        binding.weatherSet.text = getString(R.string.weather_format, t1kResult, ptyReturn, skyReturn)
                    }
                }
            }
        })
        //시간정보 표시 =============================================================================================================================================
        val retrofitCurTime = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/getCurTimeInfo/")
        val getCurTimeInfoService: GetCurTimeInfoService = retrofitCurTime.create(GetCurTimeInfoService::class.java)

        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                getCurTimeInfoService.requestCurTime(CodeList.sysCd, userToken).enqueue(object :
                    Callback<GetCurTimeInfoDTO> {
                    override fun onFailure(call: Call<GetCurTimeInfoDTO>, t: Throwable) {
                        Log.d("retrofit", t.toString())
                    }
                    override fun onResponse(call: Call<GetCurTimeInfoDTO>, response: Response<GetCurTimeInfoDTO>) {
                        curTime = response.body()
                        if (curTime?.code == 200) {
                            binding.timeSet.text = curTime?.value
                        }
                    }
                })
                delay(1000)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            job!!.join()
        }
        //유저 헤더정보===============================================================================================================================================
        val retrofitInfo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/")
        val getMyInfo: GetUserInfoService = retrofitInfo.create(GetUserInfoService::class.java)

        getMyInfo.requestUserInfo(userToken, CodeList.sysCd).enqueue(object :
            Callback<UserInfoDTO> {
            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                userInfo = response.body()



            }
        })


        //재직 이력 정보 조회 추가(수정)===================================================================================================================================
        val retrofitCoHisList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/historyManage/getCoHisList/")
        val getCoHistory : GetCoHistory = retrofitCoHisList.create(GetCoHistory::class.java)

        binding.noCompanyRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CoHistoryAdapter(historyData) { changeProjList(it) }
        }

        getCoHistory.requestGetCoHistory(CodeList.sysCd, userToken).enqueue(object : Callback<CoHistoryDTO> {
            override fun onFailure(call: Call<CoHistoryDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<CoHistoryDTO>,
                response: Response<CoHistoryDTO>
            ) {
                history = response.body()
                //회사정보 Retrofit 불러오기
                val retrofitCo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/getCoList/ALL/")
                val getCoListService: GetCoListService = retrofitCo.create(GetCoListService::class.java)

                getCoListService.requestGetCoList(CodeList.sysCd, GetCoListRequestDTO("ALL")).enqueue(object : Callback<GetCoListDTO> {
                    override fun onFailure(call: Call<GetCoListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(call: Call<GetCoListDTO>, response: Response<GetCoListDTO>) {
                        getCo = response.body()
                        for (i in 0 until history?.value?.size!!) {
                            val coAddress = history?.value?.get(i)?.co_address.toString()
                            val coCeo = history?.value?.get(i)?.co_ceo.toString()
                            val coCode = history?.value?.get(i)?.co_code.toString()
                            var coName = ""
                            for(j in 0 until getCo?.value?.size!!){
                                if(getCo?.value?.get(j)?.co_code.toString() == coCode)
                                {
                                    coName = getCo?.value?.get(j)?.co_name.toString()
                                }
                            }
                            val coContact = history?.value?.get(i)?.co_contact.toString()
                            val tenureEndDate = convertDateFormat5(history?.value?.get(i)?.co_tenure_end_date.toString())
                            val tenureStartDate = convertDateFormat5(history?.value?.get(i)?.co_tenure_start_date.toString())
                            val id = history?.value?.get(i)?.id.toString()

                            historyInputData = CoHistory(coAddress, coCeo, coCode, coName, coContact, tenureEndDate, tenureStartDate, id)
                            historyData.add(historyInputData)
                        }
                        binding.noCompanyRecycler.adapter?.notifyDataSetChanged()
                    }
                })
            }
        })
        //프로젝트 이력 정보 조회 추가(수정)===================================================================================================================================
        binding.noCompanyProjectGoRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DashBoardNoCompanyProjectListAdapter(projectListData)
        }
         val retrofitProjHisList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/historyManage/getCoHisList/")
         val getProjHistory : GetProjHistory = retrofitProjHisList.create(GetProjHistory::class.java)

        getProjHistory.requestGetProjHistory(CodeList.sysCd, userToken).enqueue(object : Callback<ProjHistoryDTO> {
            override fun onFailure(call: Call<ProjHistoryDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ProjHistoryDTO>,
                response: Response<ProjHistoryDTO>
            ) {
                projHistory = response.body()

                projectListData.clear()
                for (i in 0 until projHistory?.value?.size!!) {
                    projectGoData = DashBoardNoCompanyProjectList(projHistory?.value?.get(i)?.co_code.toString(), projHistory?.value?.get(i)?.cons_code.toString(), projHistory?.value?.get(i)?.cons_name.toString(), projHistory?.value?.get(i)?.id.toString(), projHistory?.value?.get(i)?.location.toString(), projHistory?.value?.get(i)?.proj_progress_end_date.toString(),projHistory?.value?.get(i)?.proj_progress_start_Date.toString(), projHistory?.value?.get(i)?.req_date.toString())
                    projectListData.add(projectGoData)
                }
                binding.projectAll.text = projectListData.size.toString()
                binding.noCompanyProjectGoRecycler.adapter?.notifyDataSetChanged()
            }
        })


    }
    private fun init(){
        drawer = binding.drawerLayout
        navView= binding.navView
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
    }

    private fun openDrawer(){
        binding.menu.setOnClickListener {
            drawer.openDrawer(GravityCompat.END)
        }
    }

    private fun navigationItemClick() {
        navView.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.logout_menu -> {
                    endCloseLogoutCustom(this, userToken,drawer,sharedPreference)
                    drawer.closeDrawer(GravityCompat.END)
                }
                R.id.my_page_menu ->{
                    drawer.closeDrawer(GravityCompat.END)
                    val intent = Intent(this@DashNormal, MyPageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            return@setNavigationItemSelectedListener false
        }
    }

    //회사 클릭 시 프로젝트 리스트 교환해주는 함수
    private fun changeProjList(data : CoHistory) {
        val retrofitProjHisList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/historyManage/getCoHisList/")
        val getProjHistory : GetProjHistory = retrofitProjHisList.create(GetProjHistory::class.java)
        getProjHistory.requestGetProjHistory(CodeList.sysCd, userToken).enqueue(object : Callback<ProjHistoryDTO> {
            override fun onFailure(call: Call<ProjHistoryDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ProjHistoryDTO>, response: Response<ProjHistoryDTO>) {
                projHistory = response.body()
                projectListData.clear()
                for (i in 0 until projHistory?.value?.size!!) {
                    if(projHistory?.value?.get(i)?.co_code.toString() == data.co_code){
                        projectGoData = DashBoardNoCompanyProjectList(projHistory?.value?.get(i)?.co_code.toString(), projHistory?.value?.get(i)?.cons_code.toString(), projHistory?.value?.get(i)?.cons_name.toString(), projHistory?.value?.get(i)?.id.toString(), projHistory?.value?.get(i)?.location.toString(), projHistory?.value?.get(i)?.proj_progress_end_date.toString(),projHistory?.value?.get(i)?.proj_progress_start_Date.toString(), projHistory?.value?.get(i)?.req_date.toString())
                        projectListData.add(projectGoData)
                    }
                }
                binding.projectAll.text = projectListData.size.toString()
                binding.noCompanyProjectGoRecycler.adapter?.notifyDataSetChanged()
            }
        })
    }
}