package com.gonggan.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemNoCompanyCoHistoryBinding

class CoHistory(
   val co_address : String,
   val co_ceo : String,
   val co_code : String,
   val co_name : String,
   val co_contact : String,
   val tenure_end_date : String,
   val tenure_start_date : String,
   val id : String
)

class CoHistoryAdapter(private val dataset: List<CoHistory>, private val changeList : (data:CoHistory) -> Unit) : RecyclerView.Adapter<CoHistoryAdapter.CoHistoryViewHolder>()
{
    class CoHistoryViewHolder(val binding: ItemNoCompanyCoHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CoHistoryViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_no_company_co_history, viewGroup, false)
        return CoHistoryViewHolder(ItemNoCompanyCoHistoryBinding.bind(view))
    }
    override fun onBindViewHolder(viewHolder: CoHistoryViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.companyName.text = listPosition.co_name
        viewHolder.binding.companyDate.text = "${listPosition.tenure_start_date}~${listPosition.tenure_end_date}"

        viewHolder.binding.coHistoryBtn.setOnClickListener {
            changeList.invoke(listPosition)
        }


    }

    override fun getItemCount() = dataset.size

}