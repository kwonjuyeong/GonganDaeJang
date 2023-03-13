package com.gonggan.Adapter

//사진 첨부/삭제 아이템

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemDailyWorkDocPhotoPostBinding
import com.gonggan.objects.DocFileDownLoadDTO
import com.gonggan.objects.loadFile

class ConsWorkInfoInsideFileAdapter(private val dataset: List<ImageInputList>,  private val deleteButton : (data : ImageInputList) -> Unit, private val token : String, private val consCode : String, private val sysDocNum : String):
    RecyclerView.Adapter<ConsWorkInfoInsideFileAdapter.ConsWorkInfoInsideFileViewHolder>() {

    class ConsWorkInfoInsideFileViewHolder(val binding: ItemDailyWorkDocPhotoPostBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsWorkInfoInsideFileViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_daily_work_doc_photo_post, viewGroup, false)
        return ConsWorkInfoInsideFileViewHolder(ItemDailyWorkDocPhotoPostBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: ConsWorkInfoInsideFileViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.title.text = listPosition.title


        loadFile(token ,viewHolder.binding.images , DocFileDownLoadDTO(consCode,sysDocNum,listPosition.file_path,listPosition.origin_name,listPosition.change_name))

        viewHolder.binding.deleteBtn.setOnClickListener {
           deleteButton.invoke(listPosition)
        }

    }
    override fun getItemCount() = dataset.size
}
