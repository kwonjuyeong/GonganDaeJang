package com.gonggan.Adapter

//공사일보 인력 2차 구분 아이템

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemConsWorkManpInsideBinding

class ConsManPInfoInsideAdapter(private val dataset: List<ConsManPInputList>):
    RecyclerView.Adapter<ConsManPInfoInsideAdapter.ConsWorkInfoInsideViewHolder>() {

    class ConsWorkInfoInsideViewHolder(val binding: ItemConsWorkManpInsideBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsWorkInfoInsideViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_cons_work_manp_inside, viewGroup, false)
        return ConsWorkInfoInsideViewHolder(ItemConsWorkManpInsideBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: ConsWorkInfoInsideViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.prevManpower.text = listPosition.prev_manpower.toString()
        viewHolder.binding.todayManpower.text = listPosition.today_manpower.toString()
        viewHolder.binding.nextManpower.text = listPosition.next_manpower.toString()
        viewHolder.binding.workLogConsCode.text = listPosition.work_log_cons_code

        viewHolder.binding.totalManpower.text = "${listPosition.prev_manpower + listPosition.today_manpower}"

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
    }
    override fun getItemCount() = dataset.size
}
