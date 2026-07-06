package com.petfindercr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val id: String = "",
    val nombre: String = "",
    val telefono: String? = null,
    @SerialName("foto_perfil") val fotoPerfil: String? = null,
    val descripcion: String? = null,
    val sexo: String? = null,          // "Hombre", "Mujer" u otro
    val procedencia: String? = null,   // de dónde es (opcional)
    @SerialName("fecha_registro") val fechaRegistro: String? = null
)

@Serializable
data class TipoMascota(
    val id: Long = 0,
    val nombre: String = ""
)

@Serializable
data class Reporte(
    val id: Long = 0,
    @SerialName("usuario_id") val usuarioId: String? = null,
    @SerialName("tipo_mascota_id") val tipoMascotaId: Long? = null,
    val titulo: String = "",
    val descripcion: String? = null,
    val color: String? = null,
    val raza: String? = null,
    @SerialName("fecha_reporte") val fechaReporte: String? = null,
    @SerialName("fecha_evento") val fechaEvento: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val direccion: String? = null,
    val estado: String = EstadoReporte.PERDIDA.name,
    val recompensa: Boolean = false,
    @SerialName("monto_recompensa") val montoRecompensa: Double? = null,
    // Joined fields
    val perfiles: Perfil? = null,
    @SerialName("tipos_mascota") val tiposMascota: TipoMascota? = null,
    @SerialName("imagenes_reporte") val imagenesReporte: List<ImagenReporte>? = null
)

@Serializable
data class ImagenReporte(
    val id: Long = 0,
    @SerialName("reporte_id") val reporteId: Long? = null,
    @SerialName("url_imagen") val urlImagen: String = "",
    @SerialName("fecha_subida") val fechaSubida: String? = null
)

@Serializable
data class Mensaje(
    val id: Long = 0,
    @SerialName("emisor_id") val emisorId: String? = null,
    @SerialName("receptor_id") val receptorId: String? = null,
    val mensaje: String = "",
    @SerialName("fecha_envio") val fechaEnvio: String? = null
)

@Serializable
data class SolicitudEstado(
    val id: Long = 0,
    @SerialName("reporte_id") val reporteId: Long? = null,
    @SerialName("solicitante_id") val solicitanteId: String? = null,
    @SerialName("dueno_id") val duenoId: String? = null,
    @SerialName("estado_solicitado") val estadoSolicitado: String = "",
    val estado: String = "PENDIENTE",   // PENDIENTE / APROBADA / RECHAZADA
    val fecha: String? = null,
    // Joined
    val reportes: Reporte? = null
)

@Serializable
data class Notificacion(
    val id: Long = 0,
    @SerialName("usuario_id") val usuarioId: String? = null,
    val titulo: String = "",
    val mensaje: String = "",
    val leida: Boolean = false,
    @SerialName("fecha_envio") val fechaEnvio: String? = null
)

enum class EstadoReporte {
    PERDIDA,
    ENCONTRADA,
    RECUPERADA
}
