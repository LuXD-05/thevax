package com.luxd.thevax.db.DAOs

import android.content.ContentValues
import android.database.Cursor
import com.luxd.thevax.db.entities.ClinicalCondition
import android.database.sqlite.SQLiteDatabase

class ClinicalConditionDAO(private val db: SQLiteDatabase) {

    /**
     * Adds a clinical condition to an user
     * @param id the id of the user to add
     * @param clinicalCondition the condition to add to the user
     * @return TODO
     */
    fun addForUser(id: Int, clinicalCondition: ClinicalCondition): Int {
        return 0
    }

}