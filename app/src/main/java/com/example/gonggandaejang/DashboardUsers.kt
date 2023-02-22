package com.example.gonggandaejang

//대시보드(User)

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.*
import com.allscapeservice.a22allscape_app.objects.*
import com.example.gonggandaejang.API.*
import com.example.gonggandaejang.Adapter.DashBoardProjectGo
import com.example.gonggandaejang.Adapter.DashBoardProjectGoAdapter
import com.example.gonggandaejang.databinding.ActivityDashUsersBinding
import com.example.gonggandaejang.objects.CodeList
import com.example.gonggandaejang.objects.CodeList.project_all
import com.example.gonggandaejang.objects.CodeList.project_complete
import com.example.gonggandaejang.objects.CodeList.project_progress
import com.example.gonggandaejang.objects.CodeList.project_ready
import com.example.gonggandaejang.objects.CodeList.project_stop
import com.example.gonggandaejang.objects.CodeList.sysCd
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
private var projectGo : ProjectGoDTO? = null
private var projectList : ProjectListDTO? = null
private var userInfo: UserInfoDTO? = null

//Response DTO 정의=============================================================================
private var job : Job?= null
private var curTime: GetCurTimeInfoDTO? = null
private var weather: GetWeatherInfoDTO? = null

class DashboardUsers : AppCompatActivity() {
    private lateinit var binding: ActivityDashUsersBinding
    private lateinit var context : Context
    private var allProj = 0

    private lateinit var projectGoData: DashBoardProjectGo
    private var projectListData = arrayListOf<DashBoardProjectGo>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        requestMultiplePermissions(this)

        binding.lottieAnimation.playAnimation()

        //날씨정보 표시 =============================================================================================================================================
        val retrofitWeather = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/getWeatherInfo/")
        val getWeatherService: GetWeatherService = retrofitWeather.create(GetWeatherService::class.java)

        getWeatherService.requestWeather(sysCd, userToken).enqueue(object :
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
                getCurTimeInfoService.requestCurTime(sysCd, userToken).enqueue(object :
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

        getProjectListService.requestProjectsList(sysCd, userToken).enqueue(object :
            Callback<ProjectListDTO> {
            override fun onFailure(call: Call<ProjectListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            override fun onResponse(
                call: Call<ProjectListDTO>,
                response: Response<ProjectListDTO>
            ) {
                projectList = response.body()

                Log.d("projectList", projectList?.value.toString())
                Log.d("projectList", projectList.toString())

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

        //프로젝트 이동 리스트 불러오기====================================================================================================================
        binding.userProjectGoRecycler.layoutManager = LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.HORIZONTAL }
        binding.userProjectGoRecycler.adapter = DashBoardProjectGoAdapter(projectListData)

        val retrofitProjStatus = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/{projectStatus}/")
        val projectGoService: ProjectGoService = retrofitProjStatus.create(ProjectGoService::class.java)

        //첫 접속 시 전체 프로젝트 불러오기
        projectGoService.requestProjectsGo(project_all , sysCd, userToken).enqueue(object :
            Callback<ProjectGoDTO> {
            override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                projectGo = response.body()
                projectListData.clear()
                Log.d("projectGo", Gson().toJson(projectGo?.value))
                for (i in 0 until projectGo?.value?.size!!) {
                    val projectStatus = projectGo?.value?.get(i)?.project_status_name.toString()
                    val consCode = projectGo?.value?.get(i)?.cons_code.toString()

                    projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectStatus, consCode, userInfo?.value?.authority_code.toString())
                    projectListData.add(projectGoData)
                }
                binding.userProjectGoRecycler.adapter?.notifyDataSetChanged()

            }
        })

        //전체 프로젝트 클릭시
        binding.projectAll.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(project_all , sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()
                    Log.d("projectGo", Gson().toJson(projectGo?.value))
                    for (i in 0 until projectGo?.value?.size!!) {
                        val projectStatus = projectGo?.value?.get(i)?.project_status_name.toString()
                        val consCode = projectGo?.value?.get(i)?.cons_code.toString()
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectStatus, consCode, userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.userProjectGoRecycler.adapter?.notifyDataSetChanged()

                }
            })
        }

        //준비중 프로젝트 클릭시
        binding.projectReady.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(project_ready , sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()
                    Log.d("projectGo", Gson().toJson(projectGo?.value))
                    for (i in 0 until projectGo?.value?.size!!) {
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), "준비", projectGo?.value?.get(i)?.cons_code.toString(), userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.userProjectGoRecycler.adapter?.notifyDataSetChanged()
                }
            })
        }
        //진행중인 프로젝트 클릭 시
        binding.projectProgress.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(project_progress , sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()
                    Log.d("projectGo", Gson().toJson(projectGo?.value))
                    for (i in 0 until projectGo?.value?.size!!) {
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), "진행", projectGo?.value?.get(i)?.cons_code.toString(), userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.userProjectGoRecycler.adapter?.notifyDataSetChanged()
                }
            })
        }

        //중지된 프로젝트 클릭 시
        binding.projectStop.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(project_stop ,sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()
                    Log.d("projectGo", Gson().toJson(projectGo?.value))
                    for (i in 0 until projectGo?.value?.size!!) {
                        val consCode = projectGo?.value?.get(i)?.cons_code.toString()
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), "중지", consCode, userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.userProjectGoRecycler.adapter?.notifyDataSetChanged()
                }
            })
        }

        //완료된 프로젝트 클릭 시
        binding.projectComplete.setOnClickListener {
            projectListData.clear()
            projectGoService.requestProjectsGo(project_complete , sysCd, userToken).enqueue(object :
                Callback<ProjectGoDTO> {
                override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                    projectGo = response.body()

                    for (i in 0 until projectGo?.value?.size!!) {
                        val consCode = projectGo?.value?.get(i)?.cons_code.toString()
                        projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(),"완료" , consCode, userInfo?.value?.authority_code.toString())
                        projectListData.add(projectGoData)
                    }
                    binding.userProjectGoRecycler.adapter?.notifyDataSetChanged()

                }
            })
        }

        binding.logoutBtn.setOnClickListener {
            //로그아웃 버튼
            startCloseLogoutCustom(this@DashboardUsers, userToken,sharedPreference)
        }
  }

    private fun init(){
        context = this
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}


