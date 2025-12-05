package com.undef.manoslocales.di

import com.undef.manoslocales.data.remote.RetrofitInstance
import com.undef.manoslocales.data.repository.CartRepository
import com.undef.manoslocales.data.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository {
        return ProductRepository()
    }

    @Provides
    @Singleton
    fun provideCartRepository(): CartRepository {
        return CartRepository(RetrofitInstance)
    }
}