package com.example.gonggandaejang.Adapter

//사진대지 타임라인 바깥 리스트

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.allscapeservice.a22allscape_app.objects.convertDateFormat
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemWatchGalleryOutsideBinding

data class GalleryData(
    val upload_date : String,
    val GalleryList : ArrayList<GalleryListData>
)
data class GalleryListData(
    val co_code : String,
    val cons_code : String,
    val cons_date : String,
    val file_index : Int,
    val change_name : String,
    val origin_name : String,
    val path : String,
    val title : String,
    val item_code : String,
    val pc_code : String,
    val pc_name : String,
    val product : String,
    val standard : String,
    val upload_date : String
)

class GalleryAdapter(private val context: Context?, private val dataset: List<GalleryData>, private val token : String):
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(val binding: ItemWatchGalleryOutsideBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_watch_gallery_outside, viewGroup, false)
        return GalleryViewHolder(ItemWatchGalleryOutsideBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: GalleryViewHolder, position: Int) {
        val listPosition = dataset[position]

        //갤러리 2차 아이템
        val galleryData = arrayListOf<GalleryListData>()
        var galleryInputData : GalleryListData

        viewHolder.binding.date.text = convertDateFormat(listPosition.upload_date)
        viewHolder.binding.listItemRecycler.layoutManager = LinearLayoutManager(context).also { it.orientation = LinearLayoutManager.HORIZONTAL }
        viewHolder.binding.listItemRecycler.adapter = GalleryInsideAdapter(context as Context, galleryData, token)


        //item 하나씩 보여주기
        val snapHelper: SnapHelper = PagerSnapHelper()
        if ( viewHolder.binding.listItemRecycler.onFlingListener == null) snapHelper.attachToRecyclerView(  viewHolder.binding.listItemRecycler)

        viewHolder.binding.listItemRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        for(i in 0 until listPosition.GalleryList.size){
           galleryInputData = GalleryListData(listPosition.GalleryList[i].co_code,listPosition.GalleryList[i].cons_code, listPosition.GalleryList[i].cons_date,listPosition.GalleryList[i].file_index,listPosition.GalleryList[i].change_name,
               listPosition.GalleryList[i].origin_name,listPosition.GalleryList[i].path,listPosition.GalleryList[i].title,listPosition.GalleryList[i].item_code,listPosition.GalleryList[i].pc_code,listPosition.GalleryList[i].pc_name,
               listPosition.GalleryList[i].product,listPosition.GalleryList[i].standard,listPosition.GalleryList[i].upload_date)
            galleryData.add(galleryInputData)
        }

    }

    override fun getItemCount() = dataset.size
}
