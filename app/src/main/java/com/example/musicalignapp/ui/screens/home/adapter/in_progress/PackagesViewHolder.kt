package com.example.musicalignapp.ui.screens.home.adapter.in_progress

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ItemPackageBinding
import com.example.musicalignapp.domain.model.ProjectHomeModel

class PackagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemPackageBinding.bind(view)

    fun render(
        onItemSelected: (String, String) -> Unit,
        onDeletePackageSelected: (String) -> Unit,
        projectHomeModel: ProjectHomeModel
    ) {
        binding.apply {
            Glide.with(binding.tvTitle.context).load(projectHomeModel.originalImageUrl).into(ivPackage)

            tvTitle.text = projectHomeModel.projectName
            tvLastModificationDate.text = binding.ivPackage.context.getString(
                R.string.last_modified,
                projectHomeModel.lastModified
            )

            binding.cvPackageItem.setOnClickListener { onItemSelected(projectHomeModel.projectName, projectHomeModel.originalImageUrl) }
            binding.ivDelete.setOnClickListener {
                onDeletePackageSelected(
                    projectHomeModel.projectName,
                )
            }
        }
    }
}