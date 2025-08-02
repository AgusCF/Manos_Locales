package com.undef.manoslocales.ui.theme.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.UserViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.lint.kotlin.metadata.Visibility

@Composable
fun LoginScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) } // <-- estado para mostrar/ocultar
    var showError by remember { mutableStateOf(false) }

    val loginResult by userViewModel.loginSuccess.collectAsState()

    LaunchedEffect(loginResult) {
        if (loginResult == true) {
            navController.navigate(Screen.Feed.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else if (loginResult == false) {
            showError = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (showPassword)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(imageVector = image, contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                showError = false
                userViewModel.loginUser(email, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        if (showError) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Email o contraseña incorrecta", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = {
            navController.navigate(Screen.Register.route)
        }) {
            Text("¿No tenés cuenta? Registrate")
        }
    }
}