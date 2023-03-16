package com.gonggan.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemWorkEditFileChildBinding

class InsideImageAdapter(private val dataset: List<InsideImageInfoList>, private val deleteButton : (data : InsideImageInfoList) -> Unit): RecyclerView.Adapter<InsideImageAdapter.ConsWorkInfoInsideViewHolder>() {

    class ConsWorkInfoInsideViewHolder(val binding: ItemWorkEditFileChildBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsWorkInfoInsideViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_work_edit_file_child, viewGroup, false)
        return ConsWorkInfoInsideViewHolder(ItemWorkEditFileChildBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: ConsWorkInfoInsideViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.fileName.text = listPosition.title

        viewHolder.binding.deleteFileBtn.setOnClickListener {
            deleteButton.invoke(listPosition)
        }

    }

    override fun getItemCount() = dataset.size
}
