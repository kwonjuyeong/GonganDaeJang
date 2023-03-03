package com.gonggan.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemJoinCoListBinding

class JoinCoListData(
    val co_address : String,
    val co_ceo : String,
    val co_code : String,
    val co_contact : String,
    val co_name : String,
    val co_type : String
)

class CoListAdapter(
    private val dataset: List<JoinCoListData>, val onClickSelect: (codeListData: JoinCoListData) -> Unit) :
    RecyclerView.Adapter<CoListAdapter.CoListViewHolder>() {

    class CoListViewHolder(val binding: ItemJoinCoListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CoListViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_join_co_list, viewGroup, false)
        return CoListViewHolder(ItemJoinCoListBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: CoListViewHolder, position: Int) {
        val listPosition = dataset[position]
        viewHolder.binding.coAddress.text= listPosition.co_address
        viewHolder.binding.coCeo.text = listPosition.co_ceo
        viewHolder.binding.coCode.text = listPosition.co_code
        viewHolder.binding.coContact.text = listPosition.co_contact
        viewHolder.binding.coName.text = listPosition.co_name

        viewHolder.binding.selectCo.setOnClickListener {
            onClickSelect.invoke(listPosition)
        }

    }
    override fun getItemCount() = dataset.size
}
