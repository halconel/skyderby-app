package ru.skyderby.wings.app.activities

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import ru.skyderby.wings.app.R
import ru.skyderby.wings.app.api.SkyDerbyApiService
import java.io.IOException
import androidx.core.app.ActivityOptionsCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_splash.*
import retrofit2.Response
import ru.skyderby.wings.app.api.CredentialsMessage
import ru.skyderby.wings.app.helpers.Preferences


class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Start cookie magic
        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
        java.net.CookieHandler.setDefault(SkyDerbyApiService.proxy)

        // Get authorization credentials
        Preferences.init(this)
        val username = Preferences.username
        val password = Preferences.password

        val mAuthTask = UserLoginTask(username, password)
        mAuthTask!!.execute(null as Void?)
    }

    private fun startMainActivity(profile: CredentialsMessage?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(getString(R.string.PROFILE), profile)
        }
        startActivity(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imgLogo as View, "logo")
        startActivity(intent, options.toBundle())
    }

    private val skyDerbyApiService by lazy {
        SkyDerbyApiService.create()
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(
            private val mEmail: String,
            private val mPassword: String
    ) : AsyncTask<Void, Void, Boolean>() {

        var profileApiMessage: Response<CredentialsMessage>? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            // Credentials
            val authHeader = ("$mEmail:$mPassword").toByteArray()
            val token = "Basic ${android.util.Base64
                    .encodeToString(authHeader, android.util.Base64.NO_WRAP)}"
            // Request user data to the sky derby API
            try {
                profileApiMessage = skyDerbyApiService.getProfile(token).execute()
                return profileApiMessage!!.isSuccessful
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return false
        }

        override fun onPostExecute(success: Boolean?) {
            if (success!!) startMainActivity(profileApiMessage?.body())
            else startLoginActivity()
            finish()
        }
    }

    companion object {

        // Splash screen timer
        private const val SPLASH_TIME_OUT = 3000
    }
}