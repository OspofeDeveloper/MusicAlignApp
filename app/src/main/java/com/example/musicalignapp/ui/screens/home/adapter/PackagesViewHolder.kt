package com.example.musicalignapp.ui.screens.home.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.ItemPackageBinding
import com.example.musicalignapp.domain.model.ProjectModel

class PackagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemPackageBinding.bind(view)

    fun render(
        onItemSelected: (String) -> Unit,
        onDeletePackageSelected: (String, String, String, String) -> Unit,
        packageModel: ProjectModel
    ) {
        binding.apply {
            Glide.with(binding.tvTitle.context).load(packageModel.imagesList.first())
                .into(ivPackage)

            tvTitle.text = packageModel.projectName
            tvLastModificationDate.text = binding.ivPackage.context.getString(
                R.string.last_modified,
                packageModel.lastModified
            )
            tvFileName.text = binding.ivPackage.context.getString(
                R.string.file_name,
                packageModel.filesList.first()
            )

            binding.cvPackageItem.setOnClickListener { onItemSelected(packageModel.projectName) }
            binding.ivDelete.setOnClickListener {
                onDeletePackageSelected(
                    packageModel.projectName,
                    packageModel.filesList.first().fileName,
                    packageModel.imagesList.first().imageName,
                    packageModel.jsonList.first().jsonId
                )
            }
        }
    }

}