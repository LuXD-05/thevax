package com.luxd.thevax.db.DAOs

import android.content.ContentValues
import android.database.Cursor
import com.luxd.thevax.db.entities.Therapy
import android.database.sqlite.SQLiteDatabase
import com.luxd.thevax.db.entities.User

class TherapyDAO(private val db: SQLiteDatabase) {

    /**
     * Adds a therapy to an user
     * @param id the id of the user to add
     * @param therapy the therapy to add to the user
     * @return TODO
     */
    fun addForUser(id: Int, therapy: Therapy): Int {
        return 0
    }

}