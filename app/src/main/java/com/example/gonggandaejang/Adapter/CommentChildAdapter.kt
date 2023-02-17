package com.example.gonggandaejang.Adapter

//공사일보 인력 1차 구분 아이템

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allscapeservice.a22allscape_app.DTO.ReplyDTO
import com.allscapeservice.a22allscape_app.objects.callRetrofit
import com.allscapeservice.a22allscape_app.objects.convertDateFormat4
import com.example.gonggandaejang.API.GetReply
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemCommentChildBinding
import com.example.gonggandaejang.databinding.ItemCommentParentsBinding
import com.example.gonggandaejang.objects.CodeList
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private var getReply : ReplyDTO ?= null

class CommentChildAdapter(private val context: Context, private val dataset: List<CommentData>,  private val writeCommentBtn : (data : CommentData) -> Unit, private val sysDocNum : String, private val token : String):
    RecyclerView.Adapter<CommentChildAdapter.CommentChildViewHolder>() {

    class CommentChildViewHolder(val binding: ItemCommentChildBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CommentChildViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_comment_child, viewGroup, false)
        return CommentChildViewHolder(ItemCommentChildBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(viewHolder: CommentChildViewHolder, position: Int) {
        val listPosition = dataset[position]
        var replyState = 0

        viewHolder.binding.writer.text = listPosition.writer_name
        viewHolder.binding.content.text = listPosition.content
        viewHolder.binding.writeDate.text = convertDateFormat4(listPosition.reg_date)

        //대댓글 확인==========================================================
        viewHolder.binding.replyCount.text = "답글 ${listPosition.child_count}"
        viewHolder.binding.replyCount.setOnClickListener {
            //대댓글 열기
            if(replyState == 0)
            {
                replyState = 1
                viewHolder.binding.childRecycler.visibility = VISIBLE

                val commentData = arrayListOf<CommentData>()
                var commentInputData : CommentData

                viewHolder.binding.childRecycler.layoutManager = LinearLayoutManager(context)
                viewHolder.binding.childRecycler.adapter = CommentChildAdapter(context, commentData, {writeReply(it)},sysDocNum, token)

                val retrofit = callRetrofit("http://211.107.220.103:${CodeList.portNum}/projWorkReplyManage/WorkReply/{sys_doc_num}/")
                val workReply: GetReply = retrofit.create(GetReply::class.java)

                workReply.requestGetReply(sysDocNum, listPosition.uuid, CodeList.sysCd, token).enqueue(object :
                    Callback<ReplyDTO> {
                    override fun onFailure(call: Call<ReplyDTO>, t: Throwable) {
                        Log.d("retrofit", t.toString())
                    }
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(call: Call<ReplyDTO>, response: Response<ReplyDTO>) {
                        getReply = response.body()

                        for(i in 0 until getReply?.value!!.size){
                            commentInputData = CommentData(
                                getReply?.value?.get(i)?.child_count!!.toInt(), getReply?.value?.get(i)?.content.toString(),
                                getReply?.value?.get(i)?.parent_uuid.toString(), getReply?.value?.get(i)?.reg_date.toString(), getReply?.value?.get(i)?.sys_doc_num.toString(),
                                getReply?.value?.get(i)?.uuid.toString(), getReply?.value?.get(i)?.writer_id.toString(), getReply?.value?.get(i)?.writer_name.toString())
                            commentData.add(commentInputData)
                        }
                        viewHolder.binding.childRecycler.adapter?.notifyDataSetChanged()
                    }
                })
            }
            //대댓글 닫기
            else if(replyState == 1){
                replyState = 0
                viewHolder.binding.childRecycler.visibility = GONE
            }
        }

        //댓글 작성==========================================================
        viewHolder.binding.writeBtn.setOnClickListener {
            writeCommentBtn.invoke(listPosition)
        }

    }
    override fun getItemCount() = dataset.size

    //댓글 작성
    private fun writeReply(data : CommentData){


    }
}
