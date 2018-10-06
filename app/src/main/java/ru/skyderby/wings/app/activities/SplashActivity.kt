package ru.skyderby.wings.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.zsmb.materialdrawerkt.imageloader.drawerImageLoader
import com.basecamp.turbolinks.TurbolinksAdapter
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.squareup.picasso.Picasso
import retrofit2.Response
import ru.skyderby.wings.app.R
import ru.skyderby.wings.app.api.CredentialsMessage
import ru.skyderby.wings.app.api.SkyDerbyApiService
import ru.skyderby.wings.app.helpers.AttemptLogin
import ru.ztrap.iconics.kt.setIconicsFactory
import java.lang.ref.WeakReference


class SplashActivity : AppCompatActivity(), TurbolinksAdapter {
    var profileApiMessage: CredentialsMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Start cookie magic
        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
        java.net.CookieHandler.setDefault(SkyDerbyApiService.proxy)
        // Attempt login
        UserLoginTask(this, R.id.turbolinks_view).execute(true)
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
        startMainActivity(profileApiMessage)
        finish()
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

    fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class UserLoginTask(splashActivity: SplashActivity, id: Int)
        : AttemptLogin(
            activityReference = WeakReference(splashActivity),
            adapterReference = WeakReference(splashActivity),
            viewID = id) {

        override fun onPostExecute(result: Response<CredentialsMessage>?) {
            val activity = activityReference.get() as? SplashActivity ?: return
            if(activity.isFinishing || activity.isDestroyed) return
            when {
                result?.isSuccessful == true -> activity.profileApiMessage = result.body()
                else -> activity.startLoginActivity()
            }
            super.onPostExecute(result)
        }
    }
}


