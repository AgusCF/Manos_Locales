package com.undef.manoslocales.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.undef.manoslocales.data.remote.ApiService

class UserViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

