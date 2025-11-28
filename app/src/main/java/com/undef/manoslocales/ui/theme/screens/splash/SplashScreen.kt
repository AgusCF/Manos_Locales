package com.undef.manoslocales.ui.theme.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.undef.manoslocales.R
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isInitialized by authViewModel.isInitialized.collectAsState()
    var hasCheckedAuth by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    // ✅ INICIAR verificación de autenticación solo una vez
    LaunchedEffect(Unit) {
        if (!hasCheckedAuth) {
            Log.d("DebugDev", "🔄 SplashScreen: Iniciando checkAuthStatus...")
            authViewModel.checkAuthStatus()
            hasCheckedAuth = true
        }
    }

    // ✅ NAVEGAR cuando esté listo - CON DELAY ADICIONAL PARA VERIFICACIÓN COMPLETA
    LaunchedEffect(isInitialized, isLoggedIn) {
        if (isInitialized && !hasNavigated) {
            Log.d("DebugDev", "🎯 SplashScreen: isInitialized=$isInitialized, isLoggedIn=$isLoggedIn")

            // ⏰ DELAY ADICIONAL para asegurar que la verificación esté completa
            delay(2500) // Aumentado a 2,5 segundos para dar tiempo a la verificación

            hasNavigated = true
            val destination = if (isLoggedIn) {
                Log.d("DebugDev", "➡️ Navegando a Feed (usuario logueado)")
                Screen.Feed.route
            } else {
                Log.d("DebugDev", "➡️ Navegando a Access (usuario NO logueado)")
                Screen.Access.route
            }

            navController.navigate(destination) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // ✅ FALLBACK: Si después de 5 segundos no se inicializa, navegar a Access
    LaunchedEffect(Unit) {
        delay(5000) // Aumentado timeout a 5 segundos
        if (!hasNavigated) {
            Log.w("DebugDev", "⏰ SplashScreen: Timeout, navegando a Access")
            hasNavigated = true
            navController.navigate(Screen.Access.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bienvenidos a ", style = MaterialTheme.typography.titleLarge)
            Text("Manos Locales", style = MaterialTheme.typography.titleLarge)

            // ⏰ Indicador de carga adicional
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}