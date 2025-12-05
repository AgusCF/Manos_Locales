package com.undef.manoslocales.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val productId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String? = null,
    val stock: Int
) : Parcelable