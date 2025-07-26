package com.undef.manoslocales.ui.theme.screens.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.undef.manoslocales.data.sampleProducts
import com.undef.manoslocales.ui.theme.components.ProductCard
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesOnlyScreen(
    navController: NavController,
    favoritesViewModel: FavoritesViewModel
) {
    val favorites = favoritesViewModel.favorites

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tus Favoritos ❤️") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
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
                        onClick = {
                            navController.navigate(Screen.Detail.createRoute(product.id))
                        }
                    )
                }
            }
        }
    }
}