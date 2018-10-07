package ru.skyderby.wings.app.activities

import android.Manifest.permission.READ_CONTACTS
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.basecamp.turbolinks.TurbolinksAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Response
import ru.skyderby.wings.app.R
import ru.skyderby.wings.app.api.CredentialsMessage
import ru.skyderby.wings.app.helpers.AttemptLogin
import ru.skyderby.wings.app.helpers.Preferences
import ru.ztrap.iconics.kt.setIconicsFactory
import java.lang.ref.WeakReference
import java.util.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor>, TurbolinksAdapter {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null
    var profileApiMessage: CredentialsMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        populateAutoComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }


    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    override fun onPageFinished() {
    }

    override fun pageInvalidated() {
    }

    override fun onReceivedError(errorCode: Int) {
    }

    override fun visitCompleted() {
        startMainActivity(profileApiMessage)
        finish()
    }

    override fun requestFailedWithStatusCode(statusCode: Int) {
    }

    override fun visitProposedToLocationWithAction(location: String?, action: String?) {
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    private fun startMainActivity(profile: CredentialsMessage?) {
        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
            putExtra(getString(R.string.PROFILE), profile)
        }
        startActivity(intent)
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) return

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        // Set fields error message
        email.error = when {
            TextUtils.isEmpty(emailStr) -> getString(R.string.error_field_required)
            !isEmailValid(emailStr) -> getString(R.string.error_invalid_email)
            else -> null
        }
        password.error = when {
            checkPassword(passwordStr) -> getString(R.string.error_invalid_password)
            else -> null
        }
        // There was an error; don't attempt login and focus the first
        // form field with an error.
        when {
            email.error != null -> email
            password.error != null -> password
            else -> null
        }?.requestFocus()

        if (checkEmail(emailStr) || checkPassword(passwordStr)) {
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            UserLoginTask(this, R.id.turbolinks_view).execute(true)
        }
    }

    private fun checkEmail(emailStr: String): Boolean =
            TextUtils.isEmpty(emailStr) || !isEmailValid(emailStr)

    private fun checkPassword(passwordStr: String): Boolean =
            TextUtils.isEmpty(passwordStr) || !isPasswordValid(passwordStr)

    private fun isEmailValid(email: String): Boolean {
        val regex =
                """^[-a-z0-9!#${'$'}%&'*+\/=?^_`{|}~]+(?:\.[-a-z0-9!#${'$'}%&'*+\/=?^_`{|}~]+)*@(?:[a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\.)*(?:aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])${'$'}""".toRegex()
        return email.matches(regex)
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        loaderManager.initLoader(0, null, this)
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
                    }
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class UserLoginTask(loginActivity: LoginActivity, id: Int)
        : AttemptLogin(
            activityReference = WeakReference(loginActivity),
            adapterReference = WeakReference(loginActivity),
            viewID = id) {

        //var profileApiMessage: Response<CredentialsMessage>? = null

        override fun doInBackground(vararg params: Boolean?): Response<CredentialsMessage>? {
            val activity = activityReference.get() as? LoginActivity ?: return null
            // Save credentials to preference
            Preferences.init(activity)
            Preferences.username = activity.email.text.toString()
            Preferences.password = activity.password.text.toString()
            return super.doInBackground(*params)
        }


        override fun onPostExecute(result: Response<CredentialsMessage>?) {
            val activity = activityReference.get() as? LoginActivity ?: return
            if(activity.isFinishing || activity.isDestroyed) return
            activity.mAuthTask = null
            activity.showProgress(false)

            if (result?.isSuccessful == true) {
                activity.profileApiMessage = result.body()
            } else {
                activity.password.error = if(result == null && errorMessage != null)
                    errorMessage
                else
                    activity.getString(R.string.error_incorrect_password)
                activity.password.requestFocus()
            }
            super.onPostExecute(result)
        }

        override fun onCancelled() {
            val activity = activityReference.get() as? LoginActivity ?: return
            if(activity.isFinishing || activity.isDestroyed) return
            activity.mAuthTask = null
            activity.showProgress(false)
        }
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0

    }
}