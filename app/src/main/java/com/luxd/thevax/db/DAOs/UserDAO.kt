package com.luxd.thevax.db.DAOs

import android.content.ContentValues
import android.database.Cursor
import com.luxd.thevax.db.entities.User
import android.database.sqlite.SQLiteDatabase

class UserDAO(private val db: SQLiteDatabase) {

    // TODO: test all functions

    /**
     * Adds a user
     * @param user the user to add
     */
    fun add(user: User): Int {
        val cv = ContentValues().apply {
            put("email", user.email)
            put("password_hash", user.passwordHash)
            put("first_name", user.firstName)
            put("last_name", user.lastName)
            put("age", user.age)
            put("sex", user.sex)
        }
        return db.insert("users", null, cv).toInt()
    }

    /**
     * Updates an user
     * @param id the id of the user
     * @param fieldsToUpdate a map of the fields to update
     * @return true if the user was updated, false otherwise
     */
    fun update(id: Int, fieldsToUpdate: Map<String, Any?>): Boolean {
        if (fieldsToUpdate.isEmpty()) return false

        val setClause = fieldsToUpdate.keys.joinToString(", ") { "$it = ?" }
        val sql = "UPDATE users SET $setClause WHERE id = ?"
        val args = (fieldsToUpdate.values + id).toTypedArray()

        return try {
            db.execSQL(sql, args)
            true
        } catch (e: Exception) {
            // TODO: (build &) return error message?
            false
        }
    }

    /**
     * Deletes an user
     * @param id the id of the user
     * @return true if the user was deleted, false otherwise
     */
    fun delete(id: Int): Boolean {
        return db.delete("users", "id = ?", arrayOf(id.toString())) == 1
    }

    /**
     * Finds an user by id
     * @param id the id of the user
     * @return the user if found, null otherwise
     */
    fun findById(id: Int): User? {
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ? LIMIT 1", arrayOf(id.toString()))
        return cursor.use {
            if (it.moveToFirst()) it.toUser() else null
        }
    }

    /**
     * Finds an user by email
     * @param email the email of the user
     * @return the user if found, null otherwise
     */
    fun findByEmail(email: String): User? {
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ? LIMIT 1", arrayOf(email))
        return cursor.use {
            if (it.moveToFirst()) it.toUser() else null
        }
    }

    private fun Cursor.toUser() = User(
        id = getInt(getColumnIndexOrThrow("id")),
        email = getString(getColumnIndexOrThrow("email")),
        passwordHash = getString(getColumnIndexOrThrow("password_hash")),
        firstName = getString(getColumnIndexOrThrow("first_name")),
        lastName = getString(getColumnIndexOrThrow("last_name")),
        age = getInt(getColumnIndexOrThrow("age")),
        sex = getString(getColumnIndexOrThrow("sex"))
    )

}