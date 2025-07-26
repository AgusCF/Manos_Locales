@file:OptIn(ExperimentalMaterial3Api::class)

package com.undef.manoslocales.ui.theme.screens.feed


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.undef.manoslocales.data.sampleProducts
import com.undef.manoslocales.ui.theme.components.ProductCard
import com.undef.manoslocales.ui.theme.Screen
import com.undef.manoslocales.viewmodel.FavoritesViewModel

@Composable
fun FeedScreen(
    navController: NavController,
    favoritesViewModel: FavoritesViewModel
) {
    val favorites = favoritesViewModel.favorites

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Productos Locales") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            if (favorites.isNotEmpty()) {
                Text(
                    text = "Tus Favoritos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(favorites) { product ->
                        ProductCard(
                            product = product,
                            isFavorite = true,
                            onFavoriteClick = {
                                favoritesViewModel.toggleFavorite(product)
                            },
                            onClick = {
                                navController.navigate(Screen.Detail.createRoute(product.id))
                            }
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sampleProducts) { product ->
                    val isFav = favoritesViewModel.isFavorite(product)
                    ProductCard(
                        product = product,
                        isFavorite = isFav,
                        onFavoriteClick = {
                            favoritesViewModel.toggleFavorite(product)
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

