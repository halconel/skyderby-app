package ru.skyderby.wings.app.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity

import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import co.zsmb.materialdrawerkt.draweritems.profile.profileSetting
import co.zsmb.materialdrawerkt.draweritems.sectionHeader
import co.zsmb.materialdrawerkt.imageloader.drawerImageLoader

import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.mikepenz.materialdrawer.AccountHeader

import com.basecamp.turbolinks.TurbolinksAdapter
import com.basecamp.turbolinks.TurbolinksSession
import com.basecamp.turbolinks.TurbolinksView
import com.mikepenz.materialdrawer.model.ProfileDrawerItem

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

import ru.skyderby.wings.app.R
import ru.skyderby.wings.app.api.CredentialsMessage
import ru.skyderby.wings.app.helpers.Preferences
import ru.ztrap.iconics.kt.setIconicsFactory
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity(), TurbolinksAdapter {
    private lateinit var result: Drawer
    private lateinit var headerResult: AccountHeader
    // Change the baseURL to an address that your VM or device can hit.
    private val hostName = "skyderby.ru"
    private var baseURL = "https://$hostName/?mobile=1"
    private var userProfile: CredentialsMessage? = null

    private var location: String = baseURL
    private var turbolinksView: TurbolinksView? = null
    //private val turbolinksHelper: TurbolinksHelper? = null

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        userProfile = intent.getSerializableExtra(getString(R.string.PROFILE)) as? CredentialsMessage
        val coldStart = intent.getBooleanExtra(getString(R.string.COLD_START), false)

        turbolinksView = findViewById(R.id.turbolinks_view)
        executeWebVisit(coldStart)

        if(location != baseURL){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(false)
        }

        addDrawer(savedInstanceState)

        toolbar
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
                .visit(location)
    }

    override fun onBackPressed() {
        if (result.isDrawerOpen)
            result.closeDrawer()
        else
            super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        result.saveInstanceState(outState)
        headerResult.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

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

    /**
     * The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
     * you can just open another activity, or in more complex cases, this would be a good spot for
     * routing logic to take you to the right place within your app.
     */
    override fun visitProposedToLocationWithAction(location: String, action: String) {
        // Execute the visit
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location)
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun executeWebVisit(coldStart: Boolean) {
        if (userProfile != null) {
            baseURL = "https://$hostName/profiles/${userProfile!!.id}?mobile=1"
        }

        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data
        if (Intent.ACTION_VIEW == appLinkAction) {
            location = appLinkData!!.toString()
        }
        else {
            location = intent.getStringExtra(getString(R.string.INTENT_URL)) ?: baseURL
        }
        // Execute the visit
        if (coldStart) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(true)
                    .view(turbolinksView)
                    .visit(location)
        }
        else {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .view(turbolinksView)
                    .visit(location)
        }
    }

    /**
     * Simply forwards to an error page, but you could alternatively show your own native screen
     * or do whatever other kind of error handling you want.
     */
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

    private fun addDrawer(savedInstanceState: Bundle?) {

        result = drawer {

            toolbar = this@MainActivity.toolbar
            hasStableIds = true
            savedInstance = savedInstanceState
            showOnFirstLaunch = true

            headerResult = accountHeader {
                background = R.drawable.drawer_header_bg
                savedInstance = savedInstanceState
                translucentStatusBar = true

                profile(userProfile!!.name, Preferences.username) {
                    iconUrl  = "https://${hostName+userProfile!!.photo.original}"
                    identifier = userProfile!!.id
                }
                profileSetting("Add account", "Add new Skyderby account") {
                    iicon = GoogleMaterial.Icon.gmd_add
                    identifier = 100_000
                }
                profileSetting("Manage Account") {
                    iicon = GoogleMaterial.Icon.gmd_settings
                    identifier = 100_001
                }

                onProfileChanged { _, profile, _ ->
                    when {
                        profile.identifier == 100_000L -> {
                            val size = headerResult.profiles.size
                            val newProfile = ProfileDrawerItem()
                                    .withName("New Batman ${size - 1}")
                                    .withNameShown(true)
                                    .withEmail("batman${size - 1}@gmail.com")
                                    .withIdentifier(100L + size + 1L)
                            headerResult.addProfile(newProfile, size - 2)
                        }
                        profile.identifier == 100_001L -> {
                        }
                    }
                    false
                }
            }

            sectionHeader("Skyderby") {
                divider = false
            }

            primaryItem("Home") {
                iicon = GoogleMaterial.Icon.gmd_home
                selected = location == baseURL
                onClick(openActivity(baseURL, MainActivity::class))
            }
            primaryItem("Tracks") {
                iicon = FontAwesome.Icon.faw_chart_area
                selected = location == getString(R.string.tracks)
                onClick(openActivity(getString(R.string.tracks), MainActivity::class))
            }
            primaryItem("Boogie") {
                iicon = FontAwesome.Icon.faw_medal
                selected = location == getString(R.string.events)
                onClick(openActivity(getString(R.string.events), MainActivity::class))
            }
            primaryItem("Competitions") {
                iicon = GoogleMaterial.Icon.gmd_timer
                selected = location == getString(R.string.virtual_competitions)
                onClick(openActivity(getString(R.string.virtual_competitions), MainActivity::class))
            }
            primaryItem("Places") {
                iicon = GoogleMaterial.Icon.gmd_place
                selected = location == getString(R.string.places)
                onClick(openActivity(getString(R.string.places), MainActivity::class))
            }
            primaryItem("Settings") {
                iicon = GoogleMaterial.Icon.gmd_settings
            }
        }
    }

    private fun <T : Activity> openActivity(intentUri: String, activity: KClass<T>): (View?) -> Boolean = {
        if(location != intentUri) {
            val intent = Intent(this@MainActivity, activity.java).apply {
                putExtra(getString(R.string.INTENT_URL), intentUri)
                putExtra(getString(R.string.PROFILE), userProfile)
            }
            startActivity(intent)
        }
        location != intentUri
    }
}
