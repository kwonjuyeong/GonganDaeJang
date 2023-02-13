package com.example.gonggandaejang.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemWatchGalleryBinding
import com.example.gonggandaejang.objects.DocFileDownLoadDTO
import com.example.gonggandaejang.objects.customDetailGallery
import com.example.gonggandaejang.objects.loadFile

//갤러리 2차 아이템
class GalleryInsideAdapter(private val context: Context, private val dataset: List<GalleryListData>, private val token : String):
    RecyclerView.Adapter<GalleryInsideAdapter.GalleryInsideViewHolder>() {

    class GalleryInsideViewHolder(val binding: ItemWatchGalleryBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): GalleryInsideViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_watch_gallery, viewGroup, false)
        return GalleryInsideViewHolder(ItemWatchGalleryBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: GalleryInsideViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.title.text = listPosition.title

        loadFile(token, viewHolder.binding.imageview, DocFileDownLoadDTO(listPosition.cons_code, "", listPosition.path, listPosition.origin_name, listPosition.change_name))
        viewHolder.binding.detailBtn.setOnClickListener {
            customDetailGallery(context, token, listPosition.cons_code, listPosition.path, listPosition.origin_name, listPosition.change_name, listPosition.upload_date, listPosition.title)
        }

    }

    override fun getItemCount() = dataset.size


}
