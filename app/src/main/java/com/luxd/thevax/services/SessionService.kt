package com.luxd.thevax.services

import android.content.Context
import android.content.SharedPreferences
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.db.repositories.UserRepository
import com.luxd.thevax.db.DatabaseHelper

class SessionService(context: Context) {

    // SharedPreferences db: key-value store (accessible only by this app with Context.MODE_PRIVATE)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("thevax_session", Context.MODE_PRIVATE)

    // Memory cache for the current user
    private var _user: User? = null

    private var repo: UserRepository = UserRepository(context, DatabaseHelper(context))

    /**
     * Saves the user in memory cache and the ID in persistent storage
     * @param user the user object
     */
    fun saveUser(user: User) {
        _user = user
        prefs.edit().putInt(KEY_USER_ID, user.id).apply()
    }

    /**
     * Gets the current user. Returns from cache if available, 
     * otherwise loads from DB using the saved ID.
     * @return the current User or null if not logged in
     */
    fun getUser(): User? {
        if (_user != null) return _user

        val userId = getUserId()
        if (userId != -1) {
            _user = repo.getUserById(userId)
        }
        return _user
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
     * Clears the prefs and memory cache
     */
    fun clear() {
        _user = null
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