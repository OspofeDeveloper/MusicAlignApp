package com.example.musicalignapp.ui.screens.home.adapter

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicalignapp.databinding.ItemPackageBinding
import com.example.musicalignapp.domain.model.PackageModel

class PackagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemPackageBinding.bind(view)

    fun render(
        onItemSelected: (String) -> Unit,
        onDeletePackageSelected: (String, String, String, String) -> Unit,
        packageModel: PackageModel
    ) {
        binding.apply {
            Glide.with(binding.tvTitle.context).load(packageModel.imageUrl).into(ivPackage)

            tvTitle.text = packageModel.packageName
            tvLastModificationDate.text = "Last Modified:  ${packageModel.lastModifiedDate}"
            tvFileName.text = "File Name:  ${packageModel.fileName}"
            Log.d("Pozo PackagesVewHolder", packageModel.jsonId)

            binding.cvPackageItem.setOnClickListener { onItemSelected(packageModel.id) }
            binding.ivDelete.setOnClickListener { onDeletePackageSelected(packageModel.id, packageModel.fileId, packageModel.imageId, packageModel.jsonId) }
        }
    }

}