package com.example.safetymanagement2022.di

import com.example.safetymanagement2022.data.remote.service.AccountService
import com.example.safetymanagement2022.data.remote.service.HomeService
import com.example.safetymanagement2022.data.remote.service.ListService
import com.example.safetymanagement2022.data.remote.service.SettingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitServiceModule {
    @Provides
    @Singleton
    fun provideHomeService(retrofit: Retrofit): HomeService =
        retrofit.create(HomeService::class.java)

    @Provides
    @Singleton
    fun provideListService(retrofit: Retrofit): ListService =
        retrofit.create(ListService::class.java)

    @Provides
    @Singleton
    fun provideAccountService(retrofit: Retrofit): AccountService =
        retrofit.create(AccountService::class.java)

    @Provides
    @Singleton
    fun provideSettingService(retrofit: Retrofit): SettingService =
        retrofit.create(SettingService::class.java)
}