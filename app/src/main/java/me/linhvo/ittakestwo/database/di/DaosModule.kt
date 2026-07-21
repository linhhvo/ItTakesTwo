package me.linhvo.ittakestwo.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.linhvo.ittakestwo.database.LocalDatabase
import me.linhvo.ittakestwo.database.dao.MessageDao
import me.linhvo.ittakestwo.database.dao.PairingDao
import me.linhvo.ittakestwo.database.dao.UserDao

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
    @Provides
    fun provideUserDao(database: LocalDatabase): UserDao = database.userDao()

    @Provides
    fun providePairingDao(database: LocalDatabase): PairingDao = database.pairingDao()

    @Provides
    fun provideMessageDao(database: LocalDatabase): MessageDao = database.messageDao()
}