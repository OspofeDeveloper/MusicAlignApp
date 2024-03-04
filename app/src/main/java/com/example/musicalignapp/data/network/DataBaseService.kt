package com.example.musicalignapp.data.network

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import com.example.musicalignapp.core.Constants.IMAGE_TYPE
import com.example.musicalignapp.core.Constants.JSON_TYPE
import com.example.musicalignapp.core.Constants.MUSIC_FILE_TYPE
import com.example.musicalignapp.core.Constants.PACKAGES_PATH
import com.example.musicalignapp.data.response.PackageDto
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

    suspend fun getAllPackages(): List<PackageModel> {
        return firestore.collection(PACKAGES_PATH).orderBy("lastModifiedDate", Query.Direction.DESCENDING)
            .get().await().map { myPackage ->
                myPackage.toObject(PackageDto::class.java).toDomain()
            }
    }

    suspend fun uploadJsonFile(uri: Uri, jsonName: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            val reference = storage.reference.child("uploads/json/$jsonName")
            reference.putFile(uri, createMetadata(JSON_TYPE)).addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
            }
        }
    }

    suspend fun uploadAngGetFile(uri: Uri, fileName: String): FileModel {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            val fileId = "${fileName}_${generatePackageId()}"
            val reference = storage.reference.child("uploads/files/$fileId")
            reference.putFile(uri, createMetadata(MUSIC_FILE_TYPE)).addOnSuccessListener {
                getFileUriFromStorage(it, cancellableCoroutine, fileId)
            }.addOnCanceledListener {
                cancellableCoroutine.resume(FileModel("", ""))
            }
        }
    }

    private fun getFileUriFromStorage(
        uploadTask: UploadTask.TaskSnapshot,
        suspendCancellable: CancellableContinuation<FileModel>,
        fileId: String
    ) {
        uploadTask.storage.downloadUrl.addOnSuccessListener {
            suspendCancellable.resume(FileModel(fileId, it.toString()))
        }.addOnFailureListener {
            suspendCancellable.resume(FileModel("", ""))
        }
    }

    suspend fun uploadAndDownloadImage(uri: Uri): ImageModel {
        return suspendCancellableCoroutine { suspendCancellable ->
            val imageId = generatePackageId()
            val reference = storage.reference.child("uploads/images/$imageId")
            reference.putFile(uri, createMetadata(IMAGE_TYPE)).addOnSuccessListener {
                getImageUriFromStorage(it, suspendCancellable, imageId)
            }.addOnFailureListener {
                suspendCancellable.resume(ImageModel("", ""))
            }
        }
    }

    private fun getImageUriFromStorage(
        uploadTask: UploadTask.TaskSnapshot,
        suspendCancellable: CancellableContinuation<ImageModel>,
        imageId: String
    ) {
        uploadTask.storage.downloadUrl.addOnSuccessListener {
            suspendCancellable.resume(ImageModel(imageId, it.toString()))
        }.addOnFailureListener {
            suspendCancellable.resume(ImageModel("", ""))
        }
    }

    private fun createMetadata(type: String): StorageMetadata {
        val metadata = storageMetadata {
            contentType = when(type) {
               IMAGE_TYPE -> "image/jpeg"
               MUSIC_FILE_TYPE -> "application/octet-stream"
               else -> "application/json"
            }
            setCustomMetadata("date", Date().time.toString())
        }
        return metadata
    }

    suspend fun uploadNewPackage(packageDto: PackageDto): Boolean {
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
            firestore.collection(PACKAGES_PATH).document(packageDto.id).set(packageToUpload).addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
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

        storageReference.getFile(localFile).addOnSuccessListener {
            val inputStream = FileInputStream(localFile)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val meiXml = String(bytes)
            Log.d("AlignActivity", meiXml)
            cancellableCoroutine.resume(meiXml)
        }.addOnFailureListener {
            cancellableCoroutine.resume("")
        }
    }

    suspend fun deleteFile(fileId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/files/$fileId").delete().addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
            }
        }
    }

    suspend fun deleteImage(imageId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/images/$imageId").delete().addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
            }
        }
    }

    suspend fun deletePackage(packageId: String) : Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(PACKAGES_PATH).document(packageId).delete().addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
            }
        }
    }

    suspend fun deleteJson(jsonId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/json/$jsonId").delete().addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
            }
        }
    }
}