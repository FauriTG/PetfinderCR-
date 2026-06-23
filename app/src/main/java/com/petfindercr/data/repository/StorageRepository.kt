package com.petfindercr.data.repository

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val client: SupabaseClient,
    private val context: Context
) {
    private val reportesBucket = "reportes"
    private val perfilesBucket = "perfiles"

    suspend fun uploadReporteImage(uri: Uri): Result<String> = runCatching {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: error("No se pudo leer la imagen")
        val filename = "reporte_${UUID.randomUUID()}.jpg"
        client.storage[reportesBucket].upload(filename, bytes) { upsert = true }
        client.storage[reportesBucket].publicUrl(filename)
    }

    suspend fun uploadProfileImage(uri: Uri, userId: String): Result<String> = runCatching {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: error("No se pudo leer la imagen")
        val filename = "perfil_$userId.jpg"
        client.storage[perfilesBucket].upload(filename, bytes) { upsert = true }
        client.storage[perfilesBucket].publicUrl(filename)
    }

    suspend fun deleteReporteImage(url: String): Result<Unit> = runCatching {
        val filename = url.substringAfterLast("/")
        client.storage[reportesBucket].delete(listOf(filename))
    }
}
