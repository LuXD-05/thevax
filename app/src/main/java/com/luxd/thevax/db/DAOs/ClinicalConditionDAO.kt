package com.luxd.thevax.db.DAOs

import android.content.ContentValues
import android.database.Cursor
import com.luxd.thevax.db.entities.ClinicalCondition
import android.database.sqlite.SQLiteDatabase

class ClinicalConditionDAO(private val db: SQLiteDatabase) {

    /**
     * Adds a clinical condition to an user
     * @param userId the id of the user
     * @param clinicalCondition the condition to add
     * @return the id of the user_condition link
     */
    fun addForUser(userId: Int, clinicalCondition: ClinicalCondition): Long {
        // 1. Find or create condition
        var conditionId = getConditionId(clinicalCondition.conditionName)
        if (conditionId == -1L) {
            val cv = ContentValues().apply {
                put("name", clinicalCondition.conditionName)
            }
            conditionId = db.insert("conditions", null, cv)
        }

        // 2. Link user to condition
        val cvLink = ContentValues().apply {
            put("user_id", userId)
            put("condition_id", conditionId)
        }
        return db.insert("user_conditions", null, cvLink)
    }

    private fun getConditionId(name: String): Long {
        val cursor = db.rawQuery("SELECT id FROM conditions WHERE name = ? LIMIT 1", arrayOf(name))
        return cursor.use {
            if (it.moveToFirst()) it.getLong(0) else -1L
        }
    }
}
