package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.ImageData
import com.example.gonggandaejang.Adapter.ConsWorkInfoInsideFileAdapter
import com.example.gonggandaejang.Adapter.ConsWorkInputList
import com.example.gonggandaejang.Adapter.ImageInputList
import com.example.gonggandaejang.databinding.ActivityWorkEditPhotoBinding
import java.io.Serializable


private var ImageInfoData = arrayListOf<ImageInputList>()
private lateinit var ImageInfoInputData : ImageInputList

class WorkEditPhoto : AppCompatActivity() {
    private lateinit var binding: ActivityWorkEditPhotoBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var constCode : String
    private lateinit var sysDocCode : String
    private lateinit var workLogConsCode : String
    private lateinit var imageGetData : Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWorkEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        binding.imageRecycler.layoutManager = LinearLayoutManager(this)
        binding.imageRecycler.adapter = ConsWorkInfoInsideFileAdapter(ImageInfoData) { deleteBtn(it) }


        Log.d("dddddd", imageGetData.toString())



    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        constCode = intent.getStringExtra("code")!!
        sysDocCode = intent.getStringExtra("sysDocCode")!!
        workLogConsCode = intent.getStringExtra("work_log_cons_code")!!
        imageGetData = intent.getSerializableExtra("data") as ConsWorkInputList
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteBtn(data : ImageInputList){
        //삭제 로직, data 삭제
        ImageInfoData.remove(data)
        binding.imageRecycler.adapter?.notifyDataSetChanged()
    }

}