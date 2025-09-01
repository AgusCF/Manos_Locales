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

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var alreadyNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (alreadyNavigated) return@LaunchedEffect
        alreadyNavigated = true
        // breve espera para que se vea splash
        delay(1500)
        if (isLoggedIn) {
            navController.navigate(Screen.Feed.route) {
                launchSingleTop = true
                popUpTo(0) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                launchSingleTop = true
                popUpTo(0) { inclusive = true }
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
            Text("Manos Locales", style = MaterialTheme.typography.titleLarge)
        }
    }
}