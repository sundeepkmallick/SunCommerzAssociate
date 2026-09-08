package com.suncommerz.associate.di

import com.suncommerz.associate.data.repository.InventoryRepositoryImpl
import com.suncommerz.associate.data.repository.NotificationRepositoryImpl
import com.suncommerz.associate.data.repository.OrderRepositoryImpl
import com.suncommerz.associate.data.repository.ProductRepositoryImpl
import com.suncommerz.associate.data.repository.StoreManagerRepositoryImpl
import com.suncommerz.associate.data.repository.StoreRepositoryImpl
import com.suncommerz.associate.data.repository.UserRepositoryImpl
import com.suncommerz.associate.domain.repository.InventoryRepository
import com.suncommerz.associate.domain.repository.NotificationRepository
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.repository.ProductRepository
import com.suncommerz.associate.domain.repository.StoreManagerRepository
import com.suncommerz.associate.domain.repository.StoreRepository
import com.suncommerz.associate.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun UserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(orderRepositoryImpl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun InventoryRepository(inventoryRepositoryImpl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @Singleton
    abstract fun StoreRepository(storeRepositoryImpl: StoreRepositoryImpl): StoreRepository

    @Binds
    @Singleton
    abstract fun NotificationRepository(notificationRepositoryImpl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun StoreManagerRepository(storeManagerRepositoryImpl: StoreManagerRepositoryImpl): StoreManagerRepository

}