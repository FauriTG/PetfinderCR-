package com.petfindercr.data.repository

import com.petfindercr.data.model.SolicitudEstado
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SolicitudRepository @Inject constructor(private val client: SupabaseClient) {

    private val joinQuery = Columns.raw("*, reportes(id, titulo)")

    @Serializable
    private data class SolicitudInsert(
        @SerialName("reporte_id") val reporteId: Long,
        @SerialName("solicitante_id") val solicitanteId: String,
        @SerialName("dueno_id") val duenoId: String,
        @SerialName("estado_solicitado") val estadoSolicitado: String
    )

    suspend fun crear(reporteId: Long, solicitanteId: String, duenoId: String, estadoSolicitado: String): Result<Unit> = runCatching {
        client.postgrest["solicitudes_estado"].insert(
            SolicitudInsert(reporteId, solicitanteId, duenoId, estadoSolicitado)
        )
    }

    /** Solicitudes pendientes dirigidas al dueño actual. */
    suspend fun getPendientesParaDueno(duenoId: String): Result<List<SolicitudEstado>> = runCatching {
        client.postgrest["solicitudes_estado"].select(joinQuery) {
            filter {
                eq("dueno_id", duenoId)
                eq("estado", "PENDIENTE")
            }
            order("fecha", Order.DESCENDING)
        }.decodeList<SolicitudEstado>()
    }

    suspend fun responder(solicitudId: Long, aprobada: Boolean): Result<Unit> = runCatching {
        client.postgrest["solicitudes_estado"].update(
            { set("estado", if (aprobada) "APROBADA" else "RECHAZADA") }
        ) {
            filter { eq("id", solicitudId) }
        }
    }
}
