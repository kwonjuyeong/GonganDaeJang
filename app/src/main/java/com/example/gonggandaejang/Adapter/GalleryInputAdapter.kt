package com.example.gonggandaejang.Adapter

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemWatchGalleryBinding
import com.example.gonggandaejang.objects.DocFileDownLoadDTO
import com.example.gonggandaejang.objects.customDetailGallery
import com.example.gonggandaejang.objects.loadFile

//갤러리 2차 아이템
class GalleryInputAdapter(private val context: Context, private val dataset: List<GalleryListData>, private val token : String):
    RecyclerView.Adapter<GalleryInputAdapter.GalleryInputViewHolder>() {

    class GalleryInputViewHolder(val binding: ItemWatchGalleryBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): GalleryInputViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_watch_gallery, viewGroup, false)
        return GalleryInputViewHolder(ItemWatchGalleryBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: GalleryInputViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.title.text = listPosition.title

        Log.d("input_datas", listPosition.title)
        Log.d("input_datas", listPosition.path)
        Log.d("input_datas", listPosition.origin_name)

        viewHolder.binding.root.setOnClickListener {
            Log.d("input_data_clicked", position.toString())
            Log.d("input_data_clicked", listPosition.title)
            Log.d("input_data_clicked", listPosition.path)
            Log.d("input_data_clicked", listPosition.origin_name)
        }

        loadFile(token, viewHolder.binding.imageview, DocFileDownLoadDTO(listPosition.cons_code, "", listPosition.path, listPosition.origin_name, listPosition.change_name))
        viewHolder.binding.detailBtn.setOnClickListener {
            customDetailGallery(context, token, listPosition.cons_code, listPosition.path, listPosition.origin_name, listPosition.change_name, listPosition.upload_date, listPosition.title)
        }

    }

    override fun getItemCount() = dataset.size


}
