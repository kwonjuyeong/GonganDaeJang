package com.gonggan.Adapter

//사진 첨부/삭제 아이템

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemDailyWorkPostBinding
import com.google.gson.Gson

data class ImageOutData(
    val title : String,
    val GalleryList : ArrayList<DailyImageData>
)
data class DailyImageData(
    val change_name : String,
    val cons_date : String,
    val cons_type_cd : String,
    val cons_type_nm: String,
    val cons_type_explain: String,
    val file_index: Int,
    val origin_name: String,
    val file_path : String,
    val title : String,
    val upload_date: String,
    val number : Int
)

class DailyWorkPostAdapter(private val dataset: List<ImageOutData>, private val token : String, private val consCode : String, private val sysDocNum : String):
    RecyclerView.Adapter<DailyWorkPostAdapter.DailyWorkPostViewHolder>() {

    class DailyWorkPostViewHolder(val binding: ItemDailyWorkPostBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DailyWorkPostViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_daily_work_post, viewGroup, false)
        return DailyWorkPostViewHolder(ItemDailyWorkPostBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: DailyWorkPostViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.title.text = listPosition.title

        val dailyPostData = arrayListOf<DailyImageData>()
        var dailyPostInputData : DailyImageData

        val snapHelper: SnapHelper = PagerSnapHelper()
        viewHolder.binding.imagesRecycler.apply {
            layoutManager = LinearLayoutManager(context).also { it.orientation = LinearLayoutManager.HORIZONTAL }
            adapter = DailyWorkPostInsideAdapter(dailyPostData, token, consCode, sysDocNum, listPosition.GalleryList.size)
            if ( onFlingListener == null) snapHelper.attachToRecyclerView(viewHolder.binding.imagesRecycler)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }
                override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
        }

        for(i in 0 until listPosition.GalleryList.size){
            dailyPostInputData = DailyImageData(listPosition.GalleryList[i].change_name,listPosition.GalleryList[i].cons_date, listPosition.GalleryList[i].cons_type_cd, listPosition.GalleryList[i].cons_type_nm, listPosition.GalleryList[i].cons_type_explain, listPosition.GalleryList[i].file_index , listPosition.GalleryList[i].origin_name, listPosition.GalleryList[i].file_path, listPosition.GalleryList[i].title,  listPosition.GalleryList[i].upload_date, i)
            dailyPostData.add(dailyPostInputData)
        }

    }
    override fun getItemCount() = dataset.size
}
