package com.undef.manoslocales

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.undef.manoslocales.ui.theme.ManosLocalesTheme
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.ui.theme.screens.access.AccessScreen
import com.undef.manoslocales.ui.theme.screens.cart.CartScreen
import com.undef.manoslocales.ui.theme.screens.detail.ProductDetailScreen
import com.undef.manoslocales.ui.theme.screens.favorite.FavoritesOnlyScreen
import com.undef.manoslocales.ui.theme.screens.feed.FeedScreen
import com.undef.manoslocales.ui.theme.screens.login.LoginScreen
import com.undef.manoslocales.ui.theme.screens.register.RegisterScreen
import com.undef.manoslocales.ui.theme.screens.settings.SettingsScreen
import com.undef.manoslocales.ui.theme.screens.splash.SplashScreen
import com.undef.manoslocales.viewmodel.AuthViewModel
import com.undef.manoslocales.viewmodel.CartViewModel
import com.undef.manoslocales.viewmodel.FavoritesViewModel
import com.undef.manoslocales.viewmodel.ProductViewModel
import com.undef.manoslocales.viewmodel.UserViewModel
import com.undef.manoslocales.viewmodel.SettingsViewModel
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
                val authViewModel: AuthViewModel = hiltViewModel()
                val cartViewModel: CartViewModel = hiltViewModel()
                AppNavigation(
                    navController = navController,
                    favoritesViewModel = favoritesViewModel,
                    productViewModel = productViewModel,
                    userViewModel = userViewModel,
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    cartViewModel = cartViewModel
                )
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
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            Log.i("DebugDev", "Cargando SplashScreen")
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Login.route) {
            Log.i("DebugDev", "Cargando LoginScreen")
            LoginScreen(
                navController = navController,
                userViewModel = userViewModel,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Register.route) {
            Log.i("DebugDev", "Cargando RegisterScreen")
            RegisterScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(Screen.Settings.route) {
            Log.i("DebugDev", "Cargando SettingsScreen")
            SettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Feed.route) {
            Log.i("DebugDev", "Cargando FeedScreen(MainActivity)")

            // ✅ Evitar volver atrás desde Feed
            BackHandler {}

            FeedScreen(
                navController = navController,
                favoritesViewModel = favoritesViewModel,
                productViewModel = productViewModel,
                authViewModel = authViewModel,
                cartViewModel = cartViewModel
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

            val product by productViewModel.selectedProduct.collectAsState()
            if (product != null) {
                ProductDetailScreen(
                    navController = navController,
                    product = product!!,
                    favoritesViewModel = favoritesViewModel,
                    cartViewModel = cartViewModel
                )
            } else {
                Text("Producto no encontrado")
            }
        }
        composable(Screen.FavoritesOnly.route) {
            Log.i("DebugDev", "Cargando FavoritesOnlyScreen")
            FavoritesOnlyScreen(
                navController = navController,
                favoritesViewModel = favoritesViewModel,
                cartViewModel = cartViewModel
            )
        }
        composable(Screen.Access.route) {
            Log.i("DebugDev", "Cargando AccessScreen(MainActivity)")
            AccessScreen(
                navController = navController,
                userViewModel = userViewModel,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel
            )
        }
        composable(Screen.Cart.route) {
            Log.i("DebugDev", "Cargando CartScreen")
            CartScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                productViewModel = productViewModel
            )
        }
    }
}