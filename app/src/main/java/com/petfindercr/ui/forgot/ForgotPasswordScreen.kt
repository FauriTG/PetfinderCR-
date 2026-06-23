package com.petfindercr.ui.forgot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = state.email, onValueChange = viewModel::onEmailChange,
                label = { Text("Correo electrónico") }, leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::sendReset,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Enviar enlace")
            }
            state.message?.let {
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(it, modifier = Modifier.padding(12.dp))
                }
            }
            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}
