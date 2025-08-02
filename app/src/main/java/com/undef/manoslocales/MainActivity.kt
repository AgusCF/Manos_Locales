package com.undef.manoslocales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.undef.manoslocales.ui.theme.ManosLocalesTheme
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.ui.theme.screens.detail.ProductDetailScreen
import com.undef.manoslocales.ui.theme.screens.favorite.FavoritesOnlyScreen
import com.undef.manoslocales.ui.theme.screens.feed.FeedScreen
import com.undef.manoslocales.ui.theme.screens.login.LoginScreen
import com.undef.manoslocales.ui.theme.screens.register.RegisterScreen
import com.undef.manoslocales.ui.theme.screens.settings.SettingsScreen
import com.undef.manoslocales.ui.theme.screens.splash.SplashScreen
import com.undef.manoslocales.viewmodel.FavoritesViewModel
import com.undef.manoslocales.viewmodel.ProductViewModel
import com.undef.manoslocales.viewmodel.SettingsViewModel
import com.undef.manoslocales.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManosLocalesTheme {
                val navController = rememberNavController()
                val favoritesViewModel: FavoritesViewModel = hiltViewModel()
                val productViewModel: ProductViewModel = hiltViewModel()
                val userViewModel: UserViewModel = hiltViewModel()
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                AppNavigation(navController, favoritesViewModel, productViewModel, userViewModel, settingsViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable(Screen.Feed.route) {
            FeedScreen(
                navController = navController,
                favoritesViewModel = favoritesViewModel,
                productViewModel = productViewModel
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")
            if (productId != null) {
                productViewModel.fetchProductById(productId)
            }

            val product = productViewModel.selectedProduct
            if (product != null) {
                ProductDetailScreen(
                    navController = navController,
                    product = product,
                    favoritesViewModel = favoritesViewModel
                )
            } else {
                Text("Producto no encontrado")
            }
        }
        composable(Screen.FavoritesOnly.route) {
            FavoritesOnlyScreen(
                navController = navController,
                favoritesViewModel = favoritesViewModel
            )
        }
    }
}