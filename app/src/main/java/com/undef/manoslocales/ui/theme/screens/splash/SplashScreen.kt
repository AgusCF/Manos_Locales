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
import com.undef.manoslocales.ui.theme.Screen//Recordar

@Composable
fun SplashScreen(navController: NavController) {
    // Espera 2 segundos y navega al login
    LaunchedEffect(true) {
        delay(2000)
        navController.navigate(Screen.Login.route) {
            popUpTo(0)
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