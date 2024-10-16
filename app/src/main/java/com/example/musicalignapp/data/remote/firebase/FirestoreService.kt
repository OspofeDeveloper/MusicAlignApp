package com.example.musicalignapp.data.remote.firebase

import android.annotation.SuppressLint
import android.net.Uri
import com.example.musicalignapp.core.Constants
import com.example.musicalignapp.core.Constants.FILES_PATH
import com.example.musicalignapp.core.Constants.IMAGES_PATH
import com.example.musicalignapp.core.Constants.JSON_PATH
import com.example.musicalignapp.core.Constants.PROJECTS_PATH
import com.example.musicalignapp.core.Constants.USERS_COLLECTION
import com.example.musicalignapp.core.extensions.toTwoDigits
import com.example.musicalignapp.data.remote.dto.FileDto
import com.example.musicalignapp.data.remote.dto.ImageDto
import com.example.musicalignapp.data.remote.dto.JsonDto
import com.example.musicalignapp.data.remote.dto.ProjectDto
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.CancellableContinuation
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
    suspend fun uploadProject(projectDto: ProjectDto, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableContinuation ->
            projectDto.apply {
                uploadImages(cancellableContinuation, this.project_name, this.imagesList, userId)
                uploadFiles(cancellableContinuation, this.project_name, this.filesList, userId)
                uploadJsonList(cancellableContinuation, this.project_name, this.jsonList, userId)
            }

            val projectFinished = hashMapOf(
                "project_name" to projectDto.project_name,
                "isFinished" to false,
                "last_modified" to projectDto.last_modified,
                "originalImageUrl" to projectDto.originalImageUrl,
                "currentSystem" to "01",
                "numSystems" to projectDto.filesList.size.toTwoDigits()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(projectDto.project_name)
                .set(projectFinished)
                .addOnSuccessListener {
                    cancellableContinuation.resume(true)
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
    }

    private fun uploadImages(
        cancellableContinuation: CancellableContinuation<Boolean>,
        projectName: String,
        images: List<ImageDto>,
        userId: String
    ): Boolean {
        images.map { image ->
            val imageToUpload = hashMapOf(
                "imageName" to image.imageName,
                "imageUrl" to image.imageUrl
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(projectName)
                .collection(image.imageNameNoExtension)
                .document(IMAGES_PATH)
                .set(imageToUpload)
                .addOnSuccessListener {
                    return@addOnSuccessListener
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
        return true
    }

    suspend fun replaceImage(
        imageName: String,
        imageUrl: String,
        userId: String
    ): Boolean {
        val packageName = imageName.substringBeforeLast(".").substringBeforeLast(".")
        val systemName = imageName.substringBeforeLast(".")
        val imageToUpload = hashMapOf(
            "imageName" to imageName,
            "imageUrl" to imageUrl
        )

        return suspendCancellableCoroutine { cancellableContinuation ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageName)
                .collection(systemName)
                .document(IMAGES_PATH)
                .set(imageToUpload)
                .addOnSuccessListener {
                    cancellableContinuation.resume(true)
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
    }

    suspend fun replaceFile(
        fileName: String,
        fileUrl: String,
        userId: String
    ): Boolean {
        val packageName = fileName.substringBeforeLast(".").substringBeforeLast(".")
        val systemName = fileName.substringBeforeLast(".")
        val fileToUpload = hashMapOf(
            "fileName" to fileName,
            "fileUrl" to fileUrl
        )

        return suspendCancellableCoroutine { cancellableContinuation ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageName)
                .collection(systemName)
                .document(FILES_PATH)
                .set(fileToUpload)
                .addOnSuccessListener {
                    cancellableContinuation.resume(true)
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
    }

    private fun uploadFiles(
        cancellableContinuation: CancellableContinuation<Boolean>,
        projectName: String,
        files: List<FileDto>,
        userId: String
    ): Boolean {
        files.map { file ->
            val fileToUpload = hashMapOf(
                "fileName" to file.fileName,
                "fileUrl" to file.fileUrl
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(projectName)
                .collection(file.fileNameNoExtension)
                .document(FILES_PATH)
                .set(fileToUpload)
                .addOnSuccessListener {
                    if (files.indexOf(file) == files.size - 1) {
                        return@addOnSuccessListener
                    }
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
        return true
    }

    private fun uploadJsonList(
        cancellableContinuation: CancellableContinuation<Boolean>,
        projectName: String,
        jsonList: List<JsonDto>,
        userId: String
    ): Boolean {
        jsonList.map { json ->
            val jsonToUpload = hashMapOf(
                "jsonId" to json.jsonId,
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(projectName)
                .collection(json.jsonId.substringBeforeLast('.'))
                .document(JSON_PATH)
                .set(jsonToUpload)
                .addOnSuccessListener {
                    if (jsonList.indexOf(json) == jsonList.size - 1) {
                        return@addOnSuccessListener
                    }
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
        return true
    }

    suspend fun deletePackage(packageId: String, userId: String, numSystems: Int): Boolean {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageId)
                .delete()
                .addOnSuccessListener {
                    deleteRootSystem(packageId, userId, cancellableCoroutine)
                    for (i in 1..numSystems + 1) {
                        deleteSystemImages(packageId, userId, cancellableCoroutine, i)
                        deleteSystemFiles(packageId, userId, cancellableCoroutine, i)
                        deleteSystemJson(packageId, userId, cancellableCoroutine, i)
                    }
                    cancellableCoroutine.resume(true)
                }.addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableCoroutine.resumeWithException(it)
                }
        }
    }

    private fun deleteRootSystem(packageId: String, userId: String, cancellableCoroutine: CancellableContinuation<Boolean>): Boolean {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(PROJECTS_PATH)
            .document(packageId)
            .collection(packageId)
            .document("images")
            .delete()
            .addOnSuccessListener {
                return@addOnSuccessListener
            }.addOnFailureListener {
                FirebaseCrashlytics.getInstance().recordException(it)
                cancellableCoroutine.resumeWithException(it)
            }

        return true
    }

    private fun deleteSystemImages(packageId: String, userId: String, cancellableContinuation: CancellableContinuation<Boolean>, numSystem: Int) : Boolean {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(PROJECTS_PATH)
            .document(packageId)
            .collection("${packageId}.${numSystem.toTwoDigits()}")
            .document("images")
            .delete()
            .addOnSuccessListener {
                return@addOnSuccessListener
            }.addOnFailureListener {
                FirebaseCrashlytics.getInstance().recordException(it)
                cancellableContinuation.resumeWithException(it)
            }
        return true
    }

    private fun deleteSystemFiles(packageId: String, userId: String, cancellableContinuation: CancellableContinuation<Boolean>, numSystem: Int) : Boolean {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(PROJECTS_PATH)
            .document(packageId)
            .collection("${packageId}.${numSystem.toTwoDigits()}")
            .document("files")
            .delete()
            .addOnSuccessListener {
                return@addOnSuccessListener
            }.addOnFailureListener {
                FirebaseCrashlytics.getInstance().recordException(it)
                cancellableContinuation.resumeWithException(it)
            }
        return true
    }

    private fun deleteSystemJson(packageId: String, userId: String, cancellableContinuation: CancellableContinuation<Boolean>, numSystem: Int) : Boolean {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(PROJECTS_PATH)
            .document(packageId)
            .collection("${packageId}.${numSystem.toTwoDigits()}")
            .document("jsons")
            .delete()
            .addOnSuccessListener {
                return@addOnSuccessListener
            }.addOnFailureListener {
                FirebaseCrashlytics.getInstance().recordException(it)
                cancellableContinuation.resumeWithException(it)
            }
        return true
    }

    suspend fun getAllPackages(userId: String): List<ProjectDto> {
        return firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(PROJECTS_PATH)
            .get().await().map { myPackage ->
                ProjectDto(
                    project_name = myPackage.getString("project_name") ?: "",
                    currentSystem = myPackage.getString("currentSystem") ?: "",
                    imagesList = myPackage.get("imagesList") as? List<ImageDto> ?: emptyList(),
                    filesList = myPackage.get("filesList") as? List<FileDto> ?: emptyList(),
                    jsonList = myPackage.get("jsonList") as? List<JsonDto> ?: emptyList(),
                    isFinished = myPackage.getBoolean("isFinished") ?: false,
                    last_modified = myPackage.getString("last_modified") ?: "",
                    originalImageUrl = myPackage.getString("originalImageUrl") ?: "",
                    maxNumSystems = myPackage.getString("numSystems") ?: ""
                )
            }
    }

    suspend fun getSystemNumber(packageName: String, userId: String): String {
        return suspendCancellableCoroutine { cancellableContinuation ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageName)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val currentSystem = documentSnapshot.getString("currentSystem")
                        cancellableContinuation.resume(currentSystem ?: "")
                    } else {
                        cancellableContinuation.resume("")
                    }
                }.addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
    }

    suspend fun getMaxSystemNumber(packageName: String, userId: String): String {
        return suspendCancellableCoroutine { cancellableContinuation ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageName)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val maxNumSystems = documentSnapshot.getString("numSystems")
                        cancellableContinuation.resume(maxNumSystems ?: "")
                    } else {
                        cancellableContinuation.resume("")
                    }
                }.addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
        }
    }

    suspend fun saveProject(projectDto: ProjectDto, userId: String): Boolean {
        return suspendCancellableCoroutine { cancellableContinuation ->

            val projectFinished = hashMapOf(
                "project_name" to projectDto.project_name,
                "isFinished" to projectDto.isFinished,
                "last_modified" to projectDto.last_modified,
                "originalImageUrl" to projectDto.originalImageUrl,
                "currentSystem" to projectDto.currentSystem,
                "numSystems" to projectDto.maxNumSystems
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(projectDto.project_name)
                .set(projectFinished)
                .addOnSuccessListener {
                    cancellableContinuation.resume(true)
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    cancellableContinuation.resumeWithException(it)
                }
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
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(Constants.PROJECTS_PATH).document(idPackage).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val imageUri = documentSnapshot.getString("imageUrl")
                        cancellableCoroutine.resume(imageUri.orEmpty())
                    }
                }.addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
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