package com.gonggan.Adapter

//QA 리스트 조회

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemQaPostAddFileBinding
import java.io.File

data class QAModifyData(
    val file_name : String,
    val file_index : Int,
    val file: File?
)

class QAModifyAdapter(private val dataset: List<QAModifyData>, private val deleteClicked : (data: QAModifyData) -> Unit):
    RecyclerView.Adapter<QAModifyAdapter.QAModifyViewHolder>() {

    class QAModifyViewHolder(val binding: ItemQaPostAddFileBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): QAModifyViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_qa_post_add_file, viewGroup, false)
        return QAModifyViewHolder(ItemQaPostAddFileBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: QAModifyViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.title.text = listPosition.file_name

        //파일 삭제
        viewHolder.binding.deleteBtn.setOnClickListener {
            deleteClicked.invoke(listPosition)
        }

    }

    override fun getItemCount() = dataset.size
}
