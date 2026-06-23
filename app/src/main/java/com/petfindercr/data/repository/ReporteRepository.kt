package com.petfindercr.data.repository

import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.ImagenReporte
import com.petfindercr.data.model.Reporte
import com.petfindercr.data.model.TipoMascota
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReporteRepository @Inject constructor(private val client: SupabaseClient) {

    private val joinQuery = Columns.raw(
        "*, perfiles(id, nombre, telefono, foto_perfil), tipos_mascota(id, nombre), imagenes_reporte(id, url_imagen)"
    )

    suspend fun getReportes(estado: EstadoReporte? = null): Result<List<Reporte>> = runCatching {
        client.postgrest["reportes"].select(joinQuery) {
            order("fecha_reporte", Order.DESCENDING)
            if (estado != null) filter { eq("estado", estado.name) }
        }.decodeList<Reporte>()
    }

    suspend fun getReportesByUser(userId: String): Result<List<Reporte>> = runCatching {
        client.postgrest["reportes"].select(joinQuery) {
            filter { eq("usuario_id", userId) }
            order("fecha_reporte", Order.DESCENDING)
        }.decodeList<Reporte>()
    }

    suspend fun getReporte(id: Long): Result<Reporte> = runCatching {
        client.postgrest["reportes"].select(joinQuery) {
            filter { eq("id", id) }
        }.decodeSingle<Reporte>()
    }

    suspend fun createReporte(reporte: Reporte): Result<Reporte> = runCatching {
        client.postgrest["reportes"].insert(reporte) {
            select()
        }.decodeSingle<Reporte>()
    }

    suspend fun updateReporte(reporte: Reporte): Result<Unit> = runCatching {
        client.postgrest["reportes"].update(
            {
                set("titulo", reporte.titulo)
                set("descripcion", reporte.descripcion)
                set("color", reporte.color)
                set("raza", reporte.raza)
                set("estado", reporte.estado)
                set("latitud", reporte.latitud)
                set("longitud", reporte.longitud)
                set("direccion", reporte.direccion)
                set("recompensa", reporte.recompensa)
                set("monto_recompensa", reporte.montoRecompensa)
                set("tipo_mascota_id", reporte.tipoMascotaId)
                set("fecha_evento", reporte.fechaEvento)
            }
        ) {
            filter { eq("id", reporte.id) }
        }
    }

    suspend fun deleteReporte(id: Long): Result<Unit> = runCatching {
        client.postgrest["reportes"].delete {
            filter { eq("id", id) }
        }
    }

    suspend fun updateEstado(id: Long, estado: EstadoReporte): Result<Unit> = runCatching {
        client.postgrest["reportes"].update(
            { set("estado", estado.name) }
        ) {
            filter { eq("id", id) }
        }
    }

    suspend fun addImagen(imagen: ImagenReporte): Result<Unit> = runCatching {
        client.postgrest["imagenes_reporte"].insert(imagen)
    }

    suspend fun deleteImagen(id: Long): Result<Unit> = runCatching {
        client.postgrest["imagenes_reporte"].delete {
            filter { eq("id", id) }
        }
    }

    suspend fun getTiposMascota(): Result<List<TipoMascota>> = runCatching {
        client.postgrest["tipos_mascota"].select(Columns.ALL).decodeList<TipoMascota>()
    }

    suspend fun getReportesNearby(lat: Double, lon: Double, radiusKm: Double = 10.0): Result<List<Reporte>> = runCatching {
        // Filter roughly by bounding box (1 degree ≈ 111 km)
        val delta = radiusKm / 111.0
        client.postgrest["reportes"].select(joinQuery) {
            filter {
                gte("latitud", lat - delta)
                lte("latitud", lat + delta)
                gte("longitud", lon - delta)
                lte("longitud", lon + delta)
            }
            order("fecha_reporte", Order.DESCENDING)
        }.decodeList<Reporte>()
    }
}
