package com.gonggan.Adapter

//QA 리스트 조회

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemQaSearchListBinding
import com.gonggan.DTO.QALists
import com.gonggan.objects.convertDateFormat
import com.gonggan.source.qa.QAWatchDoc

class QaAdapter(private val context: Context, private val dataset: List<QALists>, private val consCode : String):
    RecyclerView.Adapter<QaAdapter.QaViewHolder>() {

    class QaViewHolder(val binding: ItemQaSearchListBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): QaViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_qa_search_list, viewGroup, false)
        return QaViewHolder(ItemQaSearchListBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: QaViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.title.text = listPosition.title
        viewHolder.binding.coName.text = listPosition.co_name
        viewHolder.binding.writer.text = listPosition.writer_name
        viewHolder.binding.writeDate.text = convertDateFormat(listPosition.reg_date)

        viewHolder.binding.detailBtn.setOnClickListener {
                val intent = Intent(context, QAWatchDoc::class.java)
                intent.putExtra("code", consCode)
                intent.putExtra("uuid", listPosition.uuid)
                context.startActivity(intent)
                //(context as Activity).finish()
        }
    }

    override fun getItemCount() = dataset.size
}
