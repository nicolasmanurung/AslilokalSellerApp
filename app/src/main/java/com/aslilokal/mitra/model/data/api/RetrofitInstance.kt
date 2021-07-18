package com.aslilokal.mitra.model.data.api

import com.aslilokal.mitra.utils.Constants.Companion.BASE_URL
import com.aslilokal.mitra.utils.Constants.Companion.BASE_URL_FIREBASE_FCM
import com.aslilokal.mitra.utils.Constants.Companion.BASE_URL_RO
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        private val retrofitRO by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(BASE_URL_RO)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        private val retrofitFirebase by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(BASE_URL_FIREBASE_FCM)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api by lazy {
            retrofit.create(AslilokalAPI::class.java)
        }

        val apiRO by lazy {
            retrofitRO.create(AslilokalAPI::class.java)
        }

        val apiFirebase by lazy {
            retrofitFirebase.create(AslilokalAPI::class.java)
        }
    }
}