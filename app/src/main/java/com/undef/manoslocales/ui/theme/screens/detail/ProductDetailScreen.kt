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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.viewmodel.FavoritesViewModel

@Composable
fun ProductDetailScreen(
    navController: NavController,
    product: Product,
    favoritesViewModel: FavoritesViewModel
) {
    val isFavorite = remember {
        derivedStateOf { favoritesViewModel.isFavorite(product) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                onClick = { favoritesViewModel.toggleFavorite(product) }
            ) {
                Text(
                    if (isFavorite.value) "Quitar de Favoritos 💔" else "Agregar a Favoritos ❤️"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}
