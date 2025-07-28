package com.undef.manoslocales.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val category: String
) : Parcelable