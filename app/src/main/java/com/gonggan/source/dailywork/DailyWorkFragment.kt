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
import com.gonggan.DTO.WorkListResponseDTO
import com.gonggan.objects.*
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
private const val TAG = "DailyWorkFragment"

private var token : String? = null
private var consCode : String? = null
private var searchWork: WorkListResponseDTO? = null

class DailyWatchFragment : Fragment() {
    private lateinit var binding: FragmentDailyWatchBinding

    private var workLogData = ArrayList<WorkData>()
    private lateinit var workLogInputData : WorkData
    private var maxNum = 0
    private var endNum = 10



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDailyWatchBinding.inflate(layoutInflater)

        arguments?.let {
            token = it.getString("token").toString()
            consCode = it.getString("code").toString()
        }

        binding.watchDailyRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = WorkAdapter(requireContext(), workLogData, consCode.toString())
        }

        //초기 접속 조회==============================================================================
        updateDailyWorkDoc(1, "", "")
        //하단 닿으면 추가 조회
        binding.watchDailyRecycler.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!binding.watchDailyRecycler.canScrollVertically(1)) {
                    if(endNum < maxNum){
                       updateDailyWorkDoc(0, "", "")
                       Log.i(ContentValues.TAG, "End of list")
                    }
                }
            }
        })
        //검색 ======================================================================================
        //시작날짜
        binding.startDatePicker.setOnClickListener {
            callSelectCalendar(binding.startDate, requireContext())
        }
        //종료날짜
        binding.endDatePicker.setOnClickListener {
            callSelectCalendar(binding.endDate, requireContext())
        }
        //문서 검색 조회
        binding.searchBtn.setOnClickListener {
            endNum = maxNum
            val startDate = startDate(binding.startDate.text.toString())
            val endDate = endDate(binding.endDate.text.toString())
            updateDailyWorkDoc(2, startDate, endDate)
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

    private fun updateDailyWorkDoc(state : Int, searchStart : String, searchEnd : String){
        val workDiary = callRetrofit("${CodeList.portNum}/projWorkLogManage/WorkDiary/{cons_code}/").create(WorkDiary::class.java)

        workDiary.requestWorkDiary(consCode.toString(), searchStart, searchEnd , "0","$endNum", CodeList.sysCd, token.toString()).enqueue(object :
            Callback<WorkListResponseDTO> {
            override fun onFailure(call: Call<WorkListResponseDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }

            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<WorkListResponseDTO>, response: Response<WorkListResponseDTO>) {
                searchWork = response.body()
                workLogData.clear()

                if (searchWork?.value?.data?.size != null) {
                    for (i in 0 until searchWork?.value?.data!!.size) {
                        //문서명
                        if(state == 1){
                            maxNum = searchWork?.value?.count!!.toInt()
                        }
                        if (state == 0){
                            endNum += 10
                        }
                        val consDate = searchWork?.value?.data?.get(i)?.cons_date.toString()
                        val id = searchWork?.value?.data?.get(i)?.id.toString()
                        val workTitle = searchWork?.value?.data?.get(i)?.work_title.toString()
                        val writeDate = searchWork?.value?.data?.get(i)?.write_date.toString()
                        val sysDocNum = searchWork?.value?.data?.get(i)?.sys_doc_num.toString()
                        workLogInputData = WorkData(consDate, id, workTitle, writeDate, sysDocNum)
                        workLogData.add(workLogInputData)
                    }
                    binding.watchDailyRecycler.adapter?.notifyDataSetChanged()
                    if(state == 2){
                        binding.startDate.text = ""
                        binding.endDate.text = ""
                    }
                }
            }
        })
    }

}