package com.luxd.thevax.services

import android.content.Context
import android.content.SharedPreferences

class SessionService(context: Context) {

    // SharedPreferences db: key-value store (accessible only by this app with Context.MODE_PRIVATE)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("thevax_session", Context.MODE_PRIVATE)

    /**
     * Saves the passed userId in the prefs
     * @param id the id of the user
     */
    fun saveUserId(id: Int) {
        prefs.edit().putInt(KEY_USER_ID, id).apply()
    }

    /**
     * Gets the userId in the prefs
     * Returns -1 if not found
     */
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    /**
     * Checks if an user is logged in
     * @return true if an user is logged in, otherwise false
     */
    fun isLoggedIn(): Boolean {
        return getUserId() != -1
    }

    /**
     * Clears the prefs
     */
    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        // The only key-value to be saved in the prefs
        private const val KEY_USER_ID = "user_id"

        @Volatile
        private var _instance: SessionService? = null

        /**
         * Returns the singleton instance of SessionService (with checks on null, race conditions...)
         */
        fun getInstance(context: Context): SessionService {
            return _instance ?: synchronized(this) {
                _instance ?: SessionService(context.applicationContext).also { _instance = it }
            }
        }
    }

}