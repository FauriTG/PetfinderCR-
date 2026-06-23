package com.petfindercr.data.repository

import com.petfindercr.data.model.Perfil
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerfilRepository @Inject constructor(private val client: SupabaseClient) {

    suspend fun getPerfil(userId: String): Result<Perfil> = runCatching {
        client.postgrest["perfiles"]
            .select(Columns.ALL) {
                filter { eq("id", userId) }
            }
            .decodeSingle<Perfil>()
    }

    suspend fun createPerfil(perfil: Perfil): Result<Unit> = runCatching {
        client.postgrest["perfiles"].upsert(perfil)
    }

    suspend fun updatePerfil(userId: String, nombre: String, telefono: String?): Result<Unit> = runCatching {
        client.postgrest["perfiles"].update(
            {
                set("nombre", nombre)
                set("telefono", telefono)
            }
        ) {
            filter { eq("id", userId) }
        }
    }

    suspend fun updateFotoPerfil(userId: String, url: String): Result<Unit> = runCatching {
        client.postgrest["perfiles"].update(
            { set("foto_perfil", url) }
        ) {
            filter { eq("id", userId) }
        }
    }
}
