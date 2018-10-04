package ru.skyderby.wings.app.helpers

import android.content.Context
import android.content.SharedPreferences

object Preferences {

    private const val NAME = "ru.skyderby.wings.app.preferences"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    // list of app specific preferences
    private val AP_USERNAME = Pair("ru.skyderby.wings.app.username", "")
    private val AP_PASSWORD = Pair("ru.skyderby.wings.app.password", "")
    private val AP_TIMEOUT = Pair("ru.skyderby.wings.app.timeout", 5)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var username: String
        get() = preferences.getString(AP_USERNAME.first, AP_USERNAME.second)?: ""
        set(value) = preferences.edit {
            it.putString(AP_USERNAME.first, value)
        }

    var password: String
        get() = preferences.getString(AP_PASSWORD.first, AP_PASSWORD.second)?: ""
        set(value) = preferences.edit {
            it.putString(AP_PASSWORD.first, value)
        }

    var timeout: Int
        get() = preferences.getInt(AP_TIMEOUT.first, AP_TIMEOUT.second)
        set(value) = preferences.edit {
            it.putInt(AP_TIMEOUT.first, value)
        }

}