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
    authViewModel: AuthViewModel,    
    settingsViewModel: SettingsViewModel  
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userRepository = settingsViewModel.repository
    
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
    var hasNavigated by rememberSaveable { mutableStateOf(false) }

    // Efecto para manejar la navegación cuando el usuario está logueado
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && !hasNavigated) {
            hasNavigated = true
            navController.navigate(Screen.Feed.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (!isProcessingLogin) {
            isProcessingLogin = true
            
            scope.launch {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)
                    val email = account?.email
                    val nombre = account?.displayName ?: "GoogleUser"

                    if (email != null) {
                        withContext(Dispatchers.IO) {
                            delay(500)
                            val user = userRepository.getUserByEmail(email)
                            
                            if (user != null) {
                                userRepository.saveGoogleSession(user.id ?: -1)
                            } else {
                                val created = userRepository.createUserFromGoogle(nombre, email)
                                if (created != null) {
                                    userRepository.saveGoogleSession(created.id ?: -1)
                                }
                            }
                            
                            delay(500)
                            withContext(Dispatchers.Main) {
                                authViewModel.refresh()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DebugDev", "Error en login Google: ${e.message}")
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Error al iniciar sesión con Google")
                    }
                } finally {
                    isProcessingLogin = false
                }
            }
        }
    }

    // UI
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
                        // Forzar mostrar el selector de cuentas siempre
                        googleSignInClient.signOut().addOnCompleteListener {
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Iniciar sesión con Google")
                }
            }
        }
    }
}