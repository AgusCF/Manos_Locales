package com.undef.manoslocales.data.model

data class CartAddRequest(
    val userId: Int,
    val productId: Int,
    val quantity: Int
)