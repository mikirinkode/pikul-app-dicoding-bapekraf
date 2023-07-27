package com.mikirinkode.pikul.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.mikirinkode.pikul.constants.Constants
import com.mikirinkode.pikul.data.local.LocalPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): LocalPreference {
        return LocalPreference(context)
    }

    @Provides
    @Singleton
    fun provideFirebase(): Firebase {
        return Firebase
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(firebase: Firebase): FirebaseAuth {
        return firebase.auth
    }


    @Provides
    @Singleton
    fun provideFireStore(firebase: Firebase): FirebaseFirestore {
        return firebase.firestore
    }

    @Provides
    @Singleton
    fun provideStorage(firebase: Firebase): FirebaseStorage {
        return firebase.storage
    }


    @Provides
    @Singleton
    fun provideDatabase(firebase: Firebase): FirebaseDatabase {
        return firebase.database
    }
}