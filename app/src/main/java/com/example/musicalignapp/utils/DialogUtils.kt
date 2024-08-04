package com.example.musicalignapp.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.musicalignapp.R
import com.example.musicalignapp.databinding.DialogErrorLoadingPackageBinding
import com.example.musicalignapp.databinding.DialogSaveCropImageBinding

object DialogUtils {

    object GenericDialogs {
        fun showErrorDialog(error: String, layoutInflater: LayoutInflater, context: Context) {
            val dialogBinding = DialogErrorLoadingPackageBinding.inflate(layoutInflater)
            val alertDialog = AlertDialog.Builder(context).apply {
                setView(dialogBinding.root)
            }.create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialogBinding.tvError.text = error

            dialogBinding.btnOk.setOnClickListener { alertDialog.dismiss() }

            alertDialog.show()
        }
    }

    object AddFileDialogs {
        fun showSaveCropImageDialog(
            uri: Uri,
            cropImageName: String,
            layoutInflater: LayoutInflater,
            context: Context,
            onAccept: () -> Unit
        ) {
            val dialogBinding = DialogSaveCropImageBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(context).apply {
                setView(dialogBinding.root)
            }.create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialogBinding.ivCropImage.setImageURI(uri)
            dialogBinding.tvSaveCropImage.text =
                context.getString(R.string.save_crop_image, cropImageName)

            dialogBinding.btnAccept.setOnClickListener {
                dialogBinding.pbLoading.isVisible = true
                onAccept()
                dialog.dismiss()
                dialogBinding.pbLoading.isVisible = false
            }

            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}