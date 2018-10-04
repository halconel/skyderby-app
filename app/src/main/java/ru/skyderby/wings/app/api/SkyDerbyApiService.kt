package ru.skyderby.wings.app.api


import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import okhttp3.OkHttpClient
import ru.skyderby.wings.app.helpers.WebkitCookieManagerProxy
import java.net.CookiePolicy


interface SkyDerbyApiService {

    @GET("v1/profiles/current.json")
    fun getProfile(@Header("Authorization") authorization: String): Call<CredentialsMessage>

    companion object {

        val hostName: String = "skyderby.ru"
        val proxy by lazy {
            WebkitCookieManagerProxy(null, CookiePolicy.ACCEPT_ALL)
        }

        fun create(): SkyDerbyApiService {

            val client = OkHttpClient.Builder().cookieJar(proxy).build()

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://skyderby.ru/api/")
                    .client(client)
                    .build()

            return retrofit.create(SkyDerbyApiService::class.java)
        }
    }
}