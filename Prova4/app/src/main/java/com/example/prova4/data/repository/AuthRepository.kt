package com.example.prova4.data.repository

import com.example.prova4.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    // Retorna os dados do usuário atual mapeados para o nosso modelo
    fun getCurrentUserData(): User? {
        val user = firebaseAuth.currentUser ?: return User("", "", "")
        return User(
            uid = user.uid,
            name = user.displayName ?: "Usuário",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString() ?: ""
        )
    }

    // Realiza o login com e-mail e senha
    suspend fun login(email: String, pass: String): Result<FirebaseUser?> {
        return runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            result.user
        }
    }

    // Cria uma nova conta e opcionalmente define o nome de exibição
    suspend fun register(email: String, pass: String, name: String = ""): Result<FirebaseUser?> {
        return runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (!name.isBlank() && user != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates).await()
            }
            user
        }
    }

    // Atualiza o perfil do usuário (Nome de exibição, etc.)
    suspend fun updateProfile(newName: String): Result<Unit> {
        return runCatching {
            val user = firebaseAuth.currentUser ?: throw Exception("Nenhum usuário logado")
            val profileUpdates = userProfileChangeRequest {
                displayName = newName
            }
            user.updateProfile(profileUpdates).await()
        }
    }

    // Encerra a sessão do usuário
    fun logout() {
        firebaseAuth.signOut()
    }
}