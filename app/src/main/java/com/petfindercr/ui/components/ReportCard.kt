package com.petfindercr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.Reporte

@Composable
fun ReportCard(reporte: Reporte, onClick: () -> Unit) {
    val firstImage = reporte.imagenesReporte?.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (firstImage != null) {
                AsyncImage(
                    model = firstImage.urlImagen,
                    contentDescription = reporte.titulo,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxSize()) {}
                    Icon(Icons.Default.Pets, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EstadoBadge(reporte.estado)
                    Spacer(Modifier.width(8.dp))
                    reporte.tiposMascota?.nombre?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(reporte.titulo, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                reporte.descripcion?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                reporte.direccion?.let {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(it, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoBadge(estado: String) {
    val (color, text) = when (estado) {
        EstadoReporte.PERDIDA.name -> MaterialTheme.colorScheme.error to "PERDIDA"
        EstadoReporte.ENCONTRADA.name -> MaterialTheme.colorScheme.tertiary to "ENCONTRADA"
        EstadoReporte.RECUPERADA.name -> MaterialTheme.colorScheme.primary to "RECUPERADA"
        else -> MaterialTheme.colorScheme.secondary to estado
    }
    Surface(color = color, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
