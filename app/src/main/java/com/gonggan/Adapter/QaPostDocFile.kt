package com.gonggan.Adapter

//QA 리스트 조회

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemQaPostAddFileBinding
import com.gonggan.DTO.QADocFileData
import java.io.File

data class QAPostData(
    val file_name : String,
    val file : File?
)

class QaPostDocFileAdapter(private val dataset: List<QAPostData>, private val deleteClicked : (data: QAPostData) -> Unit):
    RecyclerView.Adapter<QaPostDocFileAdapter.QaPostDocFileViewHolder>() {

    class QaPostDocFileViewHolder(val binding: ItemQaPostAddFileBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): QaPostDocFileViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_qa_post_add_file, viewGroup, false)
        return QaPostDocFileViewHolder(ItemQaPostAddFileBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: QaPostDocFileViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.title.text = listPosition.file_name

        //파일 삭제
        viewHolder.binding.deleteBtn.setOnClickListener {
            deleteClicked.invoke(listPosition)
        }

    }

    override fun getItemCount() = dataset.size
}
