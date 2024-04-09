package com.example.musicalignapp.data.remote.firebase

import android.annotation.SuppressLint
import android.net.Uri
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.core.Constants.USERS_COLLECTION
import com.example.musicalignapp.data.remote.dto.PackageDto
import com.example.musicalignapp.domain.model.PackageModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    suspend fun uploadNewPackage(packageDto: PackageDto, userId: String): Boolean {
        val packageToUpload = hashMapOf(
            "id" to packageDto.id,
            "packageName" to packageDto.packageName,
            "imageUrl" to packageDto.imageUrl,
            "imageId" to packageDto.imageId,
            "fileUrl" to packageDto.fileUrl,
            "fileName" to packageDto.fileName,
            "fileId" to packageDto.fileId,
            "lastModifiedDate" to packageDto.lastModifiedDate
        )

        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(Constants.PACKAGES_PATH)
                .document(packageDto.id)
                .set(packageToUpload)
                .addOnSuccessListener {
                    cancellableCoroutine.resume(true)
                }.addOnFailureListener {
                    cancellableCoroutine.resume(false)
                }
        }
    }

    suspend fun deletePackage(packageId: String, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(Constants.PACKAGES_PATH)
                .document(packageId)
                .delete()
                .addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resumeWithException(it)
            }
        }
    }

    suspend fun getAllPackages(userId: String): List<PackageDto> {
        return firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(Constants.PACKAGES_PATH)
            .orderBy("lastModifiedDate", Query.Direction.DESCENDING)
            .get().await().map { myPackage ->
                myPackage.toObject(PackageDto::class.java)
            }
    }

//
//    suspend fun getFileContent(packageId: String): String {
//        return suspendCancellableCoroutine { cancellableCoroutine ->
//            firestore.collection(Constants.PACKAGES_PATH).document(packageId).get()
//                .addOnSuccessListener { documentSnapshot ->
//                    if (documentSnapshot.exists()) {
//                        documentSnapshot.getString("fileUrl").also {
//                            it?.let {
//                                loadFileContentFromUri(it, cancellableCoroutine)
//                            } ?: cancellableCoroutine.resume("")
//                        }
//                    }
//                }.addOnFailureListener {
//                    cancellableCoroutine.resume("")
//                }
//        }
//    }

    suspend fun getImageUriFromPackage(idPackage: String): String {
        return suspendCancellableCoroutine {  cancellableCoroutine ->
            firestore.collection(Constants.PACKAGES_PATH).document(idPackage).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val imageUri = documentSnapshot.getString("imageUrl")
                        cancellableCoroutine.resume(imageUri.orEmpty())
                    }
                }.addOnFailureListener {
                    cancellableCoroutine.resume("")
                }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun generatePackageDate(): String {
        return SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    private fun generatePackageId(): String {
        return SimpleDateFormat("yyyyMMddhhmmss").format(Date())
    }

    private fun createMetadata(type: String): StorageMetadata {
        val metadata = storageMetadata {
            contentType = when (type) {
                Constants.IMAGE_TYPE -> "image/jpeg"
                Constants.MUSIC_FILE_TYPE -> "application/octet-stream"
                else -> "application/json"
            }
            setCustomMetadata("date", Date().time.toString())
        }
        return metadata
    }
}