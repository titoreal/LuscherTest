package com.titin.testluscher.di

import android.content.Context
import com.titin.testluscher.data.api.ApiService
import com.titin.testluscher.data.repository.TestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApiService(@ApplicationContext context: Context): ApiService {
        return ApiService(context)
    }

    @Provides
    @Singleton
    fun provideTestRepository(apiService: ApiService): TestRepository {
        return TestRepository(apiService)
    }
}