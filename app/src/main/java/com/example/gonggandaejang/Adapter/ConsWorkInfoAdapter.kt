package com.example.gonggandaejang.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemConsWorkInfoBinding
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConsWorkInfoData(
    val cons_type_nm : String,
    val ConsWorkInfoInputListData : ArrayList<ConsWorkInputList>
)
data class ConsWorkInputList(
    val cons_type_cd : String,
    val cons_type_explain : String,
    val cons_type_nm : String,
    var level1_name : String,
    var level2_name : String,
    var level3_name : String,
    val next_workload : Int,
    val prev_workload : Int,
    var product : String,
    val quantity : Float,
    val today_workload : Int,
    val total_workload : Int,
    val unit : String,
    val work_log_cons_code : String,
    val work_log_cons_lv1 : Int,
    val work_log_cons_lv2 : Int,
    val work_log_cons_lv3 : Int,
    val work_log_cons_lv4 : Int,
    val imageList : ArrayList<ImageInputList>
) : Serializable
data class ImageInputList(
    val change_name : String,
    val cons_date : String,
    val cons_type_cd : String,
    val cons_type_nm: String,
    val cons_type_explain: String,
    val file_index: Int,
    val origin_name: String,
    val path : String,
    val title : String,
    val upload_date: String
) : Serializable
class ConsWorkInfoAdapter(private val context: Context, private val dataset: List<ConsWorkInfoData>, private val sysDocCode : String, private val consCode : String):
    RecyclerView.Adapter<ConsWorkInfoAdapter.ConsWorkInfoViewHolder>() {

    class ConsWorkInfoViewHolder(val binding: ItemConsWorkInfoBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsWorkInfoViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_cons_work_info, viewGroup, false)
        return ConsWorkInfoViewHolder(ItemConsWorkInfoBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: ConsWorkInfoViewHolder, position: Int) {
        val listPosition = dataset[position]

        val workData = arrayListOf<ConsWorkInputList>()
        var workInputData : ConsWorkInputList

        viewHolder.binding.consTypeNm.text = listPosition.cons_type_nm

        viewHolder.binding.insideRecycler.layoutManager = LinearLayoutManager(context)
        viewHolder.binding.insideRecycler.adapter = ConsWorkInfoInsideAdapter(context, workData, sysDocCode, consCode)

        val imageData = arrayListOf<ImageInputList>()
        var imageInputData : ImageInputList

        for(i in 0 until listPosition.ConsWorkInfoInputListData.size){
                imageData.clear()
                for(k in 0 until listPosition.ConsWorkInfoInputListData[i].imageList.size){
                    imageInputData = ImageInputList(listPosition.ConsWorkInfoInputListData[i].imageList[k].change_name, listPosition.ConsWorkInfoInputListData[i].imageList[k].cons_date, listPosition.ConsWorkInfoInputListData[i].imageList[k].cons_type_cd,listPosition.ConsWorkInfoInputListData[i].imageList[k].cons_type_nm,
                    listPosition.ConsWorkInfoInputListData[i].imageList[k].cons_type_explain,listPosition.ConsWorkInfoInputListData[i].imageList[k].file_index,listPosition.ConsWorkInfoInputListData[i].imageList[k].origin_name,listPosition.ConsWorkInfoInputListData[i].imageList[k].path,listPosition.ConsWorkInfoInputListData[i].imageList[k].title,
                    listPosition.ConsWorkInfoInputListData[i].imageList[k].upload_date)
                    imageData.add(imageInputData)
                }
                workInputData = ConsWorkInputList(listPosition.ConsWorkInfoInputListData[i].cons_type_cd, listPosition.ConsWorkInfoInputListData[i].cons_type_explain, listPosition.ConsWorkInfoInputListData[i].cons_type_nm, listPosition.ConsWorkInfoInputListData[i].level1_name, listPosition.ConsWorkInfoInputListData[i].level2_name, listPosition.ConsWorkInfoInputListData[i].level3_name, listPosition.ConsWorkInfoInputListData[i].next_workload, listPosition.ConsWorkInfoInputListData[i].prev_workload, listPosition.ConsWorkInfoInputListData[i].product, listPosition.ConsWorkInfoInputListData[i].quantity, listPosition.ConsWorkInfoInputListData[i].today_workload, listPosition.ConsWorkInfoInputListData[i].total_workload, listPosition.ConsWorkInfoInputListData[i].unit, listPosition.ConsWorkInfoInputListData[i].work_log_cons_code, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv1, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv2, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv3, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv4, imageData)
                workData.add(workInputData)
        }
        viewHolder.binding.insideRecycler.adapter?.notifyDataSetChanged()
    }
    override fun getItemCount() = dataset.size
}
