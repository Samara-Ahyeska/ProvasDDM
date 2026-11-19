package com.example.prova5.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    val currentUserName: String?
        get() = auth.currentUser?.displayName

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw IllegalStateException("Usuário não encontrado.")
        user.reload().await()
        user.uid
    }

    suspend fun register(name: String, email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw IllegalStateException("Falha ao criar conta.")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()
        user.updateProfile(profileUpdates).await()
        user.reload().await()

        user.uid
    }

    suspend fun reloadCurrentUser(): Result<Unit> = runCatching {
        auth.currentUser?.reload()?.await()
    }

    suspend fun updatePasswordAndLogout(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("Usuário não autenticado.")
        val email = user.email ?: throw IllegalStateException("E-mail do usuário não disponível.")

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()

        // Desconecta automaticamente após alterar a senha com sucesso
        auth.signOut()
    }

    suspend fun updateDisplayName(newName: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("Usuário não autenticado.")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName.trim())
            .build()
        user.updateProfile(profileUpdates).await()
        user.reload().await()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun logout() {
        auth.signOut()
    }
}