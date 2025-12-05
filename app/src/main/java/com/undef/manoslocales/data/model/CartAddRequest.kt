package com.undef.manoslocales.data.model

import com.google.gson.annotations.SerializedName

data class CartAddRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("product_id")
    val productId: Int,
    val quantity: Int
)