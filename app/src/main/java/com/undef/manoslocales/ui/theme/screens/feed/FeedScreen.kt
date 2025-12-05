@file:OptIn(ExperimentalMaterial3Api::class)

package com.undef.manoslocales.ui.theme.screens.feed

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.ui.theme.components.ProductCard
import com.undef.manoslocales.viewmodel.AuthViewModel
import com.undef.manoslocales.viewmodel.CartViewModel
import com.undef.manoslocales.viewmodel.FavoritesViewModel
import com.undef.manoslocales.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navController: NavController,
    favoritesViewModel: FavoritesViewModel,
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var hasInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.isLoggedIn.collect { loggedIn ->
            if (loggedIn) {
                Log.d("DebugDev", "✅ Usuario logueado, cargando productos y favoritos")
                productViewModel.loadProducts()
                favoritesViewModel.refreshFavorites()
            }
        }
    }

    // Estados de UI
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var expanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val error by productViewModel.errorMessage.collectAsState()
    val favorites by favoritesViewModel.favorites.collectAsState()
    val cart by cartViewModel.cart.collectAsState()

    val categories = remember(products) {
        listOf("Todas") + products.map { it.category }.distinct()
    }

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.name
            .split(" ", "-", ",", ".", "(", ")")
            .any { word -> word.startsWith(searchQuery, ignoreCase = true) }

        val matchesCategory = selectedCategory == "Todas" || product.category == selectedCategory

        matchesSearch && matchesCategory
    }

    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Log.e("DebugDev", "el isLoggedIn dio false")
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Productos Manos Locales") },
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
                        IconButton(onClick = {
                            navController.navigate(Screen.FavoritesOnly.route)
                        }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Ver favoritos")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            navController.navigate(Screen.Settings.route)
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // 🔍 Búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                )

                // 🎯 Filtro por categoría
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoría: $selectedCategory")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // ❤️ Favoritos
                if (favorites.isNotEmpty()) {
                    Text(
                        text = "Tus Favoritos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(favorites) { product ->
                            val isFavorite = favorites.any { it.id == product.id }
                            ProductCard(
                                product = product,
                                isFavorite = isFavorite,
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

                // Lista de productos filtrados
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Text(
                            text = "Ocurrió un error: $error",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn {
                            items(filteredProducts) { product ->
                                val isFavorite = favorites.any { it.id == product.id }

                                ProductCard(
                                    product = product,
                                    isFavorite = isFavorite,
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
        }
    }
}