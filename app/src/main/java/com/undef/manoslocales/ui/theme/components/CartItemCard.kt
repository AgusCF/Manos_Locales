package com.undef.manoslocales.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.undef.manoslocales.data.model.CartItemResponse
import com.undef.manoslocales.ui.theme.RosaClaro
import com.undef.manoslocales.ui.theme.RosaClaroSemi
import com.undef.manoslocales.ui.theme.RosaOscuro
import com.undef.manoslocales.ui.theme.TextoPrincipal

@Composable
fun CartItemCard(
    item: CartItemResponse,
    maxStock: Int,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = RosaClaroSemi
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoPrincipal
                )
                Text(
                    "Precio: ${item.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RosaOscuro
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Botón Restar (deshabilitado si quantity = 1)
                IconButton(
                    onClick = { onQuantityChange(item.quantity - 1) },
                    enabled = item.quantity > 1
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Restar",
                        tint = if (item.quantity > 1) RosaOscuro else Color.Gray
                    )
                }
                
                Text(
                    "${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoPrincipal,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // Botón Sumar
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    enabled = item.quantity > maxStock
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Sumar",
                        tint = if (item.quantity < maxStock) RosaOscuro else Color.Gray
                    )
                }
                
                // Botón Eliminar
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = RosaOscuro
                    )
                }
            }
        }
    }
}
