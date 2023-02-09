package com.example.gonggandaejang.Adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemPhotoPostBinding
import com.example.gonggandaejang.databinding.ItemWatchGalleryBinding
import java.io.File

data class PostGalleryData(
    val file : File,
    val bitmap: Bitmap,
    val info : PhotoInfo
)
data class PhotoInfo(
    val orig_name: String,
    val title: String
)

//
class GalleryPostAdapter(private val dataset: List<PostGalleryData>, private val deleteButton : (data : PostGalleryData) -> Unit):
    RecyclerView.Adapter<GalleryPostAdapter.GalleryPostViewHolder>() {

    class GalleryPostViewHolder(val binding: ItemPhotoPostBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): GalleryPostViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_photo_post, viewGroup, false)
        return GalleryPostViewHolder(ItemPhotoPostBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: GalleryPostViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.title.text = listPosition.info.title
        viewHolder.binding.originName.text = listPosition.info.orig_name
        viewHolder.binding.imageview.setImageBitmap(listPosition.bitmap)
        //사진정보 삭제
        viewHolder.binding.deleteBtn.setOnClickListener {
            deleteButton.invoke(listPosition)
        }

    }

    override fun getItemCount() = dataset.size
}
