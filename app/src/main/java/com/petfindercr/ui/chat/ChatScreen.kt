package com.petfindercr.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petfindercr.data.model.Mensaje

private val BgColor = Color(0xFFF8F9FC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receptorId: String,
    receptorNombre: String,
    onOpenProfile: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(receptorId) { viewModel.start(receptorId) }

    // Auto-scroll al último mensaje
    LaunchedEffect(state.mensajes.size) {
        if (state.mensajes.isNotEmpty()) listState.animateScrollToItem(state.mensajes.size - 1)
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        receptorNombre.ifBlank { "Chat" },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onOpenProfile(receptorId) }
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { onOpenProfile(receptorId) }) {
                        Icon(Icons.Default.AccountCircle, "Ver perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        bottomBar = {
            MessageInput(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                onSend = viewModel::enviar,
                sending = state.isSending
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.mensajes.isEmpty() -> Column(
                    Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👋", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Inicia la conversación", fontWeight = FontWeight.Bold, color = Ink)
                    Text(
                        "Escribe un mensaje para coordinar sobre la mascota.",
                        fontSize = 13.sp, color = Muted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.mensajes) { msg ->
                        MessageBubble(msg = msg, esMio = msg.emisorId == viewModel.currentUserId)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: Mensaje, esMio: Boolean) {
    val bubbleColor = if (esMio) MaterialTheme.colorScheme.primary else Color.White
    val textColor = if (esMio) Color.White else Ink
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (esMio) 16.dp else 4.dp,
                        bottomEnd = if (esMio) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(msg.mensaje, color = textColor, fontSize = 15.sp)
        }
    }
}

@Composable
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    sending: Boolean
) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje…") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
            )
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !sending
            ) {
                if (sending) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                else Icon(Icons.Default.Send, "Enviar")
            }
        }
    }
}
