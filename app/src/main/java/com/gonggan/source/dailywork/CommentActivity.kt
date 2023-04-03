package com.gonggan.source.dailywork

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityCommentBinding
import com.gonggan.DTO.PostGalleryDTO
import com.gonggan.DTO.ReplyDTO
import com.gonggan.API.GetReply
import com.gonggan.API.PostReply
import com.gonggan.Adapter.CommentAdapter
import com.gonggan.Adapter.CommentData
import com.gonggan.DTO.ReplyPostRequestDTO
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "CommentActivity"

private var getReply : ReplyDTO ?= null
private var postReplyD : PostGalleryDTO ?= null
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

        binding.commentParentRecycler.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity)
            adapter = CommentAdapter(this@CommentActivity, commentData,{updateParentReply()}, sysDocNum, userToken)
        }

        //이미 등록되어있는 댓글 조회
        updateParentReply()
        //부모 댓글 작성
        binding.postBtn.setOnClickListener {
            postParentReply()
        }
        //종료
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

    //댓글 업데이트
    private fun updateParentReply(){
        val workReply = callRetrofit("${CodeList.portNum}/projWorkReplyManage/WorkReply/{sys_doc_num}/").create(GetReply::class.java)
        workReply.requestGetReply(sysDocNum, "", CodeList.sysCd, userToken).enqueue(object :
            Callback<ReplyDTO> {
            override fun onFailure(call: Call<ReplyDTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ReplyDTO>, response: Response<ReplyDTO>) {
                getReply = response.body()

                commentData.clear()
                for(i in 0 until getReply?.value!!.size){
                    commentInputData = CommentData(getReply?.value?.get(i)?.child_count!!.toInt(), getReply?.value?.get(i)?.content.toString(), getReply?.value?.get(i)?.parent_uuid.toString(), getReply?.value?.get(i)?.reg_date.toString(), getReply?.value?.get(i)?.sys_doc_num.toString(), getReply?.value?.get(i)?.uuid.toString(), getReply?.value?.get(i)?.writer_id.toString(), getReply?.value?.get(i)?.writer_name.toString())
                    commentData.add(commentInputData)
                }
                binding.commentParentRecycler.adapter?.notifyDataSetChanged()
            }
        })
    }

    //댓글 등록
    private fun postParentReply(){
        val content = binding.postEditText.text.toString()
        if(content != ""){
            val postReply = callRetrofit("${CodeList.portNum}/projWorkReplyManage/WorkReply/{sys_doc_num}/").create(PostReply::class.java)
            postReply.requestPostReply(sysDocNum, "", CodeList.sysCd, userToken, ReplyPostRequestDTO(content)).enqueue(object :
                Callback<PostGalleryDTO> {
                override fun onFailure(call: Call<PostGalleryDTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<PostGalleryDTO>, response: Response<PostGalleryDTO>) {
                    postReplyD = response.body()
                    if(postReplyD?.code == 200){
                        binding.postEditText.setText("")
                        updateParentReply()
                    }
                }
            })
        }
    }


}