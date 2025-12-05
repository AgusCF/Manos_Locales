package com.undef.manoslocales.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartValidationResponse(
    val isValid: Boolean,
    val invalidItems: List<InvalidCartItem> = emptyList()
) : Parcelable

@Parcelize
data class InvalidCartItem(
    val productId: Int,
    val name: String,
    val requestedQuantity: Int,
    val availableStock: Int
) : Parcelable