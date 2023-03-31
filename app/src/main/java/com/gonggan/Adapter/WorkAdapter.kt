package com.gonggan.Adapter

//공사일보 리스트 조회

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemWatchWorkListsBinding
import com.gonggan.objects.convertDateFormat
import com.gonggan.source.dailywork.DailyWorkDocument

data class WorkData(
    val cons_date : String,
    val id : String,
    val work_title : String,
    val write_date : String,
    val sys_doc_num : String
)

class WorkAdapter(private val context: Context, private val dataset: List<WorkData>, private val cons_code : String):
    RecyclerView.Adapter<WorkAdapter.WorkViewHolder>() {

    class WorkViewHolder(val binding: ItemWatchWorkListsBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): WorkViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_watch_work_lists, viewGroup, false)
        return WorkViewHolder(ItemWatchWorkListsBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: WorkViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.title.text = listPosition.work_title
        viewHolder.binding.workDate.text = convertDateFormat(listPosition.cons_date)
        viewHolder.binding.writeDate.text = convertDateFormat(listPosition.write_date)
        viewHolder.binding.writer.text = listPosition.id

        viewHolder.binding.detailBtn.setOnClickListener {
            //상세정보보기 페이지
            val intent = Intent(context, DailyWorkDocument::class.java)
            intent.putExtra("code", cons_code)
            intent.putExtra("sysDocNum", listPosition.sys_doc_num)
            intent.putExtra("userId", listPosition.id)
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = dataset.size
}
