package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.DailyWorkDTO
import com.allscapeservice.a22allscape_app.DTO.ImageData
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.allscapeservice.a22allscape_app.objects.convertDateFormat
import com.example.gonggandaejang.API.WorkDetailDiary
import com.example.gonggandaejang.Adapter.*
import com.example.gonggandaejang.databinding.ActivityDailyWorkDocumentBinding
import com.example.gonggandaejang.objects.CodeList
import com.google.gson.Gson
import org.w3c.dom.Text
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

    //작업량
    private var consWorkInfoData = arrayListOf<ConsWorkInfoData>()
    private lateinit var consWorkInfoInputData : ConsWorkInfoData

    //인력
    private var consManpInfoData = arrayListOf<ConsWorkInfoData>()
    private lateinit var consManpInfoInputData : ConsWorkInfoData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyWorkDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        //작업일보 상세보기====================================================================================================================
        //작업량
        binding.consWorkInfoRecycler.layoutManager = LinearLayoutManager(this)
        binding.consWorkInfoRecycler.adapter = ConsWorkInfoAdapter(this, consWorkInfoData)
        //인력


        val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/DailyWork/{cons_code}/{sys_doc_num}/")
        val detailDoc: WorkDetailDiary = retrofit.create(WorkDetailDiary::class.java)

        detailDoc.requestDailyWork(constCode, sysDocNum, CodeList.sysCd, userToken).enqueue(object :
            Callback<DailyWorkDTO> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onFailure(call: Call<DailyWorkDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<DailyWorkDTO>, response: Response<DailyWorkDTO>) {
                dailywork = response.body()

                //공사일
                binding.consDate.text = convertDateFormat(dailywork?.value?.cons_date.toString())

                //금일 공사 내용, 명일 예상 작업 내용
                binding.nextContent.text = dailywork?.value?.cons_content_info?.next_content.toString()
                binding.todayContent.text = dailywork?.value?.cons_content_info?.today_content.toString()

                //공사일지 내용
                binding.workDiary.text = dailywork?.value?.work_diary.toString()

                val insideVersionList = ArrayList<String>()
                //공사일보 작업량
                Log.d("daily_code", Gson().toJson(dailywork))
                for(i in 0 until dailywork?.value?.cons_work_info?.size!!){
                        if (!insideVersionList.contains(dailywork?.value?.cons_work_info?.get(i)?.cons_type_nm.toString())) {
                            insideVersionList.add(dailywork?.value?.cons_work_info?.get(i)?.cons_type_nm.toString())
                            val division = arrayListOf<ConsWorkInputList>()

                            consWorkInfoInputData = ConsWorkInfoData(dailywork?.value?.cons_work_info?.get(i)?.cons_type_cd.toString(), dailywork?.value?.cons_work_info?.get(i)?.cons_type_explain.toString(),dailywork?.value?.cons_work_info?.get(i)?.cons_type_nm.toString(),dailywork?.value?.cons_work_info?.get(i)?.level1_name.toString(),dailywork?.value?.cons_work_info?.get(i)?.level2_name.toString(),dailywork?.value?.cons_work_info?.get(i)?.level3_name.toString(),dailywork?.value?.cons_work_info?.get(i)?.next_workload!!.toInt(),dailywork?.value?.cons_work_info?.get(i)?.prev_workload!!.toInt(),dailywork?.value?.cons_work_info?.get(i)?.product.toString(),dailywork?.value?.cons_work_info?.get(i)?.quantity!!.toInt(),dailywork?.value?.cons_work_info?.get(i)?.today_workload!!.toInt(),dailywork?.value?.cons_work_info?.get(i)?.total_workload!!.toInt(), dailywork?.value?.cons_work_info?.get(i)?.unit.toString(),dailywork?.value?.cons_work_info?.get(i)?.work_log_cons_code.toString(),dailywork?.value?.cons_work_info?.get(i)?.work_log_cons_lv1!!.toInt(),dailywork?.value?.cons_work_info?.get(i)?.work_log_cons_lv2!!.toInt(),dailywork?.value?.cons_work_info?.get(i)?.work_log_cons_lv3!!.toInt(),dailywork?.value?.cons_work_info?.get(i)?.work_log_cons_lv4!!.toInt(), division)
                            consWorkInfoData.add(consWorkInfoInputData)

                            for (m in 0 until consWorkInfoData.size) {
                                for (j in 0 until dailywork?.value?.cons_work_info!!.size) {
                                    if (consWorkInfoData[m].cons_type_nm == dailywork?.value?.cons_work_info?.get(j)?.cons_type_nm.toString()) {
                                        consWorkInfoData[m].ConsWorkInfoInputListData.add(
                                            ConsWorkInputList(
                                            dailywork?.value?.cons_work_info?.get(j)?.cons_type_cd.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.cons_type_explain.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.cons_type_nm.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.level1_name.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.level2_name.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.level3_name.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.next_workload!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.prev_workload!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.product.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.quantity!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.today_workload!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.total_workload!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.unit.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_code.toString(),
                                            dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv1!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv2!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv3!!.toInt(),
                                            dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv4!!.toInt())
                                        )

                                    }
                                }
                            }
                        }
                    binding.consWorkInfoRecycler.adapter?.notifyDataSetChanged()
                }


                /*
                //공사일보 인력
                for(i in 0 until dailywork?.value?.cons_manp_info?.size!!){
                    consManpInfoInputData = ConsWorkInfoData()
                    consManpInfoData.add(consManpInfoInputData)
                }
                binding.consManpInfoRecycler.adapter?.notifyDataSetChanged()
            */
            }
        })

        binding.bottomBtn.setOnClickListener {
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