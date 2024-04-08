package com.example.musicalignapp.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun login(user: String, password: String): FirebaseUser? {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firebaseAuth.signInWithEmailAndPassword(user, password)
                .addOnSuccessListener { cancellableCoroutine.resume(it.user) }
                .addOnFailureListener { cancellableCoroutine.resumeWithException(it) }
        }
    }

    suspend fun register(email: String, password: String): FirebaseUser? {
        return suspendCancellableCoroutine { cancellableCoroutine ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { cancellableCoroutine.resume(it.user) }
                .addOnFailureListener { cancellableCoroutine.resumeWithException(it) }
        }
    }

    fun isUserLogged(): Boolean {
        return getCurrentUser() != null
    }

    private fun getCurrentUser() = firebaseAuth.currentUser

    fun logout() {
        firebaseAuth.signOut()
    }
}