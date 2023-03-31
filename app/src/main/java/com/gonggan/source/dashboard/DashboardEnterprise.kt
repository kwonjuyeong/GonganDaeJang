package com.gonggan.source.dashboard

//대시보드(기업)

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityDashEnterpriseBinding
import com.gonggan.API.GetCurTimeInfoService
import com.gonggan.API.GetWeatherService
import com.gonggan.API.ProjectGoService
import com.gonggan.API.ProjectListService
import com.gonggan.Adapter.DashBoardProjectGo
import com.gonggan.Adapter.DashBoardProjectGoAdapter
import com.gonggan.DTO.*
import com.gonggan.objects.*
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "DashEnterprise"

private lateinit var drawer : DrawerLayout
private lateinit var navView : NavigationView
private lateinit var sharedPreference : SharedPreferences
private lateinit var editor : SharedPreferences.Editor
private lateinit var userToken : String
//Response DTO 정의=============================================================================
private var projectList : ProjectListDTO? = null
private var projectGo : ProjectGoDTO? = null
private var userInfo: UserInfoDTO? = null
private var job : Job ?= null

class DashboardEnterprise : AppCompatActivity() {
    private lateinit var binding: ActivityDashEnterpriseBinding
    //프로젝트 이동 조회=============================================================================
    private val projectListData = arrayListOf<DashBoardProjectGo>()
    private lateinit var projectGoData: DashBoardProjectGo

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashEnterpriseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        //사용자 권한 체크
        requestMultiplePermissions(this)

        //애니메이션
        binding.lottieAnimation.playAnimation()

        //날씨정보 표시
        callWeatherInfo(userToken, this@DashboardEnterprise, binding.weatherIcon, binding.weatherSet)

        //시간정보 표시
        CoroutineScope(Dispatchers.IO).launch {
            job = callTimeSet(userToken, binding.timeSet)
            job!!.join()
        }

        //프로젝트 상태 통계 현황 조회
        projectCount()

        //프로젝트로 이동하기
        binding.enterPProjectGoRecycler.apply {
            layoutManager = LinearLayoutManager(this@DashboardEnterprise).also { it.orientation = LinearLayoutManager.HORIZONTAL }
            adapter = DashBoardProjectGoAdapter(projectListData)
        }

        updateProjList(CodeList.project_all)

        binding.allProject.setOnClickListener {
             updateProjList(CodeList.project_all)
        }

        binding.readyProject.setOnClickListener {
            updateProjList(CodeList.project_ready)
        }

        binding.progressProject.setOnClickListener {
            updateProjList(CodeList.project_progress)
        }

        binding.stopProject.setOnClickListener {
            updateProjList(CodeList.project_stop)
        }

        binding.completeProject.setOnClickListener {
            updateProjList(CodeList.project_complete)
        }
    }

    private fun init(){
        drawer = binding.drawerLayout
        navView= binding.navView
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
    }

    private fun updateProjList(code : String){
        val retrofitProjectGo = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/{projectStatus}/").create(ProjectGoService::class.java)

        projectListData.clear()

        retrofitProjectGo.requestProjectsGo(code , CodeList.sysCd, userToken).enqueue(object :
            Callback<ProjectGoDTO> {
            override fun onFailure(call: Call<ProjectGoDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ProjectGoDTO>, response: Response<ProjectGoDTO>) {
                projectGo = response.body()

                Log.d("DashBoard", Gson().toJson(projectGo?.value))
                for (i in 0 until projectGo?.value?.size!!) {
                    projectGoData = DashBoardProjectGo(projectGo?.value?.get(i)?.cons_name.toString(), projectGo?.value?.get(i)?.location.toString(), projectGo?.value?.get(i)?.project_status_name.toString() ,projectGo?.value?.get(i)?.cons_code.toString() ,userInfo?.value?.authority_code.toString(), projectGo?.value?.get(i)?.project_progress!!.toFloat())
                    projectListData.add(projectGoData)
                }
                binding.enterPProjectGoRecycler.adapter?.notifyDataSetChanged()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    private fun projectCount(){
        val getProjectListService = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projStatistManage/getProjStatusStatistics/").create(ProjectListService::class.java)

        getProjectListService.requestProjectsList(CodeList.sysCd, userToken).enqueue(object :
            Callback<ProjectListDTO> {
            override fun onFailure(call: Call<ProjectListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            override fun onResponse(
                call: Call<ProjectListDTO>,
                response: Response<ProjectListDTO>
            ) {
                projectList = response.body()
                if (projectList?.code == 200) {
                    var allProj = 0
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
    }

}