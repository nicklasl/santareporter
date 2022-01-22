package nu.nldv.santareporter.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nu.nldv.santareporter.SHARED_PREFS
import nu.nldv.santareporter.persistence.SharedPrefsStorageImpl
import nu.nldv.santareporter.persistence.Storage

@Module
@InstallIn(SingletonComponent::class)
object DiModule {

    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS, Application.MODE_PRIVATE)

    @Provides
    fun provideStorage(sharedPrefs: SharedPreferences): Storage =
        SharedPrefsStorageImpl(sharedPrefs)
}