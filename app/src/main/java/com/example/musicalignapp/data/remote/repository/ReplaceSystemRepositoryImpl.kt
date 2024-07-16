package com.example.musicalignapp.data.remote.repository

import android.net.Uri
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.data.local.shared_prefs.SharedPreferences
import com.example.musicalignapp.data.remote.firebase.FirestoreService
import com.example.musicalignapp.data.remote.firebase.StorageService
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.model.JsonModel
import com.example.musicalignapp.domain.repository.ReplaceSystemRepository
import javax.inject.Inject

class ReplaceSystemRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val firestoreService: FirestoreService,
    private val sharedPreferences: SharedPreferences
) : ReplaceSystemRepository {

    override suspend fun replaceFile(fileName: String, fileUrl: Uri): Boolean {
        val storageResponse = storageService.replaceFile(fileUrl, fileName, getUserId())
        storageResponse.apply {
            if(this.fileUrl.isNotBlank() && this.fileName.isNotBlank()) {
                return firestoreService.replaceFile(fileName, this.fileUrl, getUserId())
            }
        }
        return false
    }

    override suspend fun replaceImage(imageName: String, imageUri: Uri): ImageModel {
        val imageReplaced =  storageService.uploadCropImage(imageUri, imageName, getUserId()).toDomain()
        imageReplaced.apply {
            if(this.imageUrl.isNotBlank() && this.imageName.isNotBlank()) {
                firestoreService.replaceImage(imageName, this.imageUrl, getUserId())
            }
        }
        return imageReplaced
    }

    override suspend fun replaceJson(newJson: JsonModel) {
        storageService.replaceJson(newJson.toDto(), getUserId())
    }

    private suspend fun getUserId(): String {
        return sharedPreferences.getUserId(Constants.USER_ID_KEY)
    }

}