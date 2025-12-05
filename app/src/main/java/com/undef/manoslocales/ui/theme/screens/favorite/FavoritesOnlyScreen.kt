package com.undef.manoslocales.ui.theme.screens.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.undef.manoslocales.ui.theme.components.ProductCard
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.CartViewModel
import com.undef.manoslocales.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesOnlyScreen(
    navController: NavController,
    favoritesViewModel: FavoritesViewModel,
    cartViewModel: CartViewModel
) {
    val favorites by favoritesViewModel.favorites.collectAsState(initial = emptyList())
    val isLoading by favoritesViewModel.isLoading.collectAsState(initial = false)
    val errorMessage by favoritesViewModel.errorMessage.collectAsState(initial = null)
    val cart by cartViewModel.cart.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tus Favoritos ❤️") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        BadgedBox(
                            badge = {
                                if (cart.isNotEmpty()) {
                                    Badge { Text("${cart.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $errorMessage")
            }
        } else if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Todavía no tenés favoritos")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(favorites) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = true,
                        onFavoriteClick = { favoritesViewModel.toggleFavorite(product) },
                        onAddToCart = {
                            cartViewModel.addItemFromProduct(product) { result ->
                                result.onSuccess { message ->
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }.onFailure { error ->
                                    scope.launch { snackbarHostState.showSnackbar(error.message ?: "Error desconocido") }
                                }
                            }
                        },
                        onClick = {
                            navController.navigate(Screen.Detail.createRoute(product.id))
                        }
                    )
                }
            }
        }
    }
}