package com.gonggan.Adapter

//작업량 2차 구분 아이템

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemConsWorkInfoInsideBinding
import com.gonggan.source.dailywork.DailyWorkWatchPostedPhoto
import com.gonggan.source.dailywork.WorkEditPhoto
import com.google.gson.Gson
import java.text.DecimalFormat

class ConsWorkInfoInsideAdapter(private val context: Context, private val dataset: List<ConsWorkInputList>, private val sysDocCode : String, private val consCode : String, private val consDate : String, private val userId : String):
    RecyclerView.Adapter<ConsWorkInfoInsideAdapter.ConsWorkInfoInsideViewHolder>() {

    class ConsWorkInfoInsideViewHolder(val binding: ItemConsWorkInfoInsideBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsWorkInfoInsideViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_cons_work_info_inside, viewGroup, false)
        return ConsWorkInfoInsideViewHolder(ItemConsWorkInfoInsideBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: ConsWorkInfoInsideViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.root.setOnClickListener {
            Log.d("listposition", Gson().toJson(listPosition))
        }

        //작업량 inside data================================================================================================================================
        viewHolder.binding.totalWorkload.text = listPosition.total_workload.toString()
        viewHolder.binding.todayWorkload.text = listPosition.today_workload.toString()
        viewHolder.binding.nextWorkload.text = listPosition.next_workload.toString()
        viewHolder.binding.prevWorkload.text = listPosition.prev_workload.toString()

        val floor = listPosition.level1_name.split(" ")

        if(floor[0].contains("F")){
            viewHolder.binding.floor.text = floor[0]
        }

        val tDecUp = DecimalFormat("#,###")
        viewHolder.binding.quantity.text = tDecUp.format(listPosition.quantity)
        viewHolder.binding.unit.text = listPosition.unit
        viewHolder.binding.workLogConsCode.text = listPosition.work_log_cons_code

        if(listPosition.level2_name != ""){
            listPosition.level2_name = " | ${listPosition.level2_name}"
        }
        if(listPosition.level3_name != ""){
            listPosition.level3_name = " | ${listPosition.level3_name}"
        }
        if(listPosition.product != ""){
            listPosition.product = " | ${listPosition.product}"
        }
        val levelNameResult = "${listPosition.level1_name}${listPosition.level2_name}${listPosition.level3_name}${listPosition.product}"
        viewHolder.binding.levelName.text = "($levelNameResult)"

        //파일이 없으면 조회 화면으로 돌려주는 로직 구현
        if(listPosition.imageList.isEmpty()){
            viewHolder.binding.pictureBtn.visibility = GONE
            viewHolder.binding.picturePostBtn.visibility = VISIBLE
        }else{
            viewHolder.binding.pictureBtn.visibility = VISIBLE
            viewHolder.binding.picturePostBtn.visibility = GONE
        }

        //조회버튼 - 등록된 이미지 조회 화면으로 이동
        viewHolder.binding.pictureBtn.setOnClickListener {
            val intent = Intent(context, DailyWorkWatchPostedPhoto::class.java)
            intent.putExtra("sysDocCode", sysDocCode)
            intent.putExtra("code", consCode)
            intent.putExtra("work_log_cons_code", listPosition.work_log_cons_code)
            intent.putExtra("cons_type_cd", listPosition.cons_type_cd)
            intent.putExtra("cons_date", consDate)
            intent.putExtra("title", levelNameResult)
            intent.putExtra("userId", userId)
            intent.putExtra("data", listPosition)
            context.startActivity(intent)
            (context as Activity).finish()
        }

        //첨부버튼 -> 이미지 첨부(수정) 화면으로 이동
        viewHolder.binding.picturePostBtn.setOnClickListener {
            val intent = Intent(context, WorkEditPhoto::class.java)
            intent.putExtra("sysDocCode", sysDocCode)
            intent.putExtra("code", consCode)
            intent.putExtra("work_log_cons_code", listPosition.work_log_cons_code)
            intent.putExtra("cons_type_cd", listPosition.cons_type_cd)
            intent.putExtra("cons_date", consDate)
            intent.putExtra("title", levelNameResult)
            intent.putExtra("userId", userId)
            intent.putExtra("data", listPosition)
            context.startActivity(intent)
            (context as Activity).finish()
       }


    }
    override fun getItemCount() = dataset.size
}
