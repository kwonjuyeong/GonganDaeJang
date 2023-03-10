package com.gonggan.source.dailywork

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.FragmentDailyWatchBinding
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.DTO.WorkListResponseDTO
import com.gonggan.objects.*
import com.gonggan.API.GetUserInfoService
import com.gonggan.API.WorkDiary
import com.gonggan.Adapter.WorkAdapter
import com.gonggan.Adapter.WorkData
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

//작업일보 조회 탭

private var token : String? = null
private var consCode : String? = null
private var searchWork: WorkListResponseDTO? = null

class DailyWatchFragment : Fragment() {
    private lateinit var binding: FragmentDailyWatchBinding

    private var maxNum = 0
    private var endNum = 10

    private var workLogData = ArrayList<WorkData>()
    private lateinit var workLogInputData : WorkData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDailyWatchBinding.inflate(layoutInflater)

        arguments?.let {
            token = it.getString("token").toString()
            consCode = it.getString("code").toString()
        }

        //초기 접속 시 전체 문서 조회 ======================================================================================================================
        binding.watchDailyRecycler.layoutManager = LinearLayoutManager(context)
        binding.watchDailyRecycler.adapter = WorkAdapter(requireContext(), workLogData, consCode.toString())

        val retroSearchWorkList = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkLogManage/WorkDiary/{cons_code}/")
        val workDiary : WorkDiary = retroSearchWorkList.create(WorkDiary::class.java)


        workDiary.requestWorkDiary(consCode.toString(), "", "" , "0","$endNum", CodeList.sysCd, token.toString()).enqueue(object :
            Callback<WorkListResponseDTO> {
            override fun onFailure(call: Call<WorkListResponseDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<WorkListResponseDTO>, response: Response<WorkListResponseDTO>) {
                searchWork = response.body()
                workLogData.clear()
                Log.d("response_code", searchWork?.code.toString())
                Log.d("response_msg", searchWork?.msg.toString())
                Log.d("response_value", Gson().toJson(searchWork?.value))

                if (searchWork?.value?.data?.size != null) {
                    for (i in 0 until searchWork?.value?.data!!.size) {
                        //문서명
                        maxNum = searchWork?.value?.count!!.toInt()
                        val consDate = searchWork?.value?.data?.get(i)?.cons_date.toString()
                        val id = searchWork?.value?.data?.get(i)?.id.toString()
                        val workTitle = searchWork?.value?.data?.get(i)?.work_title.toString()
                        val writeDate = searchWork?.value?.data?.get(i)?.write_date.toString()
                        val sysDocNum = searchWork?.value?.data?.get(i)?.sys_doc_num.toString()
                        workLogInputData = WorkData(consDate, id, workTitle, writeDate, sysDocNum)
                        workLogData.add(workLogInputData)
                    }
                    binding.watchDailyRecycler.adapter?.notifyDataSetChanged()
                }
            }
        })
        //리사이클러 하단에 닿으면 추가 검색=======================================================================================================================================================================
        binding.watchDailyRecycler.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!binding.watchDailyRecycler.canScrollVertically(1)) {
                    if(endNum < maxNum){
                        endNum += 10

                        workDiary.requestWorkDiary(consCode.toString(), "",  "", "0","$endNum", CodeList.sysCd, token.toString()).enqueue(object :
                            Callback<WorkListResponseDTO> {
                            override fun onFailure(call: Call<WorkListResponseDTO>, t: Throwable) {
                                Log.d("retrofit", t.toString())
                            }
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onResponse(call: Call<WorkListResponseDTO>, response: Response<WorkListResponseDTO>) {
                                searchWork = response.body()
                                workLogData.clear()
                                for (i in 0 until searchWork?.value?.data!!.size) {
                                    val consDate = searchWork?.value?.data?.get(i)?.cons_date.toString()
                                    val id = searchWork?.value?.data?.get(i)?.id.toString()
                                    val workTitle = searchWork?.value?.data?.get(i)?.work_title.toString()
                                    val writeDate = searchWork?.value?.data?.get(i)?.write_date.toString()
                                    val sysDocNum = searchWork?.value?.data?.get(i)?.sys_doc_num.toString()
                                    workLogInputData = WorkData(consDate, id, workTitle, writeDate,sysDocNum)
                                    workLogData.add(workLogInputData)
                                }
                                binding.watchDailyRecycler.adapter?.notifyDataSetChanged()
                            }
                        })
                        Log.i(ContentValues.TAG, "End of list")
                    }
                }
            }
        })

        //검색조건 - 날짜 선택=============================================================================================================================================
        binding.startDatePicker.setOnClickListener {
            binding.startDate.text = ""
            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                binding.startDate.text = getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
            }
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
        }

        binding.endDatePicker.setOnClickListener {
            binding.endDate.text = ""
            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                binding.endDate.text = getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
            }
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
        }

            //문서 검색 조회===============================================================================================================================================
            binding.searchBtn.setOnClickListener {
                endNum = maxNum
                val startDate = startDate(binding.startDate.text.toString())
                val endDate = endDate(binding.endDate.text.toString())
                Log.d("input_conCode", consCode.toString())

                workDiary.requestWorkDiary(consCode.toString(), startDate,  endDate, "0","$endNum", CodeList.sysCd, token.toString()).enqueue(object :
                    Callback<WorkListResponseDTO> { override fun onFailure(call: Call<WorkListResponseDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(
                        call: Call<WorkListResponseDTO>,
                        response: Response<WorkListResponseDTO>
                    ) {
                        searchWork = response.body()
                        workLogData.clear()
                        Log.d("searchData", Gson().toJson(searchWork?.value))
                        if (searchWork?.value?.data?.size != null) {
                            for (i in 0 until searchWork?.value?.data!!.size) {
                                val consDate = searchWork?.value?.data?.get(i)?.cons_date.toString()
                                val id = searchWork?.value?.data?.get(i)?.id.toString()
                                val workTitle = searchWork?.value?.data?.get(i)?.work_title.toString()
                                val writeDate = searchWork?.value?.data?.get(i)?.write_date.toString()
                                val sysDocNum = searchWork?.value?.data?.get(i)?.sys_doc_num.toString()
                                workLogInputData = WorkData(consDate, id, workTitle, writeDate, sysDocNum)
                                workLogData.add(workLogInputData)
                            }
                            binding.watchDailyRecycler.adapter?.notifyDataSetChanged()
                            binding.startDate.text = ""
                            binding.endDate.text = ""
                        }
                    }
                })
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = DailyWatchFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}