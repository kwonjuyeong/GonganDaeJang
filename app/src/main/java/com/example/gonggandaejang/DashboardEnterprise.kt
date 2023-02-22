package com.example.gonggandaejang

//대시보드(기업)

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.*
import com.allscapeservice.a22allscape_app.objects.*
import com.example.gonggandaejang.API.GetCurTimeInfoService
import com.example.gonggandaejang.API.GetWeatherService
import com.example.gonggandaejang.API.ProjectGoService
import com.example.gonggandaejang.API.ProjectListService
import com.example.gonggandaejang.Adapter.DashBoardProjectGo
import com.example.gonggandaejang.Adapter.DashBoardProjectGoAdapter
import com.example.gonggandaejang.databinding.ActivityDashEnterpriseBinding
import com.example.gonggandaejang.objects.CodeList
import com.example.gonggandaejang.objects.startCloseLogoutCustom
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

private lateinit var sharedPreference : SharedPreferences
private lateinit var editor : SharedPreferences.Editor
private lateinit var userToken : String

//Response DTO 정의=============================================================================
private var projectList : ProjectListDTO? = null
private var projectGo : ProjectGoDTO? = null
private var userInfo: UserInfoDTO? = null
//Response DTO 정의=============================================================================
private var job : Job?= null
private var curTime: GetCurTimeInfoDTO? = null
private var weather: GetWeatherInfoDTO? = null

class DashboardEnterprise : AppCompatActivity() {

    private lateinit var binding: ActivityDashEnterpriseBinding
    //프로젝트 리스트 조회=============================================================================
    private lateinit var projectListArray : ArrayList<ProjectListDTO>
    //프로젝트 이동 조회=============================================================================
    private val projectListData = arrayListOf<DashBoardProjectGo>()
    private lateinit var projectGoData: DashBoardProjectGo

    private var allProj = 0

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashEnterpriseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        //사용자 권한 체크
        requestMultiplePermissions(this)

            binding.lottieAnimation.playAnimation()

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
                        if(ptyReturn == "") {binding.weatherSet.text = "$skyReturn"
                        }else{
                            binding.weatherSet.text = getString(R.string.weather_format, t1kResult, ptyReturn, skyReturn)
                        }
                    }
                }
            })
            //시간정보 표시=============================================================================================================================================
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
            //시간 코루틴 사용
            CoroutineScope(Dispatchers.IO).launch {
                job!!.join()
            }



            //프로젝트 상태 통계 현황 조회==============================================================================================================================
            val retrofitProjectList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projStatistManage/getProjStatusStatistics/")
            val getProjectListService : ProjectListService = retrofitProjectList.create(ProjectListService::class.java)

            getProjectListService.requestProjectsList(CodeList.sysCd, userToken).enqueue(object :
                Callback<ProjectListDTO> {
                override fun onFailure(call: Call<ProjectListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                override fun onResponse(
                    call: Call<ProjectListDTO>,
                    response: Response<ProjectListDTO>
                ) {
                    projectList = response.body()

                    if (projectList?.code == 200) {

                        for(i in 0 until projectList?.value!!.size){
                            when(projectList?.value?.get(i)?.status){
                                "ST000001" -> {
                                    binding.readyProj.text = projectList?.value?.get(i)?.count.toString()
                                }
                                "ST000002" -> {
                                    binding.progressProj.text = projectList?.value?.get(i)?.count.toString()
                                }
                                "ST000003" -> {
                                    binding.stopProj.text = projectList?.value?.get(i)?.count.toString()
                                }
                                "ST000004" -> {
                                    binding.completeProj.text = projectList?.value?.get(i)?.count.toString()
                                }
                            }
                            allProj += projectList!!.value[i].count
                        }
                        binding.allProj.text = allProj.toString()
                    }
                }
            })
        //프로젝트로 이동하기=========================================================================================================================================================
        binding.enterPProjectGoRecycler.layoutManager = LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.HORIZONTAL }
        binding.enterPProjectGoRecycler.adapter = DashBoardProjectGoAdapter(projectListData)

        val retrofitProjectGo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/{projectStatus}/")
        val projectGoService: ProjectGoService = retrofitProjectGo.create(ProjectGoService::class.java)

        projectGoService.requestProjectsGo(CodeList.project_all , CodeList.sysCd, userToken).enqueue(object :
            Callback<ProjectGoDTO> {
            override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                projectGo = response.body()
                Log.d("projectGo", Gson().toJson(projectGo?.value))
                projectListData.clear()
                for (i in 0 until projectGo?.value?.size!!) {
                    projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectGo?.value?.get(i)?.project_status_name.toString() ,projectGo?.value?.get(i)?.cons_code.toString(),userInfo?.value?.authority_code.toString())
                    projectListData.add(projectGoData)
                }
                binding.enterPProjectGoRecycler.adapter?.notifyDataSetChanged()
            }
        })

            binding.allProject.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(CodeList.project_all , CodeList.sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()

                    for (i in 0 until projectGo?.value?.size!!) {
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectGo?.value?.get(i)?.project_status_name.toString() ,projectGo?.value?.get(i)?.cons_code.toString(),userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.enterPProjectGoRecycler.adapter?.notifyDataSetChanged()
                }
            })
        }
        binding.readyProject.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(CodeList.project_ready , CodeList.sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()

                    for (i in 0 until projectGo?.value?.size!!) {
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectGo?.value?.get(i)?.project_status_name.toString(), projectGo?.value?.get(i)?.cons_code.toString(), userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.enterPProjectGoRecycler.adapter?.notifyDataSetChanged()
                }
            })

        }
        binding.progressProject.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(CodeList.project_progress , CodeList.sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()

                    for (i in 0 until projectGo?.value?.size!!) {
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectGo?.value?.get(i)?.project_status_name.toString(),projectGo?.value?.get(i)?.cons_code.toString(), userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.enterPProjectGoRecycler.adapter?.notifyDataSetChanged()
                }
            })
        }
        binding.stopProject.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(CodeList.project_stop , CodeList.sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()
                    for (i in 0 until projectGo?.value?.size!!) {
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectGo?.value?.get(i)?.project_status_name.toString(),projectGo?.value?.get(i)?.cons_code.toString(),  userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.enterPProjectGoRecycler.adapter?.notifyDataSetChanged()

                }
            })
        }
        binding.completeProject.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(CodeList.project_complete , CodeList.sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()

                    for (i in 0 until projectGo?.value?.size!!) {
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectGo?.value?.get(i)?.project_status_name.toString() ,projectGo?.value?.get(i)?.cons_code.toString() ,userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.enterPProjectGoRecycler.adapter?.notifyDataSetChanged()
                }
            })
        }

            binding.logoutBtn.setOnClickListener {
                //로그아웃 버튼
                startCloseLogoutCustom(this@DashboardEnterprise, userToken,sharedPreference)
            }
        }
        private fun init(){
            sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
            editor = sharedPreference.edit()
            userToken = sharedPreference.getString("token", "").toString()
        }
}