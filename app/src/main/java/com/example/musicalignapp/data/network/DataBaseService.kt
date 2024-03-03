package com.example.musicalignapp.data.network

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import com.example.musicalignapp.core.Constants.PACKAGES_PATH
import com.example.musicalignapp.data.response.PackageResponse
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

    suspend fun getAllPackages(): List<PackageModel> {
        return firestore.collection(PACKAGES_PATH).orderBy("id", Query.Direction.DESCENDING)
            .get().await().map { myPackage ->
                myPackage.toObject(PackageResponse::class.java).toDomain()
            }
    }

    suspend fun uploadAngGetFileName(uri: Uri): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            val reference = storage.reference.child("uploads/files/${uri.lastPathSegment}")
            reference.putFile(uri, createMetadata(false)).addOnSuccessListener {
                getStorageUri(it, cancellableCoroutine)
            }.addOnCanceledListener {
                cancellableCoroutine.resume("")
            }
        }
    }

    suspend fun uploadAndDownloadImage(uri: Uri): String {
        return suspendCancellableCoroutine { suspendCancellable ->
            val reference = storage.reference.child("uploads/images/${uri.lastPathSegment}")
            reference.putFile(uri, createMetadata(true)).addOnSuccessListener {
                getStorageUri(it, suspendCancellable)
            }.addOnFailureListener {
                suspendCancellable.resume("")
            }
        }
    }

    private fun getStorageUri(
        uploadTask: UploadTask.TaskSnapshot,
        suspendCancellable: CancellableContinuation<String>
    ) {
        uploadTask.storage.downloadUrl.addOnSuccessListener {
            suspendCancellable.resume(it.toString())
        }.addOnFailureListener {
            suspendCancellable.resume("")
        }
    }

    private fun createMetadata(isImage: Boolean): StorageMetadata {
        val metadata = storageMetadata {
            contentType = if (isImage) "image/jpeg" else "application/octet-stream"
            setCustomMetadata("date", Date().time.toString())
        }
        return metadata
    }

    suspend fun uploadNewPackage(
        imageUrl: String,
        fileUrl: String,
        packageName: String,
        fileName: String
    ): Boolean {
        val id = generatePackageId()
        val lastModifiedDate = generatePackageDate()
        val packageDto = hashMapOf(
            "id" to id,
            "packageName" to packageName,
            "imageUrl" to imageUrl,
            "fileUrl" to fileUrl,
            "fileName" to fileName,
            "lastModifiedDate" to lastModifiedDate
        )

        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(PACKAGES_PATH).document(id).set(packageDto).addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
            }
        }
    }

    private fun generatePackageDate(): String {
        return SimpleDateFormat("dd/MM/yyyy").format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    private fun generatePackageId(): String {
        return SimpleDateFormat("yyyyMMddhhmmss").format(Date())
    }

    suspend fun getFileContent(fileId: String): String? {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(PACKAGES_PATH).document(fileId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        documentSnapshot.getString("fileUrl").also {
                            it?.let {
                                loadFileContentFromUri(it, cancellableCoroutine)
                            }
                        }
                    }
                }.addOnFailureListener {
                cancellableCoroutine.resume("")
            }
        }
    }

    private fun loadFileContentFromUri(
        fileUrl: String,
        cancellableCoroutine: CancellableContinuation<String?>
    ) {
        val storageReference = storage.getReferenceFromUrl(fileUrl)
        val localFile = File.createTempFile("mei_file", ".mei")
        storageReference.getFile(localFile)
            .addOnSuccessListener {
                val inputStream = FileInputStream(localFile)
                val bytes = inputStream.readBytes()
                inputStream.close()

                val meiXml = String(bytes)
                Log.d("AlignActivity", meiXml)
                cancellableCoroutine.resume(meiXml)
            }
            .addOnFailureListener {
                cancellableCoroutine.resume("")
            }
    }
}