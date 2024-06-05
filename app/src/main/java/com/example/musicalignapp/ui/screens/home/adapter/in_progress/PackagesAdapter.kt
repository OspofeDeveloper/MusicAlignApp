package com.example.musicalignapp.ui.screens.home.adapter.in_progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicalignapp.R
import com.example.musicalignapp.domain.model.ProjectHomeModel

class PackagesAdapter(
    private var packages: List<ProjectHomeModel> = emptyList(),
    private val onItemSelected: (String, String) -> Unit,
    private val onDeletePackageSelected: (String) -> Unit,
): RecyclerView.Adapter<PackagesViewHolder>() {

    fun updateList(packages: List<ProjectHomeModel>) {
        this.packages = packages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackagesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_package, parent, false)
        return PackagesViewHolder(view)
    }

    override fun getItemCount(): Int = packages.size

    override fun onBindViewHolder(holder: PackagesViewHolder, position: Int) {
        holder.render(onItemSelected, onDeletePackageSelected, packages[position])
    }
}