package com.gonggan.Adapter

//DashBoard project list item

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggan.R
import com.example.gonggan.databinding.ItemDashboardProjectListBinding
import com.gonggan.source.detailhome.RootActivity

class DashBoardProjectGo(
    val project_name: String?,
    val project_location: String?,
    val project_status: String?,
    val project_cons_code : String,
    val authority_code : String
)

class DashBoardProjectGoAdapter(private val dataset: List<DashBoardProjectGo>) : RecyclerView.Adapter<DashBoardProjectGoAdapter.DashProjectGoViewHolder>()
{
    class DashProjectGoViewHolder(val binding: ItemDashboardProjectListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DashProjectGoViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_dashboard_project_list, viewGroup, false)
        return DashProjectGoViewHolder(ItemDashboardProjectListBinding.bind(view))
    }
    override fun onBindViewHolder(viewHolder: DashProjectGoViewHolder, position: Int) {
        val listPosition = dataset[position]
        val context = viewHolder.binding.root.context

        viewHolder.binding.projectName.text = listPosition.project_name
        viewHolder.binding.projectLocation.text = listPosition.project_location
        viewHolder.binding.projectStatus.text= listPosition.project_status

        viewHolder.binding.projectClick.setOnClickListener {
            val intent = Intent(context, RootActivity::class.java)
            intent.putExtra("TAG", "")
            intent.putExtra("code", listPosition.project_cons_code)
            intent.run{
                context.startActivity(this)
                (context as Activity).finish()
             }
        }
    }
    override fun getItemCount() = dataset.size

}