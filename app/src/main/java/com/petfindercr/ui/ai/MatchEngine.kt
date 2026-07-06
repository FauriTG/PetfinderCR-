package com.petfindercr.ui.ai

import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.Reporte
import java.text.Normalizer
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Una posible coincidencia entre una mascota perdida del usuario y una encontrada por otro. */
data class PetMatch(
    val perdida: Reporte,
    val encontrada: Reporte,
    val score: Int,
    val razones: List<String>,
    val distanciaKm: Double?
)

/**
 * Motor de coincidencias por reglas ("IA" heurística).
 * Compara las mascotas PERDIDAS del usuario contra las ENCONTRADAS de otros usuarios
 * y calcula un porcentaje de similitud (0-100) según:
 *   tipo (25) · raza (25) · color (20) · cercanía (20) · fecha (10)
 */
object MatchEngine {

    private const val UMBRAL_MINIMO = 30

    fun findMatches(userId: String, todos: List<Reporte>): List<PetMatch> {
        val misPerdidas = todos.filter {
            it.usuarioId == userId && it.estado == EstadoReporte.PERDIDA.name
        }
        // Todas las mascotas ENCONTRADAS (incluye las propias, útil para probar
        // y por si la persona reportó por separado)
        val encontradas = todos.filter {
            it.estado == EstadoReporte.ENCONTRADA.name
        }

        val matches = mutableListOf<PetMatch>()
        for (perdida in misPerdidas) {
            for (encontrada in encontradas) {
                val resultado = puntuar(perdida, encontrada)
                if (resultado.score >= UMBRAL_MINIMO) {
                    matches += PetMatch(
                        perdida = perdida,
                        encontrada = encontrada,
                        score = resultado.score.coerceAtMost(100),
                        razones = resultado.razones,
                        distanciaKm = resultado.distanciaKm
                    )
                }
            }
        }
        return matches.sortedByDescending { it.score }
    }

    private data class Resultado(val score: Int, val razones: List<String>, val distanciaKm: Double?)

    private fun puntuar(a: Reporte, b: Reporte): Resultado {
        var score = 0
        val razones = mutableListOf<String>()

        // Tipo de mascota
        if (a.tipoMascotaId != null && a.tipoMascotaId == b.tipoMascotaId) {
            score += 25
            razones += "Mismo tipo de animal"
        }

        // Raza
        if (coincideTexto(a.raza, b.raza)) {
            score += 25
            razones += "Raza similar"
        }

        // Color
        if (coincideTexto(a.color, b.color)) {
            score += 20
            razones += "Color parecido"
        }

        // Cercanía geográfica
        val dist = distanciaKm(a, b)
        if (dist != null) {
            when {
                dist <= 3.0 -> { score += 20; razones += "Muy cerca (${fmtKm(dist)})" }
                dist <= 10.0 -> { score += 12; razones += "En la misma zona (${fmtKm(dist)})" }
                dist <= 25.0 -> { score += 6; razones += "Zona cercana (${fmtKm(dist)})" }
            }
        }

        // Fecha del evento
        val dias = diasEntre(a.fechaEvento, b.fechaEvento)
        if (dias != null && dias <= 14) {
            score += 10
            razones += "Fechas cercanas"
        }

        return Resultado(score, razones, dist)
    }

    // ── Comparación de texto (ignora mayúsculas y tildes) ──
    private fun coincideTexto(x: String?, y: String?): Boolean {
        val a = normalizar(x) ?: return false
        val b = normalizar(y) ?: return false
        if (a == b) return true
        if (a.contains(b) || b.contains(a)) return true
        // Solapamiento de palabras
        val palabrasA = a.split(" ").filter { it.length > 2 }.toSet()
        val palabrasB = b.split(" ").filter { it.length > 2 }.toSet()
        return palabrasA.intersect(palabrasB).isNotEmpty()
    }

    private fun normalizar(s: String?): String? {
        val t = s?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: return null
        return Normalizer.normalize(t, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
    }

    // ── Distancia haversine en km ──
    private fun distanciaKm(a: Reporte, b: Reporte): Double? {
        val lat1 = a.latitud ?: return null
        val lon1 = a.longitud ?: return null
        val lat2 = b.latitud ?: return null
        val lon2 = b.longitud ?: return null
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(h), sqrt(1 - h))
    }

    private fun fmtKm(km: Double): String =
        if (km < 1.0) "${(km * 1000).roundToInt()} m" else "${(km * 10).roundToInt() / 10.0} km"

    // ── Días entre dos fechas (formato "yyyy-MM-dd..." o null) ──
    private fun diasEntre(f1: String?, f2: String?): Long? {
        val d1 = parseFecha(f1) ?: return null
        val d2 = parseFecha(f2) ?: return null
        return abs(d1.toEpochDay() - d2.toEpochDay())
    }

    private fun parseFecha(s: String?): LocalDate? {
        val raw = s?.takeIf { it.length >= 10 } ?: return null
        return try {
            LocalDate.parse(raw.substring(0, 10))
        } catch (_: Exception) {
            null
        }
    }
}
