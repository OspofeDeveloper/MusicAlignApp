package com.example.musicalignapp.data.remote.firebase

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import com.example.musicalignapp.core.Constants.IMAGE_TYPE
import com.example.musicalignapp.core.Constants.JSON_TYPE
import com.example.musicalignapp.core.Constants.MUSIC_FILE_TYPE
import com.example.musicalignapp.core.Constants.PACKAGES_PATH
import com.example.musicalignapp.data.remote.dto.PackageDto
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.example.musicalignapp.domain.model.PackageModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume

class DataBaseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getFileContent(packageId: String): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(PACKAGES_PATH).document(packageId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        documentSnapshot.getString("fileUrl").also {
                            it?.let {
                                loadFileContentFromUri(it, cancellableCoroutine)
                            } ?: cancellableCoroutine.resume("")
                        }
                    }
                }.addOnFailureListener {
                    cancellableCoroutine.resume("")
                }
        }
    }

    private fun loadFileContentFromUri(
        fileUrl: String,
        cancellableCoroutine: CancellableContinuation<String>
    ) {
        val storageReference = storage.getReferenceFromUrl(fileUrl)
        val localFile = File.createTempFile("mei_file", ".mei")

        storageReference.getFile(localFile).addOnSuccessListener {
            val inputStream = FileInputStream(localFile)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val meiXml = String(bytes)
            cancellableCoroutine.resume(meiXml)
        }.addOnFailureListener {
            cancellableCoroutine.resume("")
        }
    }

    suspend fun getJsonContent(jsonId: String): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/json/$jsonId").getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val content = String(bytes)
                cancellableCoroutine.resume(content)
            }.addOnFailureListener {
                cancellableCoroutine.resume("")
            }
        }
    }

    suspend fun getImageUriFromPackage(idPackage: String): String {
        return suspendCancellableCoroutine {  cancellableCoroutine ->
            firestore.collection(PACKAGES_PATH).document(idPackage).get()
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
}