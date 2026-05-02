package com.example.eureka

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

suspend fun ensureSignedIn(): String {
    val auth = Firebase.auth
    if (auth.currentUser == null) {
        auth.signInAnonymously().await()
    }
    return auth.currentUser!!.uid
}