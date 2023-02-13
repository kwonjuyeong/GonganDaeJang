package com.example.gonggandaejang.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemConsWorkInfoBinding

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
)
data class ImageInputList(
    val change_name : String,
    val cons_date : String,
    val cons_type_cd : String,
    val cons_type_explain: String,
    val file_index: Int,
    val origin_name: String,
    val path : String,
    val title : String,
    val upload_date: String
)
class ConsWorkInfoAdapter(private val context: Context, private val dataset: List<ConsWorkInfoData>):
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
        viewHolder.binding.insideRecycler.adapter = ConsWorkInfoInsideAdapter(workData)

        for(i in 0 until listPosition.ConsWorkInfoInputListData.size){
            for(j in 0 until listPosition.ConsWorkInfoInputListData[i].imageList.size){
                workInputData = ConsWorkInputList(listPosition.ConsWorkInfoInputListData[i].cons_type_cd, listPosition.ConsWorkInfoInputListData[i].cons_type_explain, listPosition.ConsWorkInfoInputListData[i].cons_type_nm, listPosition.ConsWorkInfoInputListData[i].level1_name, listPosition.ConsWorkInfoInputListData[i].level2_name, listPosition.ConsWorkInfoInputListData[i].level3_name, listPosition.ConsWorkInfoInputListData[i].next_workload, listPosition.ConsWorkInfoInputListData[i].prev_workload, listPosition.ConsWorkInfoInputListData[i].product, listPosition.ConsWorkInfoInputListData[i].quantity, listPosition.ConsWorkInfoInputListData[i].today_workload, listPosition.ConsWorkInfoInputListData[i].total_workload, listPosition.ConsWorkInfoInputListData[i].unit, listPosition.ConsWorkInfoInputListData[i].work_log_cons_code, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv1, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv2, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv3, listPosition.ConsWorkInfoInputListData[i].work_log_cons_lv4, listPosition.ConsWorkInfoInputListData[i].imageList)
                workData.add(workInputData)
            }
        }
        viewHolder.binding.insideRecycler.adapter?.notifyDataSetChanged()
    }
    override fun getItemCount() = dataset.size
}
