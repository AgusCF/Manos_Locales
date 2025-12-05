package com.undef.manoslocales.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cart(
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0
) : Parcelable {
    fun updateTotal(): Cart =
        copy(total = items.sumOf { it.price * it.quantity })
}