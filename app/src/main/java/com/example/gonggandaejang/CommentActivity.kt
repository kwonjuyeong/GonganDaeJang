package com.example.gonggandaejang

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.allscapeservice.a22allscape_app.DTO.ReplyDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.example.gonggandaejang.API.GetReply
import com.example.gonggandaejang.Adapter.CommentAdapter
import com.example.gonggandaejang.Adapter.CommentData
import com.example.gonggandaejang.databinding.ActivityCommentBinding
import com.example.gonggandaejang.objects.CodeList
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private var getReply : ReplyDTO ?= null

private var commentData = arrayListOf<CommentData>()
private lateinit var commentInputData : CommentData


class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var sysDocNum : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        supportActionBar?.title = "댓글"

        //부모댓글 조회 =========================================================================================================
        //작업량
        binding.commentParentRecycler.layoutManager = LinearLayoutManager(this)
        binding.commentParentRecycler.adapter = CommentAdapter(this, commentData, {writeReply(it)},sysDocNum,userToken)

        val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkReplyManage/WorkReply/{sys_doc_num}/")
        val workReply: GetReply = retrofit.create(GetReply::class.java)

        workReply.requestGetReply(sysDocNum, "", CodeList.sysCd, userToken).enqueue(object :
            Callback<ReplyDTO> {
            override fun onFailure(call: Call<ReplyDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ReplyDTO>, response: Response<ReplyDTO>) {
                getReply = response.body()

                Log.d("replydata", getReply?.code.toString())
                Log.d("replydata", getReply?.msg.toString())
                Log.d("replydata", Gson().toJson(getReply?.value))
                commentData.clear()
                for(i in 0 until getReply?.value!!.size){
                    commentInputData = CommentData(getReply?.value?.get(i)?.child_count!!.toInt(), getReply?.value?.get(i)?.content.toString(),
                        getReply?.value?.get(i)?.parent_uuid.toString(), getReply?.value?.get(i)?.reg_date.toString(), getReply?.value?.get(i)?.sys_doc_num.toString(),
                        getReply?.value?.get(i)?.uuid.toString(), getReply?.value?.get(i)?.writer_id.toString(), getReply?.value?.get(i)?.writer_name.toString())
                    commentData.add(commentInputData)
                }
                binding.commentParentRecycler.adapter?.notifyDataSetChanged()
            }
        })
        //=========================================================================================================

        binding.bottomBtn.setOnClickListener {
            finish()
        }
    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        sysDocNum = intent.getStringExtra("sysDocNum")!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    //댓글 작성
    private fun writeReply(data : CommentData){


    }
}