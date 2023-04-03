package com.gonggan.Adapter

//작업량 사진 조회

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemDailyWorkPostInsideBinding
import com.gonggan.objects.DocFileDownLoadDTO
import com.gonggan.objects.loadFile

class DailyWorkPostInsideAdapter(private val dataset: List<DailyImageData>, private val token : String, private val consCode : String, private val sysDocNum : String, private val allNumber : Int):
    RecyclerView.Adapter<DailyWorkPostInsideAdapter.DailyWorkPostInsideViewHolder>() {

    class DailyWorkPostInsideViewHolder(val binding: ItemDailyWorkPostInsideBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DailyWorkPostInsideViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_daily_work_post_inside, viewGroup, false)
        return DailyWorkPostInsideViewHolder(ItemDailyWorkPostInsideBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: DailyWorkPostInsideViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.fileName.text = listPosition.origin_name

       viewHolder.binding.textView12.text = " 파일\n(${listPosition.number + 1}/$allNumber)"
       loadFile(token, viewHolder.binding.imageview , DocFileDownLoadDTO(consCode,sysDocNum,listPosition.file_path,listPosition.origin_name,listPosition.change_name))

    }
    override fun getItemCount() = dataset.size
}
