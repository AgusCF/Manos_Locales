package com.undef.manoslocales.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.undef.manoslocales.data.model.CartItemResponse

@Composable
fun CartItemCard(
    item: CartItemResponse,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                Text("Precio: \$${item.price}", style = MaterialTheme.typography.bodySmall)
                // ✅ Sin validación de stock aquí
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(item.quantity - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Restar")
                }
                Text("${item.quantity}")
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    enabled = true // ✅ siempre habilitado, validaremos después
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Sumar")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}