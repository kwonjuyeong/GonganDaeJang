package com.gonggan.source.qa

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.FragmentQABinding
import com.gonggan.API.SearchQaList
import com.gonggan.Adapter.QaAdapter
import com.gonggan.DTO.QALists
import com.gonggan.DTO.SearchQAListDTO
import com.gonggan.objects.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.max


private const val TAG = "QAFRAGMENT"
private var token : String? = null
private var consCode : String? = null
private var searchQA: SearchQAListDTO? = null

class QAFragment : Fragment() {
    private lateinit var binding: FragmentQABinding
    //필터 클릭 시 열림(0 : 닫혀있음 1 : 열려있음)
    private var filterState : Int = 0
    private var maxNum = 0
    private var endNum = 10
    private var qaData = ArrayList<QALists>()
    private lateinit var qaInputData : QALists

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentQABinding.inflate(layoutInflater)

        arguments?.let {
            token = it.getString("token").toString()
            consCode = it.getString("code").toString()
        }

        //초기 접속 시 전체 문서 조회 ======================================================================================================================
        val retroSearchQaList = ApiUtilities.callRetrofit("http://211.107.220.103:${CodeList.portNum}/projMessageBoardManage/MessageBoardList/{cons_code}/").create(SearchQaList::class.java)

        binding.qARecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = QaAdapter(requireContext(), qaData, consCode.toString())
        }
        retroSearchQaList.requestQaList(consCode.toString() ,CodeList.sysCd, token.toString(), "0", "$endNum", "", "", "", "", "", "", "").enqueue(object :
            Callback<SearchQAListDTO> {
            override fun onFailure(call: Call<SearchQAListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<SearchQAListDTO>, response: Response<SearchQAListDTO>) {
                searchQA = response.body()
                Log.d("serachqa", Gson().toJson(searchQA?.value))
                qaData.clear()
                if (searchQA?.value?.data?.size != null) {
                    for (i in 0 until searchQA?.value?.data!!.size) {
                        //문서명
                        maxNum = searchQA?.value?.count!!.toInt()
                        val coName  = searchQA?.value?.data?.get(i)?.co_name.toString()
                        val postType  = searchQA?.value?.data?.get(i)?.post_type.toString()
                        val regDate = searchQA?.value?.data?.get(i)?.reg_date.toString()
                        val title = searchQA?.value?.data?.get(i)?.title.toString()
                        val uuid = searchQA?.value?.data?.get(i)?.uuid.toString()
                        val writerId = searchQA?.value?.data?.get(i)?.writer_id.toString()
                        val writerName = searchQA?.value?.data?.get(i)?.writer_name.toString()
                        qaInputData = QALists(coName, postType, regDate, title, uuid, writerId, writerName)
                        qaData.add(qaInputData)
                    }
                    binding.qARecycler.adapter?.notifyDataSetChanged()
                }
                binding.cnt.text = searchQA?.value?.count.toString()
            }
        })
        //리사이클러 하단에 닿으면 추가 검색=======================================================================================================================================================================
        binding.qARecycler.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!binding.qARecycler.canScrollVertically(1)) {
                    if(endNum < maxNum){
                        endNum += 10
                        retroSearchQaList.requestQaList(consCode.toString() ,CodeList.sysCd, token.toString(), "0", "$endNum", "", "", "", "", "", "", "").enqueue(object :
                            Callback<SearchQAListDTO> {
                            override fun onFailure(call: Call<SearchQAListDTO>, t: Throwable) {
                                Log.d("retrofit", t.toString())
                            }
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onResponse(call: Call<SearchQAListDTO>, response: Response<SearchQAListDTO>) {
                                searchQA = response.body()
                                qaData.clear()
                                if (searchQA?.value?.data?.size != null) {
                                    for (i in 0 until searchQA?.value?.data!!.size) {
                                        val coName  = searchQA?.value?.data?.get(i)?.co_name.toString()
                                        val postType  = searchQA?.value?.data?.get(i)?.post_type.toString()
                                        val regDate = searchQA?.value?.data?.get(i)?.reg_date.toString()
                                        val title = searchQA?.value?.data?.get(i)?.title.toString()
                                        val uuid = searchQA?.value?.data?.get(i)?.uuid.toString()
                                        val writerId = searchQA?.value?.data?.get(i)?.writer_id.toString()
                                        val writerName = searchQA?.value?.data?.get(i)?.writer_name.toString()
                                        qaInputData = QALists(coName, postType, regDate, title, uuid, writerId, writerName)
                                        qaData.add(qaInputData)
                                    }
                                    binding.qARecycler.adapter?.notifyDataSetChanged()
                                }
                            }
                        })
                        Log.i(ContentValues.TAG, "End of list")
                    }
                }
            }
        })

        //문서 검색 기능(조건)===================================================================================================================================
        binding.selectionFilter.setOnClickListener {
            if(filterState == 0) {
                //문서 상태 스피너
                filterState = 1
                binding.filterLayout.visibility = View.VISIBLE
                }
            else if(filterState == 1){
                filterState = 0
                binding.filterLayout.visibility = View.GONE
                binding.regStartDate.text = ""
                binding.regEndDate.text = ""
                binding.writerSearch.setText("")
                binding.coNameSearch.setText("")
                binding.titleSearch.setText("")
                binding.contentSearch.setText("")
            }
        }
        //검색조건 - 날짜 선택=============================================================================================================================================
        binding.startDatePicker.setOnClickListener {
            binding.regStartDate.text = ""
            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                binding.regStartDate.text = getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
            }
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
        }

        binding.endDatePicker.setOnClickListener {
            binding.regEndDate.text = ""
            val cal = Calendar.getInstance()
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                binding.regEndDate.text = getString(R.string.calender_day_format, year.toString(), getMonth(month), getDay(dayOfMonth))
            }
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = Date().time }.show()
        }

        binding.searchBtn.setOnClickListener {
            maxNum = endNum

            retroSearchQaList.requestQaList(consCode.toString() ,CodeList.sysCd, token.toString(), "0", "$maxNum", binding.coNameSearch.text.toString(), binding.writerSearch.text.toString(), "", binding.titleSearch.text.toString() , binding.contentSearch.text.toString(),startDate(binding.regStartDate.text.toString()),endDate(binding.regEndDate.text.toString())).enqueue(object :
                Callback<SearchQAListDTO> {
                override fun onFailure(call: Call<SearchQAListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<SearchQAListDTO>, response: Response<SearchQAListDTO>) {
                    searchQA = response.body()
                    qaData.clear()

                    Log.d("input_result_data", Gson().toJson(searchQA?.value))

                    if (searchQA?.value?.data?.size != null) {
                        for (i in 0 until searchQA?.value?.data!!.size) {
                            //문서명
                            maxNum = searchQA?.value?.count!!.toInt()
                            val coName  = searchQA?.value?.data?.get(i)?.co_name.toString()
                            val postType  = searchQA?.value?.data?.get(i)?.post_type.toString()
                            val regDate = searchQA?.value?.data?.get(i)?.reg_date.toString()
                            val title = searchQA?.value?.data?.get(i)?.title.toString()
                            val uuid = searchQA?.value?.data?.get(i)?.uuid.toString()
                            val writerId = searchQA?.value?.data?.get(i)?.writer_id.toString()
                            val writerName = searchQA?.value?.data?.get(i)?.writer_name.toString()
                            qaInputData = QALists(coName, postType, regDate, title, uuid, writerId, writerName)
                            qaData.add(qaInputData)
                        }
                        binding.qARecycler.adapter?.notifyDataSetChanged()
                    }
                    binding.cnt.text = searchQA?.value?.count.toString()
                    binding.filterLayout.visibility = View.GONE
                }
            })
        }


        //문서 작성 버튼
        binding.postBtn.setOnClickListener {
            val intent = Intent(context, QAPost::class.java)
            intent.putExtra("code", consCode)
            startActivity(intent)
            (context as Activity).finish()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  {
        return binding.root
    }

}