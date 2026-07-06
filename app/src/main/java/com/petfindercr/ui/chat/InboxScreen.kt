package com.petfindercr.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

private val BgColor = Color(0xFFF8F9FC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onOpenChat: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Mensajes", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.conversaciones.isEmpty() -> Column(
                    Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(96.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(46.dp), tint = MaterialTheme.colorScheme.primary) }
                    Spacer(Modifier.height(18.dp))
                    Text("Aún no tienes mensajes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Cuando contactes a alguien desde un reporte, la conversación aparecerá aquí.",
                        fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center, lineHeight = 20.sp
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.conversaciones) { conv ->
                        ConversationItem(conv = conv, onClick = { onOpenChat(conv.otroId, conv.otroNombre) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(conv: Conversacion, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar
        Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            if (conv.otroFoto != null) {
                AsyncImage(model = conv.otroFoto, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(conv.otroNombre.firstOrNull()?.uppercase() ?: "U", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(conv.otroNombre, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(conv.ultimoMensaje, fontSize = 13.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    Divider(modifier = Modifier.padding(start = 86.dp), color = Color(0xFFF1F5F9))
}
