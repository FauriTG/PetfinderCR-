package com.petfindercr.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(private val client: SupabaseClient) {

    val currentUser: UserInfo? get() = client.auth.currentUserOrNull()

    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun signUp(email: String, password: String, nombre: String): Result<Unit> = runCatching {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        client.auth.signOut()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        client.auth.resetPasswordForEmail(email)
    }

    suspend fun refreshSession(): Result<Unit> = runCatching {
        client.auth.refreshCurrentSession()
    }
}
