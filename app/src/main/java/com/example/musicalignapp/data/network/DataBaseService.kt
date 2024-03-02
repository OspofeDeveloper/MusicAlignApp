package com.example.musicalignapp.data.network

import android.net.Uri
import com.example.musicalignapp.core.Constants.PACKAGES_PATH
import com.example.musicalignapp.data.response.PackageResponse
import com.example.musicalignapp.domain.model.PackageModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume

class DataBaseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getAllPackages(): List<PackageModel> {
        return firestore.collection(PACKAGES_PATH).get().await().map { myPackage ->
            myPackage.toObject(PackageResponse::class.java).toDomain()
        }
    }

    suspend fun uploadAndDownloadImage(uri: Uri): String {
        return suspendCancellableCoroutine {  suspendCancellable ->
            val reference = storage.reference.child("download/${uri.lastPathSegment}")
            reference.putFile(uri, createMetadata()).addOnSuccessListener {
                downloadImage(it, suspendCancellable)
            }.addOnFailureListener {
                suspendCancellable.resume("")
            }
        }
    }

    private fun downloadImage(
        uploadTask: UploadTask.TaskSnapshot,
        suspendCancellable: CancellableContinuation<String>
    ) {
        uploadTask.storage.downloadUrl.addOnSuccessListener {
            suspendCancellable.resume(it.toString())
        }.addOnFailureListener {
            suspendCancellable.resume("")
        }
    }

    private fun createMetadata(): StorageMetadata {
        val metadata = storageMetadata {
            contentType = "image/jpeg"
            setCustomMetadata("date", Date().time.toString())
        }
        return metadata
    }
}