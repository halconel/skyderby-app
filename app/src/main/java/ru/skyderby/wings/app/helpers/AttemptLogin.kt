package ru.skyderby.wings.app.helpers

import android.app.Activity
import android.os.AsyncTask
import com.basecamp.turbolinks.TurbolinksAdapter
import com.basecamp.turbolinks.TurbolinksSession
import retrofit2.Response
import ru.skyderby.wings.app.api.CredentialsMessage
import ru.skyderby.wings.app.api.SkyDerbyApiService
import java.io.IOException
import java.lang.ref.WeakReference

open class AttemptLogin(
        val activityReference: WeakReference<Activity>,
        private val adapterReference: WeakReference<TurbolinksAdapter>,
        private val viewID: Int
) : AsyncTask<Boolean, Void, Response<CredentialsMessage>?>() {

    var errorMessage:String? = null
    private var coldBoot: Boolean = false

    private val skyDerbyApiService by lazy {
        SkyDerbyApiService.create()
    }

    override fun doInBackground(vararg params: Boolean?): Response<CredentialsMessage>? {
        // Return immediately if there references to nothing
        if (activityReference.get() == null || adapterReference.get() == null) return null
        // Get authorization credentials
        Preferences.init(activityReference.get()!!)
        val username = Preferences.username
        val password = Preferences.password
        // Make token
        val authHeader = ("$username:$password").toByteArray()
        val token = "Basic ${android.util.Base64
                .encodeToString(authHeader, android.util.Base64.NO_WRAP)}"
        // Request user profile from the skyderby API
        try {
            coldBoot = params[0] == true
            return skyDerbyApiService.getProfile(token).execute()
        } catch (e: IOException) {
            //e.printStackTrace()
            errorMessage = e.localizedMessage
        }

        return null
    }

    override fun onPostExecute(result: Response<CredentialsMessage>?) {
        if(result == null) return
        if(coldBoot && result.isSuccessful) executeColdBoot(result)
    }

    override fun onCancelled() {

    }

    private fun executeColdBoot(result: Response<CredentialsMessage>) {
        val activity = activityReference.get()?:return
        val adapter = adapterReference.get()?:return
        if (activity.isFinishing || activity.isDestroyed) return

        // Client identification
        TurbolinksSession.setDebugLoggingEnabled(true)
        TurbolinksSession.getDefault(activity).webView.settings.userAgentString =
                "turbolinks-view\\inventory-android"
        // Loading the main page for snapshot caching if the login attempt was successful
        val profile = result.body() ?: return
        val location = "https://${SkyDerbyApiService.hostName}/profiles/${profile.id}?mobile=1"
        TurbolinksSession.getDefault(activity)
                .activity(activity)
                .adapter(adapter)
                .view(activity.findViewById(viewID))
                .visit(location)


    }

}