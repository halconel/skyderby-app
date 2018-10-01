package ru.skyderby.wings.app

import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.net.URI
import java.util.*
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.CookieJar

class WebkitCookieManagerProxy @JvmOverloads constructor(
        store: CookieStore? = null,
        cookiePolicy: CookiePolicy? = null
) : CookieManager(null, cookiePolicy), CookieJar {

    private val webkitCookieManager: android.webkit.CookieManager =
            android.webkit.CookieManager.getInstance()

    @Throws(IOException::class)
    override fun put(uri: URI, responseHeaders: Map<String, List<String>>) {
        // make sure our args are valid
        if (uri == null || responseHeaders == null) return
        // save our url once
        val url = uri!!.toString()
        // go over the headers
        responseHeaders.keys.forEach { headerKey ->
            // ignore headers which aren't cookie related
            if (!(headerKey.equals("Set-Cookie2", ignoreCase = true) ||
                            headerKey.equals("Set-Cookie", ignoreCase = true))
            ) return@forEach //Continue
            // process each of the headers
            responseHeaders[headerKey]!!.forEach { headerValue ->
                this.webkitCookieManager.setCookie(url, headerValue)
            }
        }
    }

    @Throws(IOException::class)
    override fun get(uri: URI, requestHeaders: Map<String, List<String>>): Map<String, List<String>> {
        // make sure our args are valid
        if (uri == null || requestHeaders == null) throw IllegalArgumentException("Argument is null")
        // save our url once
        val url = uri!!.toString()
        // prepare our response
        val res = java.util.HashMap<String, List<String>>()
        // get the cookie
        val cookie = this.webkitCookieManager.getCookie(url)
        // return it
        if (cookie != null) res["Cookie"] = Arrays.asList(cookie)
        return res
    }

    override fun getCookieStore(): CookieStore {
        // we don't want anyone to work with this cookie store directly
        throw UnsupportedOperationException()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val generatedResponseHeaders: HashMap<String, MutableList<String>> = hashMapOf()
        val cookiesList: MutableList<String> = mutableListOf()
        // toString correctly generates a normal cookie string
        cookies.forEach { cookiesList.add(it.toString()) }
        generatedResponseHeaders["Set-Cookie"] = cookiesList

        try { put(url.uri(), generatedResponseHeaders) }
        catch (e: IOException) {
            //Log.e(TAG, "Error adding cookies through okhttp", e)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieArrayList: MutableList<Cookie> = ArrayList()
        try {
            val cookieList = get(url.uri(), HashMap<String, List<String>>())
            // Format here looks like: "Cookie":["cookie1=val1;cookie2=val2;"]
            cookieList.values.forEach { ls ->
                ls.forEach { s ->
                    val cookies = s.split(";".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    for (cookie in cookies) {
                        val c = Cookie.parse(url, cookie)
                        cookieArrayList.add(c!!)
                    }
                }
            }
        } catch (e: IOException) {
            //Log.e(TAG, "error making cookie!", e)
        }

        return cookieArrayList
    }
}