package com.gonggan.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemWorkEditFileParentBinding

data class OutSideImageInfo(
    val title : String,
    val insideList : ArrayList<InsideImageInfoList>
)
data class InsideImageInfoList(
    val file_index: Int,
    val origin_name: String,
    val file_path : String,
    val title : String
)

private val imageInsideData = arrayListOf<InsideImageInfoList>()
private lateinit var imageInsideInputData : InsideImageInfoList

class OutsideImageAdapter(private val context: Context, private val dataset: List<OutSideImageInfo>, private val attachBtn : (data : OutSideImageInfo) -> Unit, private val deleteArrayBtn : (data : OutSideImageInfo) -> Unit):
    RecyclerView.Adapter<OutsideImageAdapter.OutsideImageViewHolder>() {

    class OutsideImageViewHolder(val binding: ItemWorkEditFileParentBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): OutsideImageViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_work_edit_file_parent, viewGroup, false)

        return OutsideImageViewHolder(ItemWorkEditFileParentBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: OutsideImageViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.outsideItemTitle.setText(listPosition.title)

        viewHolder.binding.insideRecycler.layoutManager = LinearLayoutManager(context)
        viewHolder.binding.insideRecycler.adapter = InsideImageAdapter(imageInsideData) { deleteFilePosition(it) }

        imageInsideData.clear()

        viewHolder.binding.attachBtn.setOnClickListener {
            attachBtn.invoke(listPosition)
        }
        viewHolder.binding.deleteBtn.setOnClickListener {
            deleteArrayBtn.invoke(listPosition)
            viewHolder.binding.insideRecycler.adapter?.notifyDataSetChanged()
        }

        for(i in 0 until listPosition.insideList.size){
            imageInsideInputData = InsideImageInfoList(listPosition.insideList[i].file_index, listPosition.insideList[i].origin_name, listPosition.insideList[i].file_path, viewHolder.binding.outsideItemTitle.text.toString())
            imageInsideData.add(imageInsideInputData)
        }
        viewHolder.binding.insideRecycler.adapter?.notifyDataSetChanged()
    }
    override fun getItemCount() = dataset.size

    companion object{
        fun deleteFilePosition(data : InsideImageInfoList){
            imageInsideData.remove(data)
        }
    }
}
