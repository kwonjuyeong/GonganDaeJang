package com.example.gonggandaejang.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemConsWorkInfoBinding
import com.example.gonggandaejang.databinding.ItemConsWorkInfoInsideBinding
import java.io.File
import java.text.DecimalFormat

class ConsWorkInfoInsideAdapter(private val dataset: List<ConsWorkInputList>):
    RecyclerView.Adapter<ConsWorkInfoInsideAdapter.ConsWorkInfoInsideViewHolder>() {

    class ConsWorkInfoInsideViewHolder(val binding: ItemConsWorkInfoInsideBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsWorkInfoInsideViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_cons_work_info_inside, viewGroup, false)
        return ConsWorkInfoInsideViewHolder(ItemConsWorkInfoInsideBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: ConsWorkInfoInsideViewHolder, position: Int) {
        val listPosition = dataset[position]

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

        viewHolder.binding.pictureBtn.setOnClickListener {
            for(i in 0 until listPosition.imageList.size){
                Log.d("clicked_image", listPosition.imageList[i].title)
            }
        }



    }

    override fun getItemCount() = dataset.size
}
