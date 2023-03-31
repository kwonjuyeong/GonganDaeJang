package com.gonggan.source.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityDashNormalBinding
import com.gonggan.API.*
import com.gonggan.Adapter.CoHistoryAdapter
import com.gonggan.Adapter.DashBoardNoCompanyProjectList
import com.gonggan.Adapter.DashBoardNoCompanyProjectListAdapter
import com.gonggan.DTO.*
import com.gonggan.objects.*
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.source.mypage.MyPageActivity
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "DashNormal"

private var history : CoHistoryDTO? = null
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

    private val historyData = arrayListOf<CoHistoryD>()
    private lateinit var historyInputData : CoHistoryD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashNormalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        openDrawer()
        navigationItemClick()
        requestMultiplePermissions(this)
        //날씨정보 표시
        callWeatherInfo(userToken, this@DashNormal, binding.weatherIcon, binding.weatherSet)
        //시간정보 표시
        CoroutineScope(Dispatchers.IO).launch {
            job = callTimeSet(userToken, binding.timeSet)
            job!!.join()
        }

        //재직 이력 정보 조회 추가(수정)
        binding.noCompanyRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CoHistoryAdapter(historyData) { changeProjList(it) }
        }

        val retrofitCoHisList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/historyManage/getCoHisList/").create(GetCoHistory::class.java)
        retrofitCoHisList.requestGetCoHistory(CodeList.sysCd, userToken).enqueue(object : Callback<CoHistoryDTO> {
            override fun onFailure(call: Call<CoHistoryDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<CoHistoryDTO>,
                response: Response<CoHistoryDTO>
            ) {
                history = response.body()
                //회사정보 Retrofit 불러오기
                val getCoListService = callRetrofit("http://211.107.220.103:${CodeList.portNum}/commManage/getCoList/ALL/").create(GetCoListService::class.java)

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

                            historyInputData = CoHistoryD(coAddress, coCeo, coCode, coName, coContact, tenureEndDate, tenureStartDate, id)
                            historyData.add(historyInputData)
                        }
                        binding.noCompanyRecycler.adapter?.notifyDataSetChanged()
                    }
                })
            }
        })

        //프로젝트 이력 정보 조회 추가(수정)
        binding.noCompanyProjectGoRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DashBoardNoCompanyProjectListAdapter(projectListData)
        }

         val retrofitProjHisList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/historyManage/getCoHisList/").create(GetProjHistory::class.java)
        retrofitProjHisList.requestGetProjHistory(CodeList.sysCd, userToken).enqueue(object : Callback<ProjHistoryDTO> {
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
    private fun changeProjList(data : CoHistoryD) {
        val getProjHistory = callRetrofit("http://211.107.220.103:${CodeList.portNum}/historyManage/getCoHisList/").create(GetProjHistory::class.java)
        getProjHistory.requestGetProjHistory(CodeList.sysCd, userToken).enqueue(object : Callback<ProjHistoryDTO> {
            override fun onFailure(call: Call<ProjHistoryDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ProjHistoryDTO>, response: Response<ProjHistoryDTO>) {
                projHistory = response.body()
                Log.d("project refreshed", Gson().toJson(projHistory?.value))
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