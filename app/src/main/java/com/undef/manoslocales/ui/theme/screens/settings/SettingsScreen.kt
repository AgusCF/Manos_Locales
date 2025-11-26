package com.undef.manoslocales.ui.theme.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.AuthViewModel
import com.undef.manoslocales.viewmodel.SettingsViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState(initial = true)
    val isLoading by settingsViewModel.isLoading.collectAsState()
    val errorMessage by settingsViewModel.errorMessage.collectAsState()
    val logoutSuccess by settingsViewModel.logoutSuccess.collectAsState()
    val user by settingsViewModel.user.collectAsState()
    val changePasswordResult by settingsViewModel.changePasswordResult.collectAsState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }

    // Cargar usuario si corresponde (solo una vez)
    LaunchedEffect(Unit) {
        settingsViewModel.ensureUserLoaded()
    }

    // Logout: sincroniza con AuthViewModel y navega
    LaunchedEffect(logoutSuccess) {
        if (logoutSuccess) {
            authViewModel.logout()
            snackbarHostState.showSnackbar("Sesión cerrada correctamente")
            navController.navigate(Screen.Access.route) {
                launchSingleTop = true
                popUpTo(0) { inclusive = true }
            }
            settingsViewModel.clearLogoutFlag()
        }
    }

    // Cambio de contraseña
    LaunchedEffect(changePasswordResult) {
        changePasswordResult?.let { result ->
            if (result.isSuccess) {
                snackbarHostState.showSnackbar("Contraseña cambiada con éxito")
                showChangePasswordDialog = false
                newPassword = ""
                confirmPassword = ""
                passwordError = null
            } else {
                passwordError = result.exceptionOrNull()?.localizedMessage ?: "Error al cambiar contraseña"
            }
            settingsViewModel.clearChangePasswordResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                // Cuenta editable
                Text(text = "Cuenta", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                if (user != null) {
                    var isEditing by remember { mutableStateOf(false) }
                    var editedUsername by remember { mutableStateOf(user!!.username) }
                    var editedTel by remember { mutableStateOf(user!!.tel) }
                    var fieldError by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editedUsername,
                                    onValueChange = { editedUsername = it },
                                    label = { Text("Nombre de usuario") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Email", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = user!!.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editedTel,
                                    onValueChange = { editedTel = it },
                                    label = { Text("Teléfono") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                fieldError?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            fieldError = when {
                                                editedUsername.isBlank() -> "El nombre de usuario no puede estar vacío"
                                                else -> null
                                            }
                                            if (fieldError == null) {
                                                val updatedUser = user!!.copy(
                                                    username = editedUsername,
                                                    tel = editedTel
                                                )
                                                settingsViewModel.updateUser(updatedUser)
                                                isEditing = false
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Guardar")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            editedUsername = user!!.username
                                            editedTel = user!!.tel
                                            fieldError = null
                                            isEditing = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancelar")
                                    }
                                }
                            } else {
                                Text(
                                    text = user!!.username.ifBlank { "Nombre no disponible" },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user!!.email,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (user!!.tel.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = user!!.tel, style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { isEditing = true }) {
                                    Text("Editar datos")
                                }
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

                // Seguridad
                Text(text = "Seguridad", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showChangePasswordDialog = true }, modifier = Modifier.fillMaxWidth()) {
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

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = msg, color = MaterialTheme.colorScheme.error)
                }
            }

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
                                        val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
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
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
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
                            scope.launch {
                                settingsViewModel.changePassword(newPassword)
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