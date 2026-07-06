package com.petfindercr.data.repository

import com.petfindercr.data.model.Notificacion
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificacionRepository @Inject constructor(private val client: SupabaseClient) {

    @Serializable
    private data class NotifInsert(
        @SerialName("usuario_id") val usuarioId: String,
        val titulo: String,
        val mensaje: String
    )

    suspend fun crear(usuarioId: String, titulo: String, mensaje: String): Result<Unit> = runCatching {
        client.postgrest["notificaciones"].insert(NotifInsert(usuarioId, titulo, mensaje))
    }

    suspend fun getMias(usuarioId: String): Result<List<Notificacion>> = runCatching {
        client.postgrest["notificaciones"].select {
            filter { eq("usuario_id", usuarioId) }
            order("fecha_envio", Order.DESCENDING)
        }.decodeList<Notificacion>()
    }

    suspend fun marcarLeida(id: Long): Result<Unit> = runCatching {
        client.postgrest["notificaciones"].update({ set("leida", true) }) {
            filter { eq("id", id) }
        }
    }
}
