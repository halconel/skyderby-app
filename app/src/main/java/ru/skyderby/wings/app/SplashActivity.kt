package ru.skyderby.wings.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import ru.skyderby.wings.app.api.SkyDerbyApiService
import java.io.IOException
import java.util.Base64

const val USER_NAME = "ru.skyderby.wings.app.username"
const val PASSWORD = "ru.skyderby.wings.app.password"
const val RESPONSE_CODE = "ru.skyderby.wings.app.response_code"
const val TOKEN = "ru.skyderby.wings.app.token"

class SplashActivity : Activity() {
    // Sky Derby API interface
    private val skyDerbyApiServe by lazy {
        SkyDerbyApiService.create()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            // Get authorization credentials
            val username = PreferenceSave.getInstance(this).login
            val password = PreferenceSave.getInstance(this).password

            if(username == "") {
                // This is fist app start as well
                startLoginActivity()
            }
            else {
                // Credentials
                val token = getAuthToken(username, password)
                // Request user data to the sky derby API
                try {
                    val response = skyDerbyApiServe.getProfile(token).execute()
                    if (response.isSuccessful) {
                        // Authorized
                        startMainActivity(username, password, token)
                    }
                    else {
                        // Unauthorized
                        startLoginActivity(username, password, response.code())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            // close this activity
            finish()
        }, SPLASH_TIME_OUT.toLong())
    }

    private fun startMainActivity(username: String?, password: String?, token: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(USER_NAME, username)
            putExtra(PASSWORD, password)
            putExtra(TOKEN, token)
        }
        startActivity(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun startLoginActivity(username:String, password:String, code:Int) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(USER_NAME, username)
            putExtra(PASSWORD, password)
            putExtra(RESPONSE_CODE, code)
        }
        startActivity(intent)
    }

    private fun getAuthToken(username:String, password:String): String {
        val authHeader = ("$username:$password").toByteArray()
        return "Basic " + Base64.getEncoder().encodeToString(authHeader)
    }

    companion object {

        // Splash screen timer
        private val SPLASH_TIME_OUT = 3000
    }
}