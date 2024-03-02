package com.example.musicalignapp.data.network

import com.example.musicalignapp.core.Constants.PACKAGES_PATH
import com.example.musicalignapp.data.response.PackageResponse
import com.example.musicalignapp.domain.model.PackageModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DataBaseService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getAllProducts(): List<PackageModel> {
        return firestore.collection(PACKAGES_PATH).get().await().map { myPackage ->
            myPackage.toObject(PackageResponse::class.java).toDomain()
        }
    }
}