package com.undef.manoslocales.data.model

data class CartItemResponse(
    val id: Int,
    val product_id: Int,
    val quantity: Int,
    val name: String,
    val price: String,
    val imageurl: String?
)