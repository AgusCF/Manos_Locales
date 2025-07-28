package com.undef.manoslocales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.undef.manoslocales.data.remote.RetrofitInstance
import com.undef.manoslocales.ui.theme.ManosLocalesTheme
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.ui.theme.screens.detail.ProductDetailScreen
import com.undef.manoslocales.ui.theme.screens.favorite.FavoritesOnlyScreen
import com.undef.manoslocales.ui.theme.screens.feed.FeedScreen
import com.undef.manoslocales.ui.theme.screens.login.LoginScreen
import com.undef.manoslocales.ui.theme.screens.register.RegisterScreen
import com.undef.manoslocales.ui.theme.screens.splash.SplashScreen
import com.undef.manoslocales.viewmodel.FavoritesViewModel
import com.undef.manoslocales.viewmodel.ProductViewModel
import com.undef.manoslocales.viewmodel.UserViewModel
import com.undef.manoslocales.viewmodel.UserViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManosLocalesTheme {
                val navController = rememberNavController()

                val favoritesViewModel: FavoritesViewModel = viewModel()
                val productViewModel: ProductViewModel = viewModel()

                // Instanciamos ApiService manualmente (o usa tu singleton)
                val apiService = RetrofitInstance.api

                // ViewModel con parámetros personalizados
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(apiService)
                )

                AppNavigation(
                    navController = navController,
                    favoritesViewModel = favoritesViewModel,
                    productViewModel = productViewModel,
                    userViewModel = userViewModel
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
    userViewModel: UserViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, userViewModel) // 👈 ahora recibe userViewModel
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController) // luego lo pasamos si querés registro real
        }
        composable(Screen.Feed.route) {
            FeedScreen(navController, favoritesViewModel, productViewModel)
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
            FavoritesOnlyScreen(navController, favoritesViewModel)
        }
    }
}
