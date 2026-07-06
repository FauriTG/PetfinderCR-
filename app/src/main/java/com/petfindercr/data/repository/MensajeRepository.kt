package com.petfindercr.data.repository

import com.petfindercr.data.model.Mensaje
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MensajeRepository @Inject constructor(private val client: SupabaseClient) {

    /** DTO de inserción sin id/fecha (los genera la base de datos). */
    @Serializable
    private data class MensajeInsert(
        @SerialName("emisor_id") val emisorId: String,
        @SerialName("receptor_id") val receptorId: String,
        val mensaje: String
    )

    /**
     * Devuelve la conversación entre el usuario actual y [otroId].
     * El RLS ya limita a los mensajes donde el usuario es emisor o receptor,
     * así que basta filtrar por el otro participante.
     */
    suspend fun getConversacion(otroId: String): Result<List<Mensaje>> = runCatching {
        client.postgrest["mensajes"].select {
            filter {
                or {
                    eq("emisor_id", otroId)
                    eq("receptor_id", otroId)
                }
            }
            order("fecha_envio", Order.ASCENDING)
        }.decodeList<Mensaje>()
    }

    suspend fun enviar(emisorId: String, receptorId: String, texto: String): Result<Unit> = runCatching {
        client.postgrest["mensajes"].insert(
            MensajeInsert(emisorId = emisorId, receptorId = receptorId, mensaje = texto)
        )
    }

    /** Todos los mensajes del usuario actual (el RLS ya los limita a los suyos). */
    suspend fun getTodos(): Result<List<Mensaje>> = runCatching {
        client.postgrest["mensajes"].select {
            order("fecha_envio", Order.DESCENDING)
        }.decodeList<Mensaje>()
    }
}
