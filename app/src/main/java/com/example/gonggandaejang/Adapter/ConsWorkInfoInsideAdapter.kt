package com.example.gonggandaejang.Adapter

//작업량 2차 구분 아이템

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.WorkEditPhoto
import com.example.gonggandaejang.databinding.ItemConsWorkInfoInsideBinding
import java.text.DecimalFormat

class ConsWorkInfoInsideAdapter(private val context: Context, private val dataset: List<ConsWorkInputList>, private val sysDocCode : String, private val consCode : String):
    RecyclerView.Adapter<ConsWorkInfoInsideAdapter.ConsWorkInfoInsideViewHolder>() {

    class ConsWorkInfoInsideViewHolder(val binding: ItemConsWorkInfoInsideBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsWorkInfoInsideViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_cons_work_info_inside, viewGroup, false)
        return ConsWorkInfoInsideViewHolder(ItemConsWorkInfoInsideBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: ConsWorkInfoInsideViewHolder, position: Int) {
        val listPosition = dataset[position]

        //작업량 inside data================================================================================================================================
        viewHolder.binding.totalWorkload.text = listPosition.total_workload.toString()
        viewHolder.binding.todayWorkload.text = listPosition.today_workload.toString()
        viewHolder.binding.nextWorkload.text = listPosition.next_workload.toString()
        viewHolder.binding.prevWorkload.text = listPosition.prev_workload.toString()
        viewHolder.binding.explain.text = listPosition.cons_type_explain
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
        val levelNameResult = "(${listPosition.level1_name}${listPosition.level2_name}${listPosition.level3_name}${listPosition.product})"
        viewHolder.binding.levelName.text = levelNameResult

        //================================================================================================================================================
       viewHolder.binding.pictureBtn.setOnClickListener {
           //조회버튼 클릭
            val intent = Intent(context, WorkEditPhoto::class.java)
            intent.putExtra("sysDocCode", sysDocCode)
            intent.putExtra("code", consCode)
            intent.putExtra("work_log_cons_code", listPosition.work_log_cons_code)
            intent.putExtra("data", listPosition)
            context.startActivity(intent)
       }



    }

    override fun getItemCount() = dataset.size
}
