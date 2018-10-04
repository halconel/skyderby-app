package ru.skyderby.wings.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import ru.skyderby.wings.app.R
import ru.skyderby.wings.app.api.SkyDerbyApiService
import java.io.IOException
import androidx.core.app.ActivityOptionsCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import co.zsmb.materialdrawerkt.imageloader.drawerImageLoader
import com.basecamp.turbolinks.TurbolinksAdapter
import com.basecamp.turbolinks.TurbolinksSession
import com.basecamp.turbolinks.TurbolinksView
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_splash.*
import retrofit2.Response
import ru.skyderby.wings.app.api.CredentialsMessage
import ru.skyderby.wings.app.helpers.Preferences


class SplashActivity : AppCompatActivity(), TurbolinksAdapter {
    var profileApiMessage: Response<CredentialsMessage>? = null

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
        // Initialize image loader for drawer
        drawerImageLoader {
            placeholder { ctx, tag ->
                DrawerUIUtils.getPlaceHolder(ctx)
            }
            set { imageView, uri, placeholder, tag ->
                Picasso.with(imageView.context)
                        .load(uri)
                        .placeholder(placeholder)
                        .into(imageView)
            }
            cancel { imageView ->
                Picasso.with(imageView.context)
                        .cancelRequest(imageView)
            }
        }
        // Attempt login
        val mAuthTask = UserLoginTask(username, password)
        mAuthTask!!.execute(null as Void?)
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    override fun onPageFinished() {

    }

    override fun onReceivedError(errorCode: Int) {

    }

    override fun pageInvalidated() {

    }

    override fun requestFailedWithStatusCode(statusCode: Int) {

    }

    override fun visitCompleted() {
        //startMainActivity(profileApiMessage?.body())
        //finish()
    }

    override fun visitProposedToLocationWithAction(location: String, action: String) {

    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun startMainActivity(profile: CredentialsMessage?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(getString(R.string.PROFILE), profile)
            putExtra(getString(R.string.COLD_START), true)
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
            if (success!!) {
                // Cold start
                TurbolinksSession.getDefault(this@SplashActivity).webView.settings.userAgentString =
                        "turbolinks-view\\inventory-android"
                if (profileApiMessage!!.isSuccessful) {
                    val location = "https://${SkyDerbyApiService.hostName}/profiles/${profileApiMessage!!.body()!!.id}?mobile=1"
                    val turbolinksView: TurbolinksView? = this@SplashActivity.findViewById(R.id.turbolinks_view)
                    TurbolinksSession.getDefault(this@SplashActivity)
                            .activity(this@SplashActivity)
                            .adapter(this@SplashActivity)
                            .view(turbolinksView)
                            .visit(location)
                }
                startMainActivity(profileApiMessage?.body())
            }
            else startLoginActivity()
            finish()
        }
    }

    companion object {

        // Splash screen timer
        private const val SPLASH_TIME_OUT = 3000
    }
}