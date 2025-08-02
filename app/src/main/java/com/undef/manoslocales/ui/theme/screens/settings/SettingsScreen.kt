package com.undef.manoslocales.ui.theme.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.undef.manoslocales.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ViewModel state
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState(initial = true)
    val isLoading by settingsViewModel.isLoading.collectAsState()
    val errorMessage by settingsViewModel.errorMessage.collectAsState()
    val logoutSuccess by settingsViewModel.logoutSuccess.collectAsState()
    val user by settingsViewModel.user.collectAsState()

    // Local UI state for password change
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }

    // Feedback when notifications toggle
    LaunchedEffect(notificationsEnabled) {
        snackbarHostState.showSnackbar(
            message = if (notificationsEnabled) "Notificaciones activadas" else "Notificaciones desactivadas",
            withDismissAction = true
        )
    }

    // Navigate away on logout
    LaunchedEffect(logoutSuccess) {
        if (logoutSuccess) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cuenta
                Text(text = "Cuenta", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                if (user != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Ajustá las propiedades según tu modelo User: aquí se asume `username`, `email`, `tel`
                            Text(
                                text = user?.let { it.username ?: "Nombre no disponible" } ?: "Nombre no disponible",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user?.email ?: "Email no disponible",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            user?.tel?.let { tel ->
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = tel, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    Text("No se pudo cargar la información del usuario", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Preferencias
                Text(text = "Preferencias", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notificaciones")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            settingsViewModel.setNotificationsEnabled(enabled)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seguridad: cambiar contraseña
                Text(text = "Seguridad", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cambiar contraseña")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cerrar sesión
                Button(
                    onClick = { settingsViewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
                }

                // Carga y errores generales
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = msg, color = MaterialTheme.colorScheme.error)
                }
            }

            // Diálogo de cambio de contraseña
            if (showChangePasswordDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showChangePasswordDialog = false
                        newPassword = ""
                        confirmPassword = ""
                        passwordError = null
                    },
                    title = { Text("Cambiar contraseña") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nueva contraseña") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        val icon = if (showPassword)
                                            Icons.Default.VisibilityOff
                                        else
                                            Icons.Default.Visibility
                                        Icon(icon, contentDescription = if (showPassword) "Ocultar" else "Mostrar")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar contraseña") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            passwordError?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            passwordError = null
                            if (newPassword.isBlank() || confirmPassword.isBlank()) {
                                passwordError = "Ambos campos son requeridos"
                                return@TextButton
                            }
                            if (newPassword != confirmPassword) {
                                passwordError = "Las contraseñas no coinciden"
                                return@TextButton
                            }
                            // Llamada al ViewModel para cambiar contraseña
                            scope.launch {
                                try {
                                    // Asegurate que `changePassword` exista y devuelva Boolean en tu ViewModel
                                    val success = settingsViewModel.changePassword(newPassword)
                                    if (success) {
                                        snackbarHostState.showSnackbar("Contraseña cambiada con éxito")
                                        showChangePasswordDialog = false
                                        newPassword = ""
                                        confirmPassword = ""
                                    } else {
                                        passwordError = "Error al cambiar contraseña"
                                    }
                                } catch (e: Exception) {
                                    passwordError = "Error: ${e.localizedMessage}"
                                }
                            }
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showChangePasswordDialog = false
                            passwordError = null
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
