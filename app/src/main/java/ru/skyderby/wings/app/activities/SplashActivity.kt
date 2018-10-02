package ru.skyderby.wings.app.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import ru.skyderby.wings.app.R
import ru.skyderby.wings.app.api.SkyDerbyApiService
import ru.skyderby.wings.app.helpers.PreferenceSave
import java.io.IOException
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : Activity() {
    // Sky Derby API interface
    private val skyDerbyApiServe by lazy {
        SkyDerbyApiService.create()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Start cookie magic
        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
        java.net.CookieHandler.setDefault(SkyDerbyApiService.proxy)

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
            putExtra(getString(R.string.USER_NAME), username)
            putExtra(getString(R.string.PASSWORD), password)
            putExtra(getString(R.string.TOKEN), token)
        }
        startActivity(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imgLogo as View, "logo")
        startActivity(intent, options.toBundle())
    }

    private fun startLoginActivity(username:String, password:String, code:Int) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(getString(R.string.USER_NAME), username)
            putExtra(getString(R.string.PASSWORD), password)
            putExtra(getString(R.string.RESPONSE_CODE), code)
        }
        val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imgLogo as View, "logo")
        startActivity(intent, options.toBundle())
    }

    private fun getAuthToken(username:String, password:String): String {
        val authHeader = ("$username:$password").toByteArray()
        return "Basic " + android.util.Base64.encodeToString(authHeader, android.util.Base64.NO_WRAP)
    }

    companion object {

        // Splash screen timer
        private const val SPLASH_TIME_OUT = 3000
    }
}