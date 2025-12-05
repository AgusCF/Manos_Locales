package com.undef.manoslocales.ui.theme.screens.cart

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.manoslocales.ui.theme.Fondo
import com.undef.manoslocales.ui.theme.RosaClaro
import com.undef.manoslocales.ui.theme.RosaOscuro
import com.undef.manoslocales.ui.theme.TextoPrincipal
import com.undef.manoslocales.ui.theme.components.CartItemCard
import com.undef.manoslocales.viewmodel.CartViewModel
import com.undef.manoslocales.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel
) {
    val cart by cartViewModel.cart.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cartViewModel.validateCart()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Carrito", color = TextoPrincipal) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        cartViewModel.clearCart { result ->
                            result.onFailure { error ->
                                Log.w("CartScreen", "Error al vaciar: ${error.message}")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Vaciar carrito")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = RosaClaro
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Fondo)
        ) {
            if (cart.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "El carrito está vacío",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoPrincipal
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cart) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { newQty ->
                                val product = productViewModel.products.value
                                    .find { it.id == item.product_id }
                                if (product != null && newQty > product.stock) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "No hay suficiente stock (máx: ${product.stock})"
                                        )
                                    }
                                    // no llamamos al ViewModel
                                } else {
                                    cartViewModel.updateQuantity(item.id, newQty) { result ->
                                        result.onFailure { error ->
                                            Log.w("CartScreen", "Error: ${error.message}")
                                        }
                                    }
                                }
                            },
                            onRemove = {
                                Log.i("CartScreen","Toco remover - itemId=${item.id}")
                                cartViewModel.removeItem(item.id) { result ->
                                    result.onFailure { error ->
                                        Log.w("CartScreen", "Error al eliminar: ${error.message}")
                                    }
                                }
                            }
                        )
                    }
                }

                val total = cart.sumOf {
                    it.price.replace("$", "").toDoubleOrNull()?.times(it.quantity) ?: 0.0
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RosaClaro)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Total: \$${"%.2f".format(total)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextoPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = cart.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RosaOscuro,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Text("Finalizar compra")
                    }
                }
            }
        }
    }
}