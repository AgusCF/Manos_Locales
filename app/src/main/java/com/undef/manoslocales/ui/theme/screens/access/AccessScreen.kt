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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.undef.manoslocales.R
import com.undef.manoslocales.data.model.GoogleUser
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.AuthViewModel
import com.undef.manoslocales.viewmodel.SettingsViewModel
import com.undef.manoslocales.viewmodel.UserViewModel
import kotlinx.coroutines.launch

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
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    var isProcessingLogin by rememberSaveable { mutableStateOf(false) }

    // ✅ VERIFICAR ESTADO DE AUTENTICACIÓN AL INICIAR
    LaunchedEffect(Unit) {
        Log.d("DebugDev", "🔍 AccessScreen: Verificando autenticación inicial...")
        authViewModel.refresh()
    }

    // ✅ NAVEGAR SI ESTÁ LOGUEADO
    var hasNavigated by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && !hasNavigated) {
            hasNavigated = true
            Log.d("DebugDev", "✅ Navegando a Feed desde AccessScreen")
            navController.navigate(Screen.Feed.route) {
                popUpTo(Screen.Access.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // ✅ DEBUG: Verificar configuración al cargar la pantalla
    LaunchedEffect(Unit) {
        Log.d("DebugDev", "🔍 ===== CONFIGURACIÓN GOOGLE SIGN-IN =====")
        Log.d("DebugDev", "📦 Package: ${context.packageName}")
        Log.d("DebugDev", "🔑 Web Client ID: ${context.getString(R.string.default_web_client_id)}")

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        Log.d("DebugDev", "📱 Google Play Services: $resultCode (0=SUCCESS)")

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e("DebugDev", "❌ Google Play Services no disponible")
        } else {
            Log.d("DebugDev", "✅ Google Play Services disponible")
        }

        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        Log.d("DebugDev", "👤 Última cuenta: ${lastAccount?.email ?: "Ninguna"}")
        Log.d("DebugDev", "🔍 ===== FIN CONFIGURACIÓN =====")
    }

    // ✅ Configuración Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("537032644616-4j3uri07rcdfl2p2m0j9a0nr6df6ap9v.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // 🔒 Evitar sesión automática tras reinstalar: forzar signOut al abrir Access
    LaunchedEffect(Unit) {
        try {
            googleSignInClient.signOut()
            Log.d("DebugDev", "🔒 GoogleSignIn: signOut() ejecutado para evitar auto login")
        } catch (e: Exception) {
            Log.w("DebugDev", "⚠️ No se pudo ejecutar signOut en inicio: ${e.message}")
        }
    }

    // ✅ Lanzador para resultado de Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("DebugDev", "🎯 ===== LAUNCHER EJECUTADO =====")
        Log.d("DebugDev", "📊 resultCode: ${result.resultCode}")
        Log.d("DebugDev", "📦 data: ${result.data}")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("DebugDev", "✅ RESULT_OK - Procesando cuenta Google...")
            isProcessingLogin = true

            scope.launch {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    if (task.isSuccessful) {
                        val account = task.getResult(ApiException::class.java)
                        val googleUser = GoogleUser(
                            email = account?.email ?: "",
                            displayName = account?.displayName,
                            id = account?.id
                        )

                        Log.d("DebugDev", "✅ Cuenta obtenida: ${googleUser.email}")
                        authViewModel.signInWithGoogle(googleUser, navController)
                    } else {
                        val exception = task.exception
                        Log.e("DebugDev", "❌ Task falló: ${exception?.message}", exception)
                        snackbarHostState.showSnackbar("Error al obtener cuenta de Google")
                    }
                } catch (e: ApiException) {
                    Log.e("DebugDev", "❌ ApiException: código ${e.statusCode}", e)
                    snackbarHostState.showSnackbar("Error de autenticación: ${e.message}")
                } catch (e: Exception) {
                    Log.e("DebugDev", "❌ Error general: ${e.message}", e)
                    snackbarHostState.showSnackbar("Error inesperado: ${e.message}")
                } finally {
                    isProcessingLogin = false
                    Log.d("DebugDev", "🔚 Procesamiento finalizado")
                }
            }
        } else {
            Log.d("DebugDev", "❌ RESULT_CANCELED o ERROR - Código: ${result.resultCode}")
            scope.launch {
                snackbarHostState.showSnackbar("Inicio de sesión cancelado")
            }
        }

        Log.d("DebugDev", "🎯 ===== FIN LAUNCHER =====")
    }

    // ✅ UI
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

                Button(
                    onClick = {
                        Log.d("DebugDev", "🔄 ===== INICIANDO GOOGLE SIGN-IN =====")
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                        Log.d("DebugDev", "✅ Actividad lanzada")
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    enabled = !isLoading && !isProcessingLogin
                ) {
                    if (isLoading || isProcessingLogin) {
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