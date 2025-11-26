package com.undef.manoslocales.ui.theme.screens.access

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.undef.manoslocales.R
import com.undef.manoslocales.data.model.GoogleUser
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.AuthViewModel
import com.undef.manoslocales.viewmodel.SettingsViewModel
import com.undef.manoslocales.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AccessScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Configuración de Google Sign In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Estados
    var isProcessingLogin by rememberSaveable { mutableStateOf(false) }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // Efecto para navegación cuando el login es exitoso
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            Log.d("DebugDev", "🔄 Navegando a Feed después de login exitoso")
            navController.navigate(Screen.Feed.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && !isProcessingLogin) {
            isProcessingLogin = true

            scope.launch {
                try {
                    Log.d("DebugDev", "🔍 Procesando resultado de Google Sign-In...")

                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)

                    val googleUser = GoogleUser(
                        email = account?.email ?: "",
                        displayName = account?.displayName,
                        id = account?.id
                    )

                    Log.d("DebugDev", "📧 Email de Google: ${googleUser.email}")

                    // Usar el ViewModel en lugar de llamar al repository directamente
                    authViewModel.signInWithGoogle(googleUser)

                } catch (e: Exception) {
                    Log.e("DebugDev", "❌ Error en Google Sign-In: ${e.message}", e)
                    snackbarHostState.showSnackbar("Error al iniciar sesión con Google: ${e.message}")
                } finally {
                    isProcessingLogin = false
                }
            }
        }
    }

    // Mostrar loading mientras procesa
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // UI normal
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(160.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Manos Locales", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate(Screen.Login.route) },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Iniciar sesión")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(Screen.Register.route) },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Registrarse")
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        Log.d("DebugDev", "🔄 Iniciando flujo Google Sign-In...")
                        // Forzar mostrar el selector de cuentas siempre
                        googleSignInClient.signOut().addOnCompleteListener {
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Procesando...")
                    } else {
                        Text("Iniciar sesión con Google")
                    }
                }
            }
        }
    }
}