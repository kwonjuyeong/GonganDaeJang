package com.gonggan.Adapter

//사진대지 안쪽 사진 데이터

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemWatchGalleryBinding
import com.gonggan.objects.DocFileDownLoadDTO
import com.gonggan.objects.customDetailGallery
import com.gonggan.objects.loadFile

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

        viewHolder.binding.root.setOnClickListener {
            Log.d("ddddddd", listPosition.toString())
        }
        loadFile(token, viewHolder.binding.imageview, DocFileDownLoadDTO(listPosition.cons_code, "", listPosition.path, listPosition.origin_name, listPosition.change_name))

        viewHolder.binding.detailBtn.setOnClickListener {
        customDetailGallery(context, token, GalleryListData(listPosition.co_code, listPosition.cons_code,listPosition.cons_date, listPosition.file_index, listPosition.change_name, listPosition.origin_name, listPosition.path, listPosition.title, listPosition.item_code, listPosition.pc_code, listPosition.pc_name, listPosition.product, listPosition.standard, listPosition.upload_date))
        }
    }

    override fun getItemCount() = dataset.size


}
