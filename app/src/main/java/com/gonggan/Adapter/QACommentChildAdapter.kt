package com.gonggan.Adapter

//공사일보 인력 1차 구분 아이템

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemCommentChildBinding
import com.gonggan.API.*
import com.gonggan.DTO.*
import com.gonggan.objects.ApiUtilities.callRetrofit
import com.gonggan.objects.CodeList
import com.gonggan.objects.convertDateFormat4
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private var getReply : ReplyQADTO?= null
private var postReplyD : PostQADTO?= null
private var deleteReply : PostQADTO ?= null
private var putReply : ReplyQADTO ?= null
private var userInfo: UserInfoDTO? = null

class QACommentChildAdapter(private val context: Context, private val dataset: List<ReplyQAData>, private val token : String):
    RecyclerView.Adapter<QACommentChildAdapter.QACommentChildViewHolder>() {

    class QACommentChildViewHolder(val binding: ItemCommentChildBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): QACommentChildViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_comment_child, viewGroup, false)
        return QACommentChildViewHolder(ItemCommentChildBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(viewHolder: QACommentChildViewHolder, position: Int) {
        val listPosition = dataset[position]

        val commentData = arrayListOf<ReplyQAData>()
        var commentInputData : ReplyQAData
        //메뉴 열고 닫기 상태
        var replyState = 0
        var writeState = 0
        var modifyState = 0

        //댓글 정보 표시==============================================================================================================================
        viewHolder.binding.writer.text = listPosition.writer_name

        if(listPosition.content != "null"){
            viewHolder.binding.content.text = listPosition.content
            viewHolder.binding.deleteLayout.visibility = VISIBLE
            viewHolder.binding.writer.visibility = VISIBLE
        }else{
            viewHolder.binding.content.text = "작성자에 의해 삭제된 댓글입니다."
            viewHolder.binding.deleteLayout.visibility = GONE
            viewHolder.binding.writer.visibility = GONE
        }

        //대댓글 조회 =================================================================================================================================
        val retrofit = callRetrofit("${CodeList.portNum}/projMessageBoardManage/MessageBoardReply/{post_uuid}/")
        val workReply: GetQAReply = retrofit.create(GetQAReply::class.java)

        val retrofitInfo = callRetrofit("${CodeList.portNum}/userManage/getMyInfo/")
        val getMyInfo: GetUserInfoService = retrofitInfo.create(GetUserInfoService::class.java)

        viewHolder.binding.writeDate.text = convertDateFormat4(listPosition.reg_date)
        //대댓글 확인==========================================================
        viewHolder.binding.replyCount.text = "답글 ${listPosition.child_count} ▲"
        viewHolder.binding.replyCount.setOnClickListener {
            //대댓글 열기
            if(replyState == 0)
            {
                replyState = 1
                viewHolder.binding.childRecycler.visibility = VISIBLE
                viewHolder.binding.childRecycler.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = QACommentChildAdapter(context, commentData, token)
                }

                workReply.requestGetQaReply(listPosition.post_uuid, CodeList.sysCd, token, listPosition.uuid).enqueue(object :
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
                        viewHolder.binding.replyCount.text = "답글 ${listPosition.child_count} ▼"
                        viewHolder.binding.childRecycler.adapter?.notifyDataSetChanged()
                    }
                })
            }
            //대댓글 닫기
            else if(replyState == 1){
                replyState = 0
                commentData.clear()
                viewHolder.binding.replyCount.text = "답글 ${listPosition.child_count} ▲"
                viewHolder.binding.childRecycler.visibility = GONE
            }
        }

        //답글쓰기 버튼==========================================================
        viewHolder.binding.writeBtn.setOnClickListener {
            if(writeState == 0)
            {   writeState = 1
                viewHolder.binding.constraintLayout.visibility = VISIBLE
                viewHolder.binding.writeBtn.text = "취소"
            }
            else if(writeState == 1){
                writeState = 0
                viewHolder.binding.writeBtn.text = "답글쓰기"
                viewHolder.binding.postEditText.setText("")
                viewHolder.binding.constraintLayout.visibility = GONE
            }
        }

        //답글 등록 버튼 =================================================
        viewHolder.binding.postBtn.setOnClickListener {

            val postReply = callRetrofit("${CodeList.portNum}/projMessageBoardManage/MessageBoardReply/{post_uuid}/").create(PostQAReply::class.java)

            val content = viewHolder.binding.postEditText.text.toString()

            postReply.requestPostQaReply(listPosition.post_uuid, CodeList.sysCd, token, listPosition.uuid, ReplyPostRequestDTO(content)).enqueue(object :
                Callback<PostQADTO> {
                override fun onFailure(call: Call<PostQADTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<PostQADTO>, response: Response<PostQADTO>) {
                    postReplyD = response.body()

                    Log.d("reply_",postReplyD?.code.toString())
                    Log.d("reply_",postReplyD?.msg.toString())
                    Log.d("reply_",postReplyD?.value.toString())

                    if(postReplyD?.code == 200){
                        Toast.makeText(context, "등록", Toast.LENGTH_SHORT).show()

                        workReply.requestGetQaReply(listPosition.post_uuid, CodeList.sysCd, token, listPosition.uuid).enqueue(object :
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
                                writeState = 0
                                viewHolder.binding.writeBtn.text = "답글쓰기"
                                viewHolder.binding.postEditText.setText("")
                                viewHolder.binding.childRecycler.visibility = VISIBLE
                                viewHolder.binding.constraintLayout.visibility = GONE
                                listPosition.child_count = listPosition.child_count + 1
                                viewHolder.binding.replyCount.text = "답글 ${listPosition.child_count} ▼"
                                viewHolder.binding.childRecycler.adapter?.notifyDataSetChanged()
                            }
                        })
                    }
                }
            })
        }


        viewHolder.binding.deleteBtn.setOnClickListener {

            getMyInfo.requestUserInfo(token, CodeList.sysCd).enqueue(object :
                Callback<UserInfoDTO> {
                override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                    userInfo = response.body()

                    if(listPosition.writer_id == userInfo?.value?.id){

                        val deleteReplys = callRetrofit("${CodeList.portNum}/projMessageBoardManage/MessageBoardReply/{post_uuid}/").create(DeleteQAReply::class.java)

                        deleteReplys.requestDeleteQaReply(listPosition.post_uuid, CodeList.sysCd, token, listPosition.uuid).enqueue(object :
                            Callback<PostQADTO> {
                            override fun onFailure(call: Call<PostQADTO>, t: Throwable) {
                                Log.d("retrofit", t.toString())
                            }
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onResponse(call: Call<PostQADTO>, response: Response<PostQADTO>) {
                                deleteReply = response.body()

                                Log.d("delete_reply", deleteReply?.code.toString())
                                Log.d("delete_reply", deleteReply?.msg.toString())
                                Log.d("delete_reply", deleteReply?.value.toString())

                                if(deleteReply?.code == 200){

                                    workReply.requestGetQaReply(listPosition.post_uuid, CodeList.sysCd, token, listPosition.uuid).enqueue(object :
                                        Callback<ReplyQADTO> {
                                        override fun onFailure(call: Call<ReplyQADTO>, t: Throwable) {
                                            Log.d("retrofit", t.toString())
                                        }
                                        @SuppressLint("NotifyDataSetChanged")
                                        override fun onResponse(call: Call<ReplyQADTO>, response: Response<ReplyQADTO>) {
                                            getReply = response.body()
                                            viewHolder.binding.deleteLayout.visibility = GONE
                                            viewHolder.binding.writer.visibility = GONE
                                            commentData.clear()
                                            for(i in 0 until getReply?.value!!.size){
                                                commentInputData = ReplyQAData(getReply?.value?.get(i)?.child_count!!.toInt(), getReply?.value?.get(i)?.co_code.toString(), getReply?.value?.get(i)?.content.toString(), getReply?.value?.get(i)?.parent_uuid.toString(),  getReply?.value?.get(i)?.post_uuid.toString(),getReply?.value?.get(i)?.reg_date.toString(),  getReply?.value?.get(i)?.uuid.toString(), getReply?.value?.get(i)?.writer_id.toString(), getReply?.value?.get(i)?.writer_name.toString())
                                                commentData.add(commentInputData)
                                            }
                                            viewHolder.binding.content.text = "작성자에 의해 삭제된 댓글입니다."
                                            viewHolder.binding.childRecycler.adapter?.notifyDataSetChanged()
                                        }
                                    })
                                }
                            }
                        })
                    }else{
                        Toast.makeText(context, "작성자만 댓글을 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
        //댓글 수정 ===============================================================================================================================
        viewHolder.binding.modifyBtn.setOnClickListener {

            getMyInfo.requestUserInfo(token, CodeList.sysCd).enqueue(object :
                Callback<UserInfoDTO> {
                override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                    userInfo = response.body()

                    if(listPosition.writer_id == userInfo?.value?.id){
                        if(modifyState == 0)
                        {
                            modifyState = 1
                            viewHolder.binding.modifyLayout.visibility = VISIBLE
                            viewHolder.binding.modifyEditText.setText(listPosition.content)
                            viewHolder.binding.modifyBtn.text = "취소"
                        }
                        else if(modifyState == 1){
                            modifyState = 0
                            viewHolder.binding.modifyBtn.text = "수정"
                            viewHolder.binding.modifyEditText.setText("")
                            viewHolder.binding.modifyLayout.visibility = GONE
                        }

                    }else{
                        Toast.makeText(context, "작성자만 댓글을 수정할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }

                }
            })
        }


        viewHolder.binding.modifyGoBtn.setOnClickListener {
            val retrofitPut = callRetrofit("${CodeList.portNum}/projMessageBoardManage/MessageBoardReply/{post_uuid}/").create(PutQAReply::class.java)

            val content = viewHolder.binding.modifyEditText.text.toString()

            retrofitPut.requestPutQAReply(listPosition.post_uuid,CodeList.sysCd, token, listPosition.uuid, ReplyPutRequestDTO(content)).enqueue(object :
                Callback<ReplyQADTO> {
                override fun onFailure(call: Call<ReplyQADTO>, t: Throwable) {
                    Log.d("retrofit", t.toString())
                }
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<ReplyQADTO>, response: Response<ReplyQADTO>) {
                    putReply = response.body()

                    if(putReply?.code == 200){

                        workReply.requestGetQaReply(listPosition.post_uuid, CodeList.sysCd, token, listPosition.uuid).enqueue(object :
                            Callback<ReplyQADTO> {
                            override fun onFailure(call: Call<ReplyQADTO>, t: Throwable) { Log.d("retrofit", t.toString()) }
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onResponse(call: Call<ReplyQADTO>, response: Response<ReplyQADTO>) {
                                getReply = response.body()
                                viewHolder.binding.modifyBtn.text = "수정"
                                viewHolder.binding.modifyEditText.setText("")
                                viewHolder.binding.modifyLayout.visibility = GONE
                                Toast.makeText(context, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                                commentData.clear()
                                for(i in 0 until getReply?.value!!.size){
                                    commentInputData = ReplyQAData(getReply?.value?.get(i)?.child_count!!.toInt(), getReply?.value?.get(i)?.co_code.toString(), getReply?.value?.get(i)?.content.toString(), getReply?.value?.get(i)?.parent_uuid.toString(),  getReply?.value?.get(i)?.post_uuid.toString(),getReply?.value?.get(i)?.reg_date.toString(),  getReply?.value?.get(i)?.uuid.toString(), getReply?.value?.get(i)?.writer_id.toString(), getReply?.value?.get(i)?.writer_name.toString())
                                    commentData.add(commentInputData)
                                }
                                viewHolder.binding.childRecycler.adapter?.notifyDataSetChanged()
                            }
                        })
                    }
                }
            })
        }

    }
    override fun getItemCount() = dataset.size

}
