package com.example.musicalignapp.data.remote.firebase

import android.net.Uri
import android.util.Log
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.core.Constants.IMAGE_TYPE
import com.example.musicalignapp.core.Constants.JSON_TYPE
import com.example.musicalignapp.core.generators.Generator
import com.example.musicalignapp.data.remote.dto.ImageDto
import com.example.musicalignapp.data.remote.dto.JsonDto
import com.example.musicalignapp.di.InterfaceAppModule.IdGeneratorAnnotation
import com.example.musicalignapp.di.InterfaceAppModule.PackageDateGeneratorAnnotation
import com.example.musicalignapp.domain.model.FileModel
import com.example.musicalignapp.domain.model.ImageModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
            storage.reference.child("uploads/$userId/${imageId.substringBeforeLast('.')}/images/")
                .listAll()
                .addOnSuccessListener { result ->
                    val deleteTasks = result.items.map { it.delete() }
                    Log.d("Pozo", "deleteImage: ${result.items.joinToString { it.name }}")
                    Tasks.whenAll(deleteTasks)
                        .addOnSuccessListener {
                            cancellableCoroutine.resume(true)
                        }
                        .addOnFailureListener { e ->
                            FirebaseCrashlytics.getInstance().recordException(e)
                            cancellableCoroutine.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    cancellableCoroutine.resumeWithException(e)
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
                            FirebaseCrashlytics.getInstance().recordException(e)
                            cancellableCoroutine.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    cancellableCoroutine.resumeWithException(e)
                }
        }
    }

    suspend fun deleteJson(projectId: String, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/$userId/$projectId/jsons/").listAll()
                .addOnSuccessListener { result ->
                    val deleteTasks = result.items.map { it.delete() }
                    Tasks.whenAll(deleteTasks)
                        .addOnSuccessListener {
                            cancellableCoroutine.resume(true)
                        }
                        .addOnFailureListener { e ->
                            FirebaseCrashlytics.getInstance().recordException(e)
                            cancellableCoroutine.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    cancellableCoroutine.resumeWithException(e)
                }
        }
    }

    suspend fun deleteFinalOutputJson(projectId: String, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/$userId/$projectId").listAll()
                .addOnSuccessListener { result ->
                    val deleteTasks = result.items.map { it.delete() }
                    Tasks.whenAll(deleteTasks)
                        .addOnSuccessListener {
                            cancellableCoroutine.resume(true)
                        }
                        .addOnFailureListener { e ->
                            FirebaseCrashlytics.getInstance().recordException(e)
                            cancellableCoroutine.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    cancellableCoroutine.resumeWithException(e)
                }
        }
    }

    suspend fun uploadAngGetFile(uri: Uri, fileName: String, userId: String): FileModel {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            val reference =
                storage.reference.child("uploads/$userId/${getProjectName(fileName)}/files/$fileName")
            reference.putFile(uri, createMetadata(Constants.MUSIC_FILE_TYPE)).addOnSuccessListener {
                getFileUriFromStorage(it, cancellableCoroutine, fileName)
            }.addOnCanceledListener {
                cancellableCoroutine.resume(FileModel("", ""))
            }
        }
    }

    suspend fun replaceFile(uri: Uri, fileName: String, userId: String): FileModel {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            val reference =
                storage.reference.child("uploads/$userId/${getProjectName(fileName)}/files/")

            reference.listAll().addOnSuccessListener { listResult ->
                val baseName = fileName.substringBeforeLast(".")

                listResult.items.forEach { item ->
                    if (item.name.substringBeforeLast(".") == baseName) {
                        item.delete().addOnFailureListener { exception ->
                            FirebaseCrashlytics.getInstance().recordException(exception)
                            cancellableCoroutine.resumeWithException(exception)
                        }
                    }
                }

                val newFileRef = reference.child(fileName)
                newFileRef.putFile(uri, createMetadata(Constants.MUSIC_FILE_TYPE))
                    .addOnSuccessListener {
                        getFileUriFromStorage(it, cancellableCoroutine, fileName)
                    }.addOnCanceledListener {
                    cancellableCoroutine.resume(FileModel("", ""))
                }.addOnFailureListener { exception ->
                    FirebaseCrashlytics.getInstance().recordException(exception)
                    cancellableCoroutine.resumeWithException(exception)
                }
            }.addOnFailureListener { exception ->
                FirebaseCrashlytics.getInstance().recordException(exception)
                cancellableCoroutine.resumeWithException(exception)
            }
        }
    }


    suspend fun getImagesNameList(userId: String): List<String> {
        return suspendCancellableCoroutine { suspendCancellableCoroutine ->
            storage.reference.child("uploads/$userId").listAll()
                .addOnSuccessListener { result ->
                    val folderNames = result.prefixes.map { it.name }
                    suspendCancellableCoroutine.resume(folderNames)
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    suspendCancellableCoroutine.resumeWithException(e)
                }
        }
    }

    suspend fun uploadAndDownloadImage(uri: Uri, userId: String): ImageModel {
        return suspendCancellableCoroutine { suspendCancellable ->
//            val imageId = packageDateGenerator.generate()
//            val reference = storage.reference.child("uploads/$userId/images/$imageId")
//            reference.putFile(uri, createMetadata(Constants.IMAGE_TYPE)).addOnSuccessListener {
//                getImageUriFromStorage(it, suspendCancellable, imageId)
//            }.addOnFailureListener {
//                suspendCancellable.resume(ImageModel("", ""))
//            }
        }
    }

    suspend fun uploadCropImage(
        uri: Uri,
        cropImageName: String,
        userId: String
    ): ImageDto {
        return suspendCancellableCoroutine { suspendCancellable ->
            val reference =
                storage.reference.child("uploads/$userId/${getProjectName(cropImageName)}/images/$cropImageName")
            reference.putFile(uri, createMetadata(IMAGE_TYPE)).addOnSuccessListener {
                getImageUriFromStorage(it, suspendCancellable, cropImageName)
            }.addOnFailureListener {
                FirebaseCrashlytics.getInstance().recordException(it)
                suspendCancellable.resume(ImageDto("", "", ""))
            }
        }
    }

    suspend fun uploadOriginalImage(
        imageUrl: Uri,
        imageName: String,
        userId: String
    ): ImageDto {
        return suspendCancellableCoroutine { suspendCancellable ->
            val reference =
                storage.reference.child("uploads/$userId/${imageName.substringBeforeLast('.')}/images/$imageName")
            reference.putFile(imageUrl, createMetadata(IMAGE_TYPE)).addOnSuccessListener {
                getImageUriFromStorage(it, suspendCancellable, imageName)
            }.addOnFailureListener {
                FirebaseCrashlytics.getInstance().recordException(it)
                suspendCancellable.resumeWithException(it)
            }
        }
    }

    suspend fun uploadFinalOutputJson(
        jsonFile: JsonDto,
        fromAlign: Boolean,
        userId: String
    ): Boolean {
        return suspendCancellableCoroutine { cancellableContinuation ->
            val projectName = if (fromAlign) {
                jsonFile.jsonProjectName.substringBeforeLast('.').substringBeforeLast('_')
            } else {
                jsonFile.jsonProjectName.substringBeforeLast('_')
            }
            val reference =
                storage.reference.child("uploads/$userId/$projectName/${jsonFile.jsonId}")
            reference.putFile(jsonFile.jsonUri, createMetadata(JSON_TYPE)).addOnSuccessListener {
                cancellableContinuation.resume(true)
            }.addOnFailureListener { exception ->
                FirebaseCrashlytics.getInstance().recordException(exception)
                cancellableContinuation.resumeWithException(exception)
            }
        }
    }

    suspend fun uploadJsonFiles(jsonFiles: List<JsonDto>, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableContinuation ->
            jsonFiles.forEach {
                uploadSingleJsonFile(cancellableContinuation, it, false, userId)
            }
            cancellableContinuation.resume(true)
        }
    }

    suspend fun uploadJsonFile(jsonFile: JsonDto, fromAlign: Boolean, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableContinuation ->
            uploadSingleJsonFile(cancellableContinuation, jsonFile, fromAlign, userId)
            cancellableContinuation.resume(true)
        }
    }

    suspend fun replaceJson(newJson: JsonDto, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableContinuation ->
            val projectName = newJson.jsonProjectName
            val reference =
                storage.reference.child("uploads/$userId/$projectName/jsons/${newJson.jsonId}")
            reference.putFile(newJson.jsonUri, createMetadata(JSON_TYPE)).addOnSuccessListener {
                cancellableContinuation.resume(true)
            }.addOnFailureListener { exception ->
                FirebaseCrashlytics.getInstance().recordException(exception)
                cancellableContinuation.resumeWithException(exception)
            }
        }
    }

    private fun uploadSingleJsonFile(
        cancellableContinuation: CancellableContinuation<Boolean>,
        jsonFile: JsonDto,
        fromAlign: Boolean,
        userId: String
    ): Boolean {
        val projectName =
            if (fromAlign) jsonFile.jsonProjectName.substringBeforeLast('.') else jsonFile.jsonProjectName
        val reference =
            storage.reference.child("uploads/$userId/$projectName/jsons/${jsonFile.jsonId}")
        reference.putFile(jsonFile.jsonUri, createMetadata(JSON_TYPE)).addOnSuccessListener {
            return@addOnSuccessListener
        }.addOnFailureListener { exception ->
            FirebaseCrashlytics.getInstance().recordException(exception)
            cancellableContinuation.resumeWithException(exception)
        }
        return true
    }

    private fun getFileUriFromStorage(
        uploadTask: UploadTask.TaskSnapshot,
        suspendCancellable: CancellableContinuation<FileModel>,
        fileId: String
    ) {
        uploadTask.storage.downloadUrl.addOnSuccessListener {
            suspendCancellable.resume(FileModel(fileId, it.toString()))
        }.addOnFailureListener {
            FirebaseCrashlytics.getInstance().recordException(it)
            suspendCancellable.resume(FileModel("", ""))
        }
    }

    private fun getImageUriFromStorage(
        uploadTask: UploadTask.TaskSnapshot,
        suspendCancellable: CancellableContinuation<ImageDto>,
        imageId: String
    ) {
        uploadTask.storage.downloadUrl.addOnSuccessListener {
            suspendCancellable.resume(ImageDto("", imageId, it.toString()))
        }.addOnFailureListener {
            FirebaseCrashlytics.getInstance().recordException(it)
            suspendCancellable.resume(ImageDto("", "", ""))
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

    private fun getProjectName(name: String): String =
        name.substringBeforeLast(".").substringBeforeLast(".")

}