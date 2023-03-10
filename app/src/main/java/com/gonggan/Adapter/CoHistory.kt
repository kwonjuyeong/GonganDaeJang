package com.gonggan.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemNoCompanyCoHistoryBinding
import com.gonggan.DTO.CoHistoryD

class CoHistoryAdapter(private val dataset: List<CoHistoryD>, private val changeList : (data:CoHistoryD) -> Unit) : RecyclerView.Adapter<CoHistoryAdapter.CoHistoryViewHolder>()
{
    class CoHistoryViewHolder(val binding: ItemNoCompanyCoHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CoHistoryViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_no_company_co_history, viewGroup, false)
        return CoHistoryViewHolder(ItemNoCompanyCoHistoryBinding.bind(view))
    }
    override fun onBindViewHolder(viewHolder: CoHistoryViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.companyName.text = listPosition.co_name
        viewHolder.binding.companyDate.text = "${listPosition.co_tenure_start_date}~${listPosition.co_tenure_end_date}"

        viewHolder.binding.coHistoryBtn.setOnClickListener {
            changeList.invoke(listPosition)
        }

    }

    override fun getItemCount() = dataset.size

}