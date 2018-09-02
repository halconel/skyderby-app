package ru.skyderby.wings.app.api


import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface SkyDerbyApiService {

    @GET("v1/profiles/current.json")
    fun getProfile(@Header("Authorization") authorization: String): Call<CredentialsMessage>

    companion object {
        fun create(): SkyDerbyApiService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://skyderby.ru/api/")
                    .build()

            return retrofit.create(SkyDerbyApiService::class.java)
        }
    }
}