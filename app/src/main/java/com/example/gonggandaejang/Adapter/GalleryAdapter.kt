package com.example.gonggandaejang.Adapter

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

//갤러리 1차 아이템
private val galleryData = arrayListOf<GalleryListData>()
private lateinit var  galleryInputData : GalleryListData

data class GalleryData(
    val cons_code : String,
    val title : String,
    val path : String,
    val origin_name : String,
    val change_name : String,
    val upload_date : String,
    val GalleryList : ArrayList<GalleryListData>
)
data class GalleryListData(
    val cons_code : String,
    val title : String,
    val path : String,
    val origin_name : String,
    val change_name : String,
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
        viewHolder.binding.date.text = convertDateFormat(listPosition.upload_date)
        galleryData.clear()
        viewHolder.binding.listItemRecycler.layoutManager = LinearLayoutManager(context).also { it.orientation = LinearLayoutManager.HORIZONTAL }
        viewHolder.binding.listItemRecycler.adapter = GalleryInputAdapter(context as Context, galleryData, token)

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
            Log.d("input_data_list_size", listPosition.GalleryList.size.toString())
            for(i in 0 until listPosition.GalleryList.size){
                Log.d("input_data_list", i.toString())

                Log.d("input_data_list", listPosition.GalleryList[i].cons_code)
                Log.d("input_data_list_title", listPosition.GalleryList[i].title)
                Log.d("input_data_list_path", listPosition.GalleryList[i].path)
                Log.d("input_data_list_origin", listPosition.GalleryList[i].origin_name)

                galleryInputData = GalleryListData(listPosition.GalleryList[i].cons_code, listPosition.GalleryList[i].title, listPosition.GalleryList[i].path, listPosition.GalleryList[i].origin_name, listPosition.GalleryList[i].change_name, listPosition.GalleryList[i].upload_date)
                galleryData.add(galleryInputData)
            }
            viewHolder.binding.listItemRecycler.adapter?.notifyDataSetChanged()


    }

    override fun getItemCount() = dataset.size
}
