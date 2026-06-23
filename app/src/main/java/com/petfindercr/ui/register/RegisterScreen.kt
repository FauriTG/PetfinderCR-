package com.petfindercr.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) { if (state.success) onRegisterSuccess() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(8.dp))
        Text("Crear cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.nombre, onValueChange = viewModel::onNombreChange,
            label = { Text("Nombre completo") }, leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.email, onValueChange = viewModel::onEmailChange,
            label = { Text("Correo electrónico") }, leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.telefono, onValueChange = viewModel::onTelefonoChange,
            label = { Text("Teléfono (opcional)") }, leadingIcon = { Icon(Icons.Default.Phone, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password, onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.confirmPassword, onValueChange = viewModel::onConfirmPasswordChange,
            label = { Text("Confirmar contraseña") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = viewModel::register,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Registrarse")
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?")
            TextButton(onClick = onNavigateToLogin) { Text("Inicia sesión") }
        }

        state.error?.let { error ->
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
