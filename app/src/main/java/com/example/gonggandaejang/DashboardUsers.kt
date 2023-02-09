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
import com.example.gonggandaejang.API.ProjectGoService
import com.example.gonggandaejang.API.ProjectListService
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

class DashboardUsers : AppCompatActivity() {
    private lateinit var binding: ActivityDashUsersBinding
    private lateinit var context : Context

    private lateinit var projectListArray : ArrayList<ProjectList>
    private var allProj = 0
    private var residentProj = 0
    private var normalProj = 0
    private var readyProj = 0
    private var progressProj = 0
    private var stopProj = 0
    private var completeProj = 0

    private lateinit var projectGoData: DashBoardProjectGo
    private var projectListData = arrayListOf<DashBoardProjectGo>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        requestMultiplePermissions(this)

        //프로젝트 상태 통계 현황 조회 표시 =====================================================================================================
        val retroProjList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projStatistManage/getProjStatusStatistics/")
        val getProjectListService : ProjectListService = retroProjList.create(ProjectListService::class.java)

        getProjectListService.requestProjectsList(sysCd, userToken).enqueue(object :
            Callback<ProjectListDTO> {
            override fun onFailure(call: Call<ProjectListDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            override fun onResponse(
                call: Call<ProjectListDTO>,
                response: Response<ProjectListDTO>
            ) {
                projectList = response.body()
                Log.d("projectList", Gson().toJson(projectList?.value))
                if (projectList?.code == 200) {
                    projectListArray = projectList?.value!!
                    val len = projectListArray.size
                    for(i in 0 until len)
                    {
                    allProj += projectListArray[i].cnt

                    when(projectListArray[i].reside_class_cd){
                        "SD010000" -> {
                            residentProj += projectListArray[i].cnt
                        }
                        "SD010001" -> {
                            normalProj += projectListArray[i].cnt
                        }
                        }
                    when (projectListArray[i].proj_status_cd) {
                        project_ready -> {
                            readyProj += projectListArray[i].cnt
                        }
                        project_progress -> {
                            progressProj += projectListArray[i].cnt
                        }
                        project_stop -> {
                            stopProj += projectListArray[i].cnt
                        }
                        project_complete -> {
                            completeProj += projectListArray[i].cnt
                        }
                    }
                 }
                }
                binding.allProj.text = allProj.toString()
                binding.readyProj.text = readyProj.toString()
                binding.progressProj.text = progressProj.toString()
                binding.stopProj.text = stopProj.toString()
                binding.completeProj.text = completeProj.toString()
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
}


