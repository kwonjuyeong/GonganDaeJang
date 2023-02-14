package com.example.gonggandaejang.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggandaejang.R
import com.example.gonggandaejang.databinding.ItemConsManpInfoBinding

data class ConsManPInfoData(
    val cons_type_nm : String,
    val ConsManPInfoInputListData : ArrayList<ConsManPInputList>
)
data class ConsManPInputList(
    val cons_type_cd : String,
    val cons_type_explain : String,
    val cons_type_nm : String,
    var level1_name : String,
    var level2_name : String,
    var level3_name : String,
    val next_manpower : Int,
    val prev_manpower : Int,
    var product : String,
    val today_manpower : Int,
    val work_log_cons_code : String,
    val work_log_cons_lv1 : Int,
    val work_log_cons_lv2 : Int,
    val work_log_cons_lv3 : Int,
    val work_log_cons_lv4 : Int
)

class ConsManPInfoAdapter(private val context: Context, private val dataset: List<ConsManPInfoData>):
    RecyclerView.Adapter<ConsManPInfoAdapter.ConsManPInfoViewHolder>() {

    class ConsManPInfoViewHolder(val binding: ItemConsManpInfoBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConsManPInfoViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_cons_manp_info, viewGroup, false)
        return ConsManPInfoViewHolder(ItemConsManpInfoBinding.bind(view))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: ConsManPInfoViewHolder, position: Int) {
        val listPosition = dataset[position]
        val manPData = arrayListOf<ConsManPInputList>()
        var manPInputData : ConsManPInputList

        viewHolder.binding.consTypeNm.text = listPosition.cons_type_nm

        viewHolder.binding.insideRecycler.layoutManager = LinearLayoutManager(context)
        viewHolder.binding.insideRecycler.adapter = ConsManPInfoInsideAdapter(manPData)


        for(i in 0 until listPosition.ConsManPInfoInputListData.size){
            manPInputData = ConsManPInputList(listPosition.ConsManPInfoInputListData[i].cons_type_cd, listPosition.ConsManPInfoInputListData[i].cons_type_explain, listPosition.ConsManPInfoInputListData[i].cons_type_nm, listPosition.ConsManPInfoInputListData[i].level1_name, listPosition.ConsManPInfoInputListData[i].level2_name,listPosition.ConsManPInfoInputListData[i].level3_name,listPosition.ConsManPInfoInputListData[i].next_manpower,listPosition.ConsManPInfoInputListData[i].prev_manpower,listPosition.ConsManPInfoInputListData[i].product,listPosition.ConsManPInfoInputListData[i].today_manpower, listPosition.ConsManPInfoInputListData[i].work_log_cons_code, listPosition.ConsManPInfoInputListData[i].work_log_cons_lv1,listPosition.ConsManPInfoInputListData[i].work_log_cons_lv2,listPosition.ConsManPInfoInputListData[i].work_log_cons_lv3,listPosition.ConsManPInfoInputListData[i].work_log_cons_lv4)
            manPData.add(manPInputData)
        }
        viewHolder.binding.insideRecycler.adapter?.notifyDataSetChanged()

    }
    override fun getItemCount() = dataset.size
}
