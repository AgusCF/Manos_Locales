package com.undef.manoslocales.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Favorite(
    val id: Int? = null,

    // Cambio de camelCase a snake_case para compatibilidad con el Backend
    @SerializedName("user_id")
    val userId: Int,

    // Cambio de camelCase a snake_case para compatibilidad con el Backend
    @SerializedName("product_id")
    val productId: Int
): Parcelable