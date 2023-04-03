package com.gonggan.Adapter

//QA 리스트 조회

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemQaFileListBinding
import com.example.gonggan.databinding.ItemQaSearchListBinding
import com.gonggan.DTO.QADocFileData
import com.gonggan.DTO.QALists
import com.gonggan.objects.convertDateFormat
import com.gonggan.objects.docFileDownload
import com.gonggan.source.qa.QAWatchDoc

class QaWatchDocFileAdapter(private val dataset: List<QADocFileData>, private val downLoadClicked : (data: QADocFileData) -> Unit):
    RecyclerView.Adapter<QaWatchDocFileAdapter.QaWatchDocFileViewHolder>() {

    class QaWatchDocFileViewHolder(val binding: ItemQaFileListBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): QaWatchDocFileViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_qa_file_list, viewGroup, false)
        return QaWatchDocFileViewHolder(ItemQaFileListBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: QaWatchDocFileViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.title.text = listPosition.orig_name

        //파일 다운로드
        viewHolder.binding.downloadBtn.setOnClickListener {
          downLoadClicked.invoke(listPosition)
        }
    }

    override fun getItemCount() = dataset.size
}
