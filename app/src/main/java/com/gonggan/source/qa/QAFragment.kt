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
    private var qaData = ArrayList<QALists>()
    private lateinit var qaInputData : QALists

    //state
    //검색 필터
    private var filterState : Int = 0
    private var maxNum = 0
    private var endNum = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentQABinding.inflate(layoutInflater)

        arguments?.let {
            token = it.getString("token").toString()
            consCode = it.getString("code").toString()
        }

        binding.qARecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = QaAdapter(requireContext(), qaData, consCode.toString())
        }

        //state(0 = 초기 검색, 1 = 하단 접합 검색, 2 = 검색조건 검색)
        updateQAContents(0, "","","","","","")
        binding.qARecycler.setOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (!binding.qARecycler.canScrollVertically(1)) {
                if(endNum < maxNum){
                updateQAContents(1, "","","","","","")
                Log.i(ContentValues.TAG, "End of list")
                }
            }
        }
        })

        //문서 검색 기능(조건)
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
        //날짜 선택
        binding.startDatePicker.setOnClickListener {
            callSelectCalendar(binding.regStartDate, requireContext())
        }
        binding.endDatePicker.setOnClickListener {
           callSelectCalendar(binding.regEndDate, requireContext())
        }

        binding.searchBtn.setOnClickListener {
            maxNum = endNum
            updateQAContents(2, startDate(binding.regStartDate.text.toString()), endDate(binding.regEndDate.text.toString()), binding.coNameSearch.text.toString(), binding.writerSearch.text.toString(), binding.titleSearch.text.toString(), binding.contentSearch.text.toString())
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


    private fun updateQAContents(state : Int, startDate : String, endDate : String, searchCoName : String, writer : String, titleKeyword : String, contentKeyWord : String){
        val retroSearchQaList = ApiUtilities.callRetrofit("${CodeList.portNum}/projMessageBoardManage/MessageBoardList/{cons_code}/").create(SearchQaList::class.java)

        if(state == 1){
            endNum += 10
        }
        retroSearchQaList.requestQaList(consCode.toString() ,CodeList.sysCd, token.toString(), "0", "$endNum", searchCoName, writer, "", titleKeyword, contentKeyWord, startDate, endDate).enqueue(object :
            Callback<SearchQAListDTO> {
            override fun onFailure(call: Call<SearchQAListDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<SearchQAListDTO>, response: Response<SearchQAListDTO>) {
                searchQA = response.body()
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
                if(state == 2){
                binding.filterLayout.visibility = View.GONE
                }
            }
        })
    }

}