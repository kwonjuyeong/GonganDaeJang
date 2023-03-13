package com.gonggan.source.qa

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gonggan.R
import com.example.gonggan.databinding.ActivityQacommentBinding
import com.gonggan.API.GetQAReply
import com.gonggan.API.PostQAReply
import com.gonggan.Adapter.QACommentAdapter
import com.gonggan.DTO.*
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private var getReply : ReplyQADTO ?= null
private var postReplyD : PostQADTO ?= null

private var commentData = arrayListOf<ReplyQAData>()
private lateinit var commentInputData : ReplyQAData

class QACommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQacommentBinding
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userToken: String
    private lateinit var uuid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQacommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setSupportActionBar(binding.include.mainToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            supportActionBar?.title = "댓글"
        }

        //RecyclerView
        binding.commentParentRecycler.apply {
            layoutManager = LinearLayoutManager(this@QACommentActivity)
            adapter = QACommentAdapter(this@QACommentActivity, commentData, userToken)
        }

        updateParentReply()

        //부모 댓글 등록============================
        binding.postBtn.setOnClickListener {
            postParentReply()
        }
        //확인 버튼 ================================
        binding.bottomBtn.setOnClickListener {
            finish()
        }

    }

    private fun init() {
        sharedPreference = getSharedPreferences("user_auto", MODE_PRIVATE)
        editor = sharedPreference.edit()
        userToken = sharedPreference.getString("token", "").toString()
        uuid = intent.getStringExtra("uuid")!!
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

    private fun updateParentReply(){
        //부모댓글 조회 =========================================================================================================
        val getReplys = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projMessageBoardManage/MessageBoardReply/{post_uuid}/").create(GetQAReply::class.java)

        getReplys.requestGetQaReply(uuid, CodeList.sysCd, userToken, "").enqueue(object :
            Callback<ReplyQADTO> {
            override fun onFailure(call: Call<ReplyQADTO>, t: Throwable) {
                Log.d("retrofit", t.toString())
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ReplyQADTO>, response: Response<ReplyQADTO>) {
                getReply = response.body()

                commentData.clear()

                for(i in 0 until getReply?.value!!.size){
                    commentInputData = ReplyQAData(getReply?.value?.get(i)?.child_count!!.toInt(), getReply?.value?.get(i)?.co_code.toString(), getReply?.value?.get(i)?.content.toString(), getReply?.value?.get(i)?.parent_uuid.toString(),  getReply?.value?.get(i)?.post_uuid.toString(),getReply?.value?.get(i)?.reg_date.toString(),  getReply?.value?.get(i)?.uuid.toString(), getReply?.value?.get(i)?.writer_id.toString(), getReply?.value?.get(i)?.writer_name.toString())
                    commentData.add(commentInputData)
                }
                binding.commentParentRecycler.adapter?.notifyDataSetChanged()
            }
        })
    }

    private fun postParentReply(){
        val content = binding.postEditText.text.toString()
        if(content != ""){
            val postReply = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projMessageBoardManage/MessageBoardReply/{post_uuid}/").create(PostQAReply::class.java)

            postReply.requestPostQaReply(uuid, CodeList.sysCd, userToken, "", ReplyPostRequestDTO(content)).enqueue(object :
                Callback<PostQADTO> {
                override fun onFailure(call: Call<PostQADTO>, t: Throwable) { Log.d("retrofit", t.toString()) }

                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<PostQADTO>, response: Response<PostQADTO>) {
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