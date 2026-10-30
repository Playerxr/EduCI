package com.example.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun getStudentUniqueId(): String {
        return currentUser?.uid ?: "STUDENT-GUEST"
    }

    fun getStudentEmail(): String? {
        return currentUser?.email
    }

    fun getStudentDisplayName(): String? {
        return currentUser?.displayName
    }

    fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || pass.length < 6) {
            onResult(false, "L'adresse email doit être valide et le mot de passe contenir au moins 6 caractères.")
            return
        }
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && displayName.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName.trim())
                            .build()
                        user.updateProfile(profileUpdates)
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Échec de la création de compte.")
                }
            }
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            onResult(false, "Veuillez remplir l'email et le mot de passe.")
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Erreur de connexion. Vérifiez vos identifiants.")
                }
            }
    }

    fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Erreur de connexion anonyme.")
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}
