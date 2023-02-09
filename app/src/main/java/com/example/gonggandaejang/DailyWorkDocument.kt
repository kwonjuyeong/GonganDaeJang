package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.DailyWorkDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.example.gonggandaejang.API.WorkDetailDiary
import com.example.gonggandaejang.Adapter.DashBoardProjectGoAdapter
import com.example.gonggandaejang.databinding.ActivityDailyWorkDocumentBinding
import com.example.gonggandaejang.objects.CodeList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//작업일보 문서

private var dailywork: DailyWorkDTO? = null

class DailyWorkDocument : AppCompatActivity() {

    private lateinit var binding: ActivityDailyWorkDocumentBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var constCode : String
    private lateinit var sysDocNum : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyWorkDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        //작업일보 상세보기====================================================================================================================
        //작업일지 사진 데이터
        //binding.workPictureRecycler.layoutManager = LinearLayoutManager(this)
        //binding.workPictureRecycler.adapter = DashBoardProjectGoAdapter(projectListData)

        //공사일보
        //binding.workDocRecycler.layoutManager = LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.HORIZONTAL}
        //binding.workDocRecycler.adapter = DashBoardProjectGoAdapter(projectListData)

        val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/DailyWork/{cons_code}/{sys_doc_num}/")
        val detailDoc: WorkDetailDiary = retrofit.create(WorkDetailDiary::class.java)

        detailDoc.requestDailyWork(constCode, sysDocNum, CodeList.sysCd, userToken).enqueue(object :
            Callback<DailyWorkDTO> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onFailure(call: Call<DailyWorkDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<DailyWorkDTO>, response: Response<DailyWorkDTO>) {
                dailywork = response.body()





            }
        })




        binding.bottomBtn.setOnClickListener {
            //문서 등록



            finish()
        }

    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        sysDocNum = intent.getStringExtra("sysDocNum")!!
        constCode = intent.getStringExtra("code")!!
    }


}