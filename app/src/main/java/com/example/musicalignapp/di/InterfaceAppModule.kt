package com.example.musicalignapp.di

import com.example.musicalignapp.core.generators.Generator
import com.example.musicalignapp.core.generators.IdGenerator
import com.example.musicalignapp.core.generators.PackageDateGenerator
import com.example.musicalignapp.core.jsonconverter.AlignmentResultToJsonConverter
import com.example.musicalignapp.core.jsonconverter.JsonConverter
import com.example.musicalignapp.data.remote.repository.AddFileRepositoryImpl
import com.example.musicalignapp.data.remote.repository.AlignRepositoryImpl
import com.example.musicalignapp.data.remote.repository.HomeRepositoryImpl
import com.example.musicalignapp.data.remote.repository.LoginRepositoryImpl
import com.example.musicalignapp.data.remote.repository.SignInRepositoryImpl
import com.example.musicalignapp.domain.repository.AddFileRepository
import com.example.musicalignapp.domain.repository.AlignRepository
import com.example.musicalignapp.domain.repository.HomeRepository
import com.example.musicalignapp.domain.repository.LoginRepository
import com.example.musicalignapp.domain.repository.SignInRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceAppModule {

    @Binds
    @Singleton
    abstract fun provideAlignmentResultToJsonConverter(
        alignmentResultToJsonConverter: AlignmentResultToJsonConverter
    ): JsonConverter

    @Binds
    @Singleton
    abstract fun provideAddFileRepository(
        addFileRepositoryImpl: AddFileRepositoryImpl
    ): AddFileRepository

    @Binds
    @Singleton
    abstract fun provideAlignRepository(
        alignRepositoryImpl: AlignRepositoryImpl
    ): AlignRepository

    @Binds
    @Singleton
    abstract fun provideHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun provideLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository

    @Binds
    @Singleton
    abstract fun provideSignInRepository(
        signInRepositoryImpl: SignInRepositoryImpl
    ): SignInRepository

    @Binds
    @Singleton
    @IdGeneratorAnnotation
    abstract fun provideIdGenerator(
        idGenerator: IdGenerator
    ): Generator<String>

    @Binds
    @Singleton
    @PackageDateGeneratorAnnotation
    abstract fun providePackageDateGenerator(
        packageDateGenerator: PackageDateGenerator
    ): Generator<String>


    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class IdGeneratorAnnotation

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class PackageDateGeneratorAnnotation
}