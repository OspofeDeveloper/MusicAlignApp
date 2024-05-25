package com.example.musicalignapp.data.remote.firebase

import android.net.Uri
import android.util.Log
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.core.generators.Generator
import com.example.musicalignapp.di.InterfaceAppModule.IdGeneratorAnnotation
import com.example.musicalignapp.di.InterfaceAppModule.PackageDateGeneratorAnnotation
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class StorageService @Inject constructor(
    private val storage: FirebaseStorage,
    @IdGeneratorAnnotation private val idGenerator: Generator<String>,
    @PackageDateGeneratorAnnotation private val packageDateGenerator: Generator<String>
) {

    suspend fun deleteImage(imageId: String, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/$userId/images/$imageId").delete()
                .addOnSuccessListener {
                    cancellableCoroutine.resume(true)
                }.addOnFailureListener {
                cancellableCoroutine.resumeWithException(it)
            }
        }
    }

    suspend fun deleteFile(fileId: String, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/$userId/$fileId/files/").listAll()
                .addOnSuccessListener { result ->
                    val deleteTasks = result.items.map { it.delete() }
                    Tasks.whenAll(deleteTasks)
                        .addOnSuccessListener {
                            cancellableCoroutine.resume(true)
                        }
                        .addOnFailureListener { e ->
                            cancellableCoroutine.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    cancellableCoroutine.resumeWithException(e)
                }
        }
    }

    suspend fun uploadAngGetFile(uri: Uri, fileName: String, userId: String): FileModel {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            val projectName = fileName.substringBeforeLast(".").substringBeforeLast(".")
            val reference = storage.reference.child("uploads/$userId/$projectName/files/$fileName")
            reference.putFile(uri, createMetadata(Constants.MUSIC_FILE_TYPE)).addOnSuccessListener {
                getFileUriFromStorage(it, cancellableCoroutine, fileName)
            }.addOnCanceledListener {
                cancellableCoroutine.resume(FileModel("", ""))
            }
        }
    }

    suspend fun uploadAndDownloadImage(uri: Uri, userId: String): ImageModel {
        return suspendCancellableCoroutine { suspendCancellable ->
            val imageId = packageDateGenerator.generate()
            val reference = storage.reference.child("uploads/$userId/images/$imageId")
            reference.putFile(uri, createMetadata(Constants.IMAGE_TYPE)).addOnSuccessListener {
                getImageUriFromStorage(it, suspendCancellable, imageId)
            }.addOnFailureListener {
                suspendCancellable.resume(ImageModel("", ""))
            }
        }
    }

    suspend fun uploadCropImage(
        uri: Uri,
        cropImageName: String,
        imageName: String,
        userId: String
    ): Boolean {
        return suspendCancellableCoroutine { suspendCancellable ->
            val projectName = cropImageName.substringBeforeLast(".").substringBeforeLast(".")
            val reference =
                storage.reference.child("uploads/$userId/$projectName/images/$cropImageName")
            reference.putFile(uri, createMetadata(Constants.IMAGE_TYPE)).addOnSuccessListener {
                suspendCancellable.resume(true)
            }.addOnFailureListener {
                suspendCancellable.resume(false)
            }
        }
    }


    suspend fun uploadJsonFile(uri: Uri, jsonName: String, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            val reference = storage.reference.child("uploads/$userId/json/$jsonName")
            reference.putFile(uri, createMetadata(Constants.JSON_TYPE)).addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resume(false)
            }
        }
    }

    suspend fun deleteJson(jsonId: String, userId: String): Boolean {
        Log.d("Pozo DatabaseService", "jsonId = $jsonId")
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/$userId/json/$jsonId").delete().addOnSuccessListener {
                cancellableCoroutine.resume(true)
            }.addOnFailureListener {
                cancellableCoroutine.resumeWithException(it)
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