package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggandaejang.Adapter.ConsWorkInfoInsideFileAdapter
import com.example.gonggandaejang.Adapter.ImageInputList
import com.example.gonggandaejang.databinding.ActivityWorkEditPhotoBinding


private var ImageInfoData = arrayListOf<ImageInputList>()
private lateinit var ImageInfoInputData : ImageInputList

class WorkEditPhoto : AppCompatActivity() {
    private lateinit var binding: ActivityWorkEditPhotoBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var constCode : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWorkEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        binding.imageRecycler.layoutManager = LinearLayoutManager(this)
        binding.imageRecycler.adapter = ConsWorkInfoInsideFileAdapter(ImageInfoData) { deleteBtn(it) }






    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        constCode = intent.getStringExtra("code")!!
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteBtn(data : ImageInputList){
        //삭제 로직, data 삭제
        ImageInfoData.remove(data)
        binding.imageRecycler.adapter?.notifyDataSetChanged()
    }

}