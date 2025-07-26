package com.undef.manoslocales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType

import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.undef.manoslocales.data.sampleProducts
import com.undef.manoslocales.ui.theme.ManosLocalesTheme
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.ui.theme.screens.detail.ProductDetailScreen
import com.undef.manoslocales.ui.theme.screens.feed.FeedScreen
import com.undef.manoslocales.ui.theme.screens.login.LoginScreen
import com.undef.manoslocales.ui.theme.screens.register.RegisterScreen
import com.undef.manoslocales.ui.theme.screens.settings.SettingsScreen
import com.undef.manoslocales.ui.theme.screens.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManosLocalesTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Feed.route) { FeedScreen(navController) }
        //composable(Screen.Settings.route) { SettingsScreen(navController) }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")
            val product = sampleProducts.find { it.id == productId }
            if (product != null) {
                ProductDetailScreen(navController, product)
            } else {
                Text("Producto no encontrado")
            }
        }
    }
}