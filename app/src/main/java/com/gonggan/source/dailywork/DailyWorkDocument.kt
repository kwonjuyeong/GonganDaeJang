package com.gonggan.source.dailywork

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.databinding.ActivityDailyWorkDocumentBinding
import com.gonggan.API.WorkDetailDiary
import com.gonggan.Adapter.*
import com.gonggan.DTO.DailyWorkDTO
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import com.gonggan.objects.convertDateFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//작업일보 문서
private const val TAG = "DailyWorkDocument"
private var dailywork: DailyWorkDTO? = null

class DailyWorkDocument : AppCompatActivity() {
    private lateinit var binding: ActivityDailyWorkDocumentBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var consCode : String
    private lateinit var sysDocNum : String
    private lateinit var userId : String

    //작업량
    private var consWorkInfoData = arrayListOf<ConsWorkInfoData>()
    private lateinit var consWorkInfoInputData : ConsWorkInfoData

    //인력
    private var consManpInfoData = arrayListOf<ConsManPInfoData>()
    private lateinit var consManpInfoInputData : ConsManPInfoData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyWorkDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.apply {
            title = "공사일보 조회"
        }

        //작업일보 상세보기====================================================================================================================
        //작업량
        binding.consWorkInfoRecycler.apply {
            layoutManager = LinearLayoutManager(this@DailyWorkDocument).also { it.orientation = LinearLayoutManager.HORIZONTAL }
            adapter = ConsWorkInfoAdapter(consWorkInfoData, sysDocNum, consCode, userId)
        }
        //인력
        binding.consManpInfoRecycler.apply {
            layoutManager = LinearLayoutManager(this@DailyWorkDocument).also { it.orientation = LinearLayoutManager.HORIZONTAL }
            adapter = ConsManPInfoAdapter(this@DailyWorkDocument, consManpInfoData)
        }

        updateDailyWork()

        //댓글 작성
        binding.commentBtn.setOnClickListener {
            val intent = Intent(this@DailyWorkDocument, CommentActivity ::class.java)
            intent.putExtra("sysDocNum", sysDocNum)
            startActivity(intent)
        }

        binding.bottomBtn.setOnClickListener {
            finish()
        }
    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        sysDocNum = intent.getStringExtra("sysDocNum")!!
        consCode = intent.getStringExtra("code")!!
        userId = intent.getStringExtra("userId")!!
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
        finish()
    }



    private fun updateDailyWork(){
        val detailDoc = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/DailyWork/{cons_code}/{sys_doc_num}/").create(WorkDetailDiary::class.java)

        detailDoc.requestDailyWork(consCode, sysDocNum, CodeList.sysCd, userToken).enqueue(object :
            Callback<DailyWorkDTO> {
            override fun onFailure(call: Call<DailyWorkDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<DailyWorkDTO>, response: Response<DailyWorkDTO>) {
                dailywork = response.body()

                //프로젝트 정보 표시
                binding.consDate.text = convertDateFormat(dailywork?.value?.cons_date.toString())
                binding.nextContent.text = dailywork?.value?.cons_content_info?.next_content.toString()
                binding.todayContent.text = dailywork?.value?.cons_content_info?.today_content.toString()
                binding.workDiary.text = dailywork?.value?.work_diary.toString()


                //공사일보 작업량 조회===========================================================================================
                val insideConsTypeWork = ArrayList<String>()
                val insideConsTypeList2 = ArrayList<String>()

                consWorkInfoData.clear()
                consManpInfoData.clear()

                for(i in 0 until dailywork?.value?.cons_work_info?.size!!){
                    if (!insideConsTypeWork.contains(dailywork?.value?.cons_work_info?.get(i)?.cons_type_nm.toString())) {
                        insideConsTypeWork.add(dailywork?.value?.cons_work_info?.get(i)?.cons_type_nm.toString())
                        val division = arrayListOf<ConsWorkInputList>()
                        consWorkInfoInputData = ConsWorkInfoData(dailywork?.value?.cons_work_info?.get(i)?.cons_type_nm.toString(), dailywork?.value?.cons_date.toString() ,division)
                        consWorkInfoData.add(consWorkInfoInputData)
                    }
                }
                for (m in 0 until consWorkInfoData.size) {
                    for (j in 0 until dailywork?.value?.cons_work_info!!.size) {
                        if (consWorkInfoData[m].cons_type_nm == dailywork?.value?.cons_work_info?.get(j)?.cons_type_nm.toString()) {
                            val imageData = arrayListOf<ImageInputList>()
                            var imageInputData : ImageInputList
                            imageData.clear()
                            for(l in 0 until dailywork?.value?.cons_work_info!![j].imageList.size){
                                imageInputData = ImageInputList(dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.chan_name.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.cons_date.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.cons_type_cd.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.cons_type_nm.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.cons_type_explain.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.file_index!!.toInt(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.orig_name.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.filePath.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.title.toString(),
                                    dailywork?.value?.cons_work_info?.get(j)?.imageList?.get(l)?.upload_date.toString())
                                imageData.add(imageInputData)
                            }
                            consWorkInfoData[m].ConsWorkInfoInputListData.add(ConsWorkInputList(
                                dailywork?.value?.cons_work_info?.get(j)?.cons_type_cd.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.cons_type_explain.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.cons_type_nm.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.level1_name.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.level2_name.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.level3_name.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.next_workload!!.toInt(),
                                dailywork?.value?.cons_work_info?.get(j)?.prev_workload!!.toInt(),
                                dailywork?.value?.cons_work_info?.get(j)?.product.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.quantity!!.toFloat(),
                                dailywork?.value?.cons_work_info?.get(j)?.today_workload!!.toInt(),
                                dailywork?.value?.cons_work_info?.get(j)?.total_workload!!.toInt(),
                                dailywork?.value?.cons_work_info?.get(j)?.unit.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_code.toString(),
                                dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv1!!.toInt(),
                                dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv2!!.toInt(),
                                dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv3!!.toInt(),
                                dailywork?.value?.cons_work_info?.get(j)?.work_log_cons_lv4!!.toInt(), imageData))

                        }
                    }
                }
                binding.consWorkInfoRecycler.adapter?.notifyDataSetChanged()

                //인력조회===================================================================================================

                for(i in 0 until dailywork?.value?.cons_manp_info?.size!!){
                    if (!insideConsTypeList2.contains(dailywork?.value?.cons_manp_info?.get(i)?.cons_type_nm.toString())) {
                        insideConsTypeList2.add(dailywork?.value?.cons_manp_info?.get(i)?.cons_type_nm.toString())
                        val division = arrayListOf<ConsManPInputList>()
                        consManpInfoInputData = ConsManPInfoData(dailywork?.value?.cons_manp_info?.get(i)?.cons_type_nm.toString() ,division)
                        consManpInfoData.add(consManpInfoInputData)
                    }
                }

                for (m in 0 until consManpInfoData.size) {
                    for (j in 0 until dailywork?.value?.cons_manp_info!!.size) {
                        if (consManpInfoData[m].cons_type_nm == dailywork?.value?.cons_manp_info?.get(j)?.cons_type_nm.toString()) {
                            consManpInfoData[m].ConsManPInfoInputListData.add(ConsManPInputList(
                                dailywork?.value?.cons_manp_info?.get(j)?.cons_type_cd.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.cons_type_explain.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.cons_type_nm.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.level1_name.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.level2_name.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.level3_name.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.next_manpower!!.toInt(),
                                dailywork?.value?.cons_manp_info?.get(j)?.prev_manpower!!.toInt(),
                                dailywork?.value?.cons_manp_info?.get(j)?.product.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.today_manpower!!.toInt(),
                                dailywork?.value?.cons_manp_info?.get(j)?.work_log_cons_code.toString(),
                                dailywork?.value?.cons_manp_info?.get(j)?.work_log_cons_lv1!!.toInt(),
                                dailywork?.value?.cons_manp_info?.get(j)?.work_log_cons_lv2!!.toInt(),
                                dailywork?.value?.cons_manp_info?.get(j)?.work_log_cons_lv3!!.toInt(),
                                dailywork?.value?.cons_manp_info?.get(j)?.work_log_cons_lv4!!.toInt()))
                        }
                    }
                }
                binding.consManpInfoRecycler.adapter?.notifyDataSetChanged()
            }
        })
    }

}