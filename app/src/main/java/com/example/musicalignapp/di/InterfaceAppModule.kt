package com.example.musicalignapp.di

import com.example.musicalignapp.core.jsonconverter.AlignmentResultToJsonConverter
import com.example.musicalignapp.core.jsonconverter.JsonConverter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceAppModule {

    @Binds
    @Singleton
    abstract fun provideRestCountriesRepository(
        alignmentResultToJsonConverter: AlignmentResultToJsonConverter
    ): JsonConverter
}