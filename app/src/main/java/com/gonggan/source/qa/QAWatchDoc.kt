package com.gonggan.source.qa

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityQawatchDocBinding
import com.gonggan.API.GetUserInfoService
import com.gonggan.API.WatchQADoc
import com.gonggan.Adapter.QaWatchDocFileAdapter
import com.gonggan.DTO.QADocFileData
import com.gonggan.DTO.UserInfoDTO
import com.gonggan.DTO.WatchQADocDTO
import com.gonggan.objects.*
import com.gonggan.source.detailhome.RootActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "QAWatchDocument"

private var watchDoc : WatchQADocDTO?= null

private var userInfo: UserInfoDTO? = null

private lateinit var sharedPreference : SharedPreferences
private lateinit var editor : SharedPreferences.Editor
private lateinit var userToken : String
private lateinit var consCode : String
private lateinit var uuid : String

class QAWatchDoc : AppCompatActivity() {

    private var fileData = arrayListOf<QADocFileData>()
    private lateinit var fileInputData: QADocFileData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityQawatchDocBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.apply {
            title = getString(R.string.communication)
        }


        val retroWatchQaList = ApiUtilities.callRetrofit("http://211.107.220.103:${CodeList.portNum}/projMessageBoardManage/MessageBoard/{cons_code}/").create(WatchQADoc::class.java)
        val retrofitInfo = ApiUtilities.callRetrofit("http://211.107.220.103:${CodeList.portNum}/userManage/getMyInfo/").create(GetUserInfoService::class.java)

        binding.filesRecycler.apply {
            layoutManager = LinearLayoutManager(this@QAWatchDoc)
            adapter = QaWatchDocFileAdapter(fileData) { qaDownLoad(it) }
        }

        retroWatchQaList.requestWatchQa(consCode,CodeList.sysCd, userToken ,uuid).enqueue(object : Callback<WatchQADocDTO> {
            override fun onFailure(call: Call<WatchQADocDTO>, t: Throwable) { Log.d("QAWatchDoc_error", t.toString()) }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<WatchQADocDTO>, response: Response<WatchQADocDTO>) {
                watchDoc = response.body()
                    Log.d(TAG, Gson().toJson(watchDoc?.value))
                    retrofitInfo.requestUserInfo(userToken, CodeList.sysCd).enqueue(object :
                        Callback<UserInfoDTO> {
                        override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                        override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                            userInfo = response.body()

                            if(watchDoc?.value?.writer_id.toString() == userInfo?.value?.id.toString()) {
                                binding.deleteBtn.visibility = VISIBLE
                                binding.modifyBtn.visibility = VISIBLE
                            }
                        }
                    })

                    binding.titleText.text = watchDoc?.value?.title.toString()
                    binding.writerText.text = watchDoc?.value?.writer_name.toString()
                    binding.writeDate.text = convertDateFormat(watchDoc?.value?.reg_date)
                    val content = Html.fromHtml(watchDoc?.value?.content.toString()).toString()
                    binding.contentText.text = content

                    if(watchDoc?.value?.files?.size != null){
                        for(i in 0 until watchDoc?.value?.files!!.size){
                            fileInputData = QADocFileData(watchDoc?.value?.files?.get(i)?.chan_name.toString(), watchDoc?.value?.files?.get(i)?.file_index!!.toInt(), watchDoc?.value?.files?.get(i)?.file_path.toString(), watchDoc?.value?.files?.get(i)?.orig_name.toString(), watchDoc?.value?.files?.get(i)?.post_uuid.toString(), watchDoc?.value?.files?.get(i)?.reg_date.toString())
                            fileData.add(fileInputData)
                        }
                    }
                    binding.filesRecycler.adapter?.notifyDataSetChanged()


            }
        })


        binding.modifyBtn.setOnClickListener {
            val intent = Intent(this@QAWatchDoc, QAModify::class.java)
            intent.putExtra("code", consCode)
            intent.putExtra("uuid", uuid)
            startActivity(intent)
            finish()
        }

        binding.deleteBtn.setOnClickListener {
            deleteDocCustom(this@QAWatchDoc, deleteDocData(consCode, userToken, uuid))
        }

        //목록으로
        binding.backBtn.setOnClickListener{
            finish()
        }
    }

    private fun init(){
      sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
      editor = sharedPreference.edit()
      userToken = sharedPreference.getString("token", "").toString()
      uuid = intent.getStringExtra("uuid")!!
      consCode = intent.getStringExtra("code")!!
    }

    private fun qaDownLoad(data : QADocFileData){
        CoroutineScope(Dispatchers.IO).launch{
            docFileDownload(this@QAWatchDoc, userToken, DocFileDownLoadDTO(consCode, "", data.file_path, data.orig_name, data.chan_name))
        }
    }
}