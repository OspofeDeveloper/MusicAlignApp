package com.example.musicalignapp.data.remote.firebase

import com.example.musicalignapp.core.Constants.FILES_PATH
import com.example.musicalignapp.core.Constants.IMAGES_PATH
import com.example.musicalignapp.core.Constants.JSON_PATH
import com.example.musicalignapp.core.Constants.PROJECTS_PATH
import com.example.musicalignapp.core.Constants.USERS_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DataBaseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getFile(packageId: String, systemName: String, userId: String): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageId)
                .collection(systemName)
                .document(FILES_PATH)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        documentSnapshot.getString("fileUrl").also {
                            it?.let {
                                cancellableCoroutine.resume(it)
                            } ?: run {
                                cancellableCoroutine.resumeWithException(Throwable("Fichero no encontrado"))
                            }
                        }
                    }
                }.addOnFailureListener {
                    cancellableCoroutine.resumeWithException(it)
                }
        }
    }

    private fun loadFileContentFromUri(
        fileUrl: String,
        cancellableCoroutine: CancellableContinuation<File>
    ) {
        val storageReference = storage.getReferenceFromUrl(fileUrl)
        val localFile = File.createTempFile("mei_file", ".mei")

        storageReference.getFile(localFile).addOnSuccessListener {
            cancellableCoroutine.resume(localFile)
        }.addOnFailureListener {
            cancellableCoroutine.resumeWithException(it)
        }
    }

    suspend fun getJsonContent(packageName: String, jsonName: String, userId: String): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/$userId/$packageName/$JSON_PATH/$jsonName")
                .getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val content = String(bytes)
                cancellableCoroutine.resume(content)
            }.addOnFailureListener {
                cancellableCoroutine.resume("")
            }
        }
    }

    suspend fun getImageUriFromPackage(packageName: String, systemName: String, userId: String): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageName)
                .collection(systemName)
                .document(IMAGES_PATH)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val imageUri = documentSnapshot.getString("imageUrl")
                        cancellableCoroutine.resume(imageUri.orEmpty())
                    }
                }.addOnFailureListener {
                    cancellableCoroutine.resumeWithException(it)
                }
        }
    }
}