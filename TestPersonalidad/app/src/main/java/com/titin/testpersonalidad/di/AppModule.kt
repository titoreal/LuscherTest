package com.titin.testpersonalidad.di

import android.content.Context
import com.titin.testpersonalidad.data.api.ApiService
import com.titin.testpersonalidad.data.repository.TestRepository
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