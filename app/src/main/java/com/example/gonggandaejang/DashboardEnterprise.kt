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
import com.example.gonggandaejang.API.ProjectGoService
import com.example.gonggandaejang.API.ProjectListService
import com.example.gonggandaejang.Adapter.DashBoardProjectGo
import com.example.gonggandaejang.Adapter.DashBoardProjectGoAdapter
import com.example.gonggandaejang.databinding.ActivityDashEnterpriseBinding
import com.example.gonggandaejang.objects.CodeList
import com.example.gonggandaejang.objects.startCloseLogoutCustom
import com.google.gson.Gson
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
class DashboardEnterprise : AppCompatActivity() {

        private lateinit var binding: ActivityDashEnterpriseBinding
    //프로젝트 리스트 조회=============================================================================
    private lateinit var projectListArray : ArrayList<ProjectList>
    //프로젝트 이동 조회=============================================================================
    private val projectListData = arrayListOf<DashBoardProjectGo>()
    private lateinit var projectGoData: DashBoardProjectGo
    private var allCount = 0
    private var allReg = 0
    private var allNormal = 0

    private var readyAll = 0
    private var readyReg = 0
    private var readyNormal = 0

    private var progressAll = 0
    private var progressReg = 0
    private var progressNormal = 0

    private var stopAll = 0
    private var stopReg = 0
    private var stopNormal = 0

    private var completeAll = 0
    private var completeReg = 0
    private var completeNormal = 0
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashEnterpriseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        //사용자 권한 체크
        requestMultiplePermissions(this)

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

                Log.d("projectList", Gson().toJson(projectList?.value))

                if (projectList?.code == 200) {
                    projectListArray = projectList?.value!!
                    val len = projectListArray.size

                    for(i in 0 until len) {

                        allCount += projectListArray[i].cnt

                        when(projectListArray[i].proj_status_cd)
                        {
                            "ST000001" -> {
                                readyAll += projectListArray[i].cnt
                            }
                            "ST000002" -> {
                                progressAll += projectListArray[i].cnt
                            }
                            "ST0000003"-> {
                                stopAll += projectListArray[i].cnt
                            }
                            "ST000004" ->{
                                completeAll += projectListArray[i].cnt
                            }
                        }

                        if(projectListArray[i].reside_class_cd == "SD010000"){
                            allReg += projectListArray[i].cnt

                            when(projectListArray[i].proj_status_cd){
                                "ST000001" -> {
                                    readyReg += projectListArray[i].cnt
                                }
                                "ST000002" -> {
                                    progressReg += projectListArray[i].cnt
                                }
                                "ST0000003"-> {
                                    stopReg += projectListArray[i].cnt
                                }
                                "ST000004" ->{
                                    completeReg += projectListArray[i].cnt
                                }
                            }
                        }else if(projectListArray[i].reside_class_cd == "SD010001"){
                            allNormal += projectListArray[i].cnt
                            when(projectListArray[i].proj_status_cd){
                                "ST000001" -> {
                                    readyNormal += projectListArray[i].cnt
                                }
                                "ST000002" -> {
                                    progressNormal += projectListArray[i].cnt
                                }
                                "ST0000003"-> {
                                    stopNormal += projectListArray[i].cnt
                                }
                                "ST000004" ->{
                                    completeNormal += projectListArray[i].cnt
                                }
                            }
                        }
                    }
                    binding.allProj.text = allCount.toString()
                    binding.allResident.text = allReg.toString()
                    binding.allNormal.text = allNormal.toString()
                    binding.readyProj.text = readyAll.toString()
                    binding.progressProj.text = progressAll.toString()
                    binding.stopProj.text = stopAll.toString()
                    binding.completeProj.text = completeAll.toString()
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