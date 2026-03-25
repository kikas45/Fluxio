package com.example.inprideexchange.di
import com.example.inprideexchange.appThemeScreen.ThemeDataStore
import com.example.inprideexchange.data.theme.ThemeRepository
import com.example.inprideexchange.data.theme.ThemeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    @Provides
    @Singleton
    fun provideThemeRepository(
        dataStore: ThemeDataStore
    ): ThemeRepository {
        return ThemeRepositoryImpl(dataStore)
    }
}