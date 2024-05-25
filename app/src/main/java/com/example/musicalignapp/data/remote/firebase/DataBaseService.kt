package com.example.musicalignapp.data.remote.firebase

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

class DataBaseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getFileContent(packageId: String, userId: String): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(packageId)
                .get()
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

    suspend fun getJsonContent(jsonId: String, userId: String): String {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            storage.reference.child("uploads/$userId/json/$jsonId").getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val content = String(bytes)
                cancellableCoroutine.resume(content)
            }.addOnFailureListener {
                cancellableCoroutine.resume("")
            }
        }
    }

    suspend fun getImageUriFromPackage(idPackage: String, userId: String): String {
        return suspendCancellableCoroutine {  cancellableCoroutine ->
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PROJECTS_PATH)
                .document(idPackage)
                .get()
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