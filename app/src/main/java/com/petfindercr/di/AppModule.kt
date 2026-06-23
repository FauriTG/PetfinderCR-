package com.petfindercr.di

import android.content.Context
import com.petfindercr.data.remote.SupabaseClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = SupabaseClientProvider.client

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
