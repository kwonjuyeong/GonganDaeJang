package com.gonggan.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemNoCompanyProjListBinding

class DashBoardNoCompanyProjectList(
   val co_code : String,
   val cons_code : String,
   val cons_name : String,
   val id : String,
   val location : String,
   val proj_progress_end_date : String,
   val proj_progress_start_date : String,
   val req_date : String
)

class DashBoardNoCompanyProjectListAdapter(private val dataset: List<DashBoardNoCompanyProjectList>)
: RecyclerView.Adapter<DashBoardNoCompanyProjectListAdapter.DashBoardNoCompanyProjectListViewHolder>()
{
    class DashBoardNoCompanyProjectListViewHolder(val binding: ItemNoCompanyProjListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DashBoardNoCompanyProjectListViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_no_company_proj_list, viewGroup, false)
        return DashBoardNoCompanyProjectListViewHolder(ItemNoCompanyProjListBinding.bind(view))
    }
    override fun onBindViewHolder(viewHolder: DashBoardNoCompanyProjectListViewHolder, position: Int) {
        val listPosition = dataset[position]

        viewHolder.binding.consName.text = listPosition.cons_name
        viewHolder.binding.consLocation.text = listPosition.location
        viewHolder.binding.projHistoryBtn.setOnClickListener {
            //버튼 이벤트 넣기

        }
    }

    override fun getItemCount() = dataset.size

}