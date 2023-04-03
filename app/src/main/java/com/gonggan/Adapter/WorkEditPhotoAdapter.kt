package com.gonggan.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemWorkEditFileChildBinding
import com.example.gonggan.databinding.ItemWorkEditFileParentBinding
import com.gonggan.objects.customDetailFile
import com.google.gson.Gson
import java.io.File

data class OutSideImageInfo(
    var parentTitle : String,
    val fileList : ArrayList<InsideImageInfoList>
)
data class InsideImageInfoList(
    var file_index: Int,
    val origin_name: String,
    val change_name : String,
    val file_path : String,
    var title : String,
    val file : File?
)

class OutsideAdapter(private val context: Context, private val token : String, private val sysDocNum : String, private val consCode : String, private val outSideData: ArrayList<OutSideImageInfo> , private val deleteBtn : (data: OutSideImageInfo) -> Unit, private val attachBtn : (data: Int) -> Unit) : RecyclerView.Adapter<OutsideAdapter.OutsideViewHolder>() {
    inner class OutsideViewHolder(val binding: ItemWorkEditFileParentBinding) : RecyclerView.ViewHolder(binding.root) {
        val innerAdapter = InsideAdapter(context, token, consCode, sysDocNum, ArrayList())
        var watcher: TextWatcher? = null
        init {
            binding.insideRecycler.apply {
                adapter = innerAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }
        fun bind(outsideItem: OutSideImageInfo) {
            val insideAdapter = InsideAdapter(context, token, consCode, sysDocNum, outsideItem.fileList)
            binding.outsideItemEditText.setText(outsideItem.parentTitle)
            binding.insideRecycler.adapter = insideAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutsideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_work_edit_file_parent, parent, false)
        return OutsideViewHolder(ItemWorkEditFileParentBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: OutsideViewHolder, position: Int) {
        val listPosition = outSideData[position]

        viewHolder.watcher = viewHolder.binding.outsideItemEditText.addTextChangedListener {
            listPosition.parentTitle = viewHolder.binding.outsideItemEditText.text.toString()
            for(i in 0 until listPosition.fileList.size){
                listPosition.fileList[i].title = listPosition.parentTitle
            }
        }

        viewHolder.binding.attachBtn.setOnClickListener {
            attachBtn.invoke(position)
        }

        viewHolder.binding.deleteBtn.setOnClickListener {
            deleteBtn.invoke(listPosition)
        }

        val innerDataList = listPosition.fileList
        viewHolder.innerAdapter.setData(innerDataList)

        viewHolder.bind(listPosition)
    }

    override fun getItemCount(): Int {
        return outSideData.size
    }

    override fun onViewRecycled(holder: OutsideViewHolder) {
        // Remove the TextWatcher when the view is recycled
        holder.binding.outsideItemEditText.removeTextChangedListener(holder.watcher)
    }

    class InsideAdapter(private val context: Context, private val token : String, private val consCode: String, private val sysDocNum: String, private var insideDataList: ArrayList<InsideImageInfoList>) : RecyclerView.Adapter<InsideAdapter.InsideViewHolder>() {
        class InsideViewHolder(val binding: ItemWorkEditFileChildBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(insideItem: InsideImageInfoList) {
                binding.fileName.text = insideItem.origin_name
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsideViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_work_edit_file_child, parent, false)
            return InsideViewHolder(ItemWorkEditFileChildBinding.bind(view))
        }

        override fun onBindViewHolder(viewHolder: InsideViewHolder, position: Int) {
            val insideItem = insideDataList[position]

            viewHolder.binding.root.setOnClickListener {
            customDetailFile(context, token, consCode, InsideImageInfoList(insideItem.file_index, insideItem.origin_name,insideItem.change_name, insideItem.file_path, insideItem.title, insideItem.file))
            }

            //파일 삭제 이벤트
            viewHolder.binding.deleteFileBtn.setOnClickListener {
                deleteBtn(insideItem)
            }

            viewHolder.bind(insideItem)
        }

        //사진 리스트 삭제(Outside)
        @SuppressLint("NotifyDataSetChanged")
        private fun deleteBtn(data : InsideImageInfoList){
            this.insideDataList.remove(data)
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return insideDataList.size
        }

        //error
        @SuppressLint("NotifyDataSetChanged")
        fun setData(dataList: ArrayList<InsideImageInfoList>) {
            this.insideDataList = dataList
            notifyDataSetChanged()
        }

    }
}