package me.linhvo.ittakestwo.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.linhvo.ittakestwo.Configs
import me.linhvo.ittakestwo.datastore.ConfigsSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideConfigsDataStore(
        @ApplicationContext context: Context,
        configsSerializer: ConfigsSerializer
    ): DataStore<Configs> =
        DataStoreFactory.create(
            serializer = configsSerializer,
            produceFile = { context.dataStoreFile("configs.pb") }
        )
}