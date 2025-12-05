package com.undef.manoslocales.ui.theme.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.viewmodel.CartViewModel
import com.undef.manoslocales.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    navController: NavController,
    product: Product,
    favoritesViewModel: FavoritesViewModel,
    cartViewModel: CartViewModel
) {
    val favorites by favoritesViewModel.favorites.collectAsState()
    val isFavorite = favorites.any { it.id == product.id }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = product.imageUrl),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(product.description)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Precio: \$${product.price}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        favoritesViewModel.toggleFavorite(product)
                    }
                ) {
                    Text(
                        if (isFavorite) "Quitar de Favoritos 💔" else "Agregar a Favoritos ❤️"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        cartViewModel.addItemFromProduct(product) { result ->
                            result.onSuccess { message ->
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            }.onFailure { error ->
                                scope.launch { snackbarHostState.showSnackbar(error.message ?: "Error desconocido") }
                            }
                        }
                    },
                    enabled = product.stock > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (product.stock > 0) "Agregar al carrito" else "Sin stock")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Volver")
                }
            }
        }
    }
}