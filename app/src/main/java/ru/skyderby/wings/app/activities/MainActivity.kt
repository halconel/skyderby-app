package ru.skyderby.wings.app.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.basecamp.turbolinks.TurbolinksSession
import com.basecamp.turbolinks.TurbolinksAdapter
import com.basecamp.turbolinks.TurbolinksView

//import com.mikepenz.materialdrawer.DrawerBuilder

import ru.skyderby.wings.app.R
import ru.skyderby.wings.app.api.CredentialsMessage

class MainActivity : AppCompatActivity(), TurbolinksAdapter {
    // Change the baseURL to an address that your VM or device can hit.
    private val hostName = "skyderby.ru"
    private var baseURL = "https://$hostName/?mobile=1"
    private var profile: CredentialsMessage? = null

    private var location: String? = null
    private var turbolinksView: TurbolinksView? = null
    //private val turbolinksHelper: TurbolinksHelper? = null

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //DrawerBuilder().withActivity(this).build()

        profile = intent.getSerializableExtra(getString(R.string.PROFILE)) as? CredentialsMessage
        if (profile != null) {
            baseURL = "https://$hostName/profiles/${profile!!.id}/edit?mobile=1"
        }

        TurbolinksSession.getDefault(this).webView.settings.userAgentString =
                "turbolinks-view\\inventory-android"

        // Find the custom TurbolinksView object in your layout
        turbolinksView = findViewById(R.id.turbolinks_view)

        // For this example we set a default location, unless one is passed in through an intent
        location = intent.getStringExtra(getString(R.string.INTENT_URL)) ?: baseURL

        // Execute the visit
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location)
    }

    override fun onRestart() {
        super.onRestart()

        // Since the webView is shared between activities, we need to tell Turbolinks
        // to load the location from the previous activity upon restarting
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .restoreWithCachedSnapshot(true)
                .view(turbolinksView)
                .visit(location)    }
    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    override fun onPageFinished() {

    }

    override fun onReceivedError(errorCode: Int) {
        handleError(errorCode)
    }

    override fun pageInvalidated() {

    }

    override fun requestFailedWithStatusCode(statusCode: Int) {
        handleError(statusCode)
    }

    override fun visitCompleted() {

    }

    // The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
    // you can just open another activity, or in more complex cases, this would be a good spot for
    // routing logic to take you to the right place within your app.
    override fun visitProposedToLocationWithAction(location: String, action: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(getString(R.string.INTENT_URL), location)

        this.startActivity(intent)
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    // Simply forwards to an error page, but you could alternatively show your own native screen
    // or do whatever other kind of error handling you want.
    private fun handleError(code: Int) {
        if (code == 404) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(false)
                    .view(turbolinksView)
                    .visit(baseURL + "/error")
        }
        else if(code == 401) {

        }
    }
}
