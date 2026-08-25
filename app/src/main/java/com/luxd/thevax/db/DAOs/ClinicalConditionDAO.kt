package com.luxd.thevax.db.DAOs

import com.luxd.thevax.db.entities.ClinicalCondition
import android.database.sqlite.SQLiteDatabase

class ClinicalConditionDAO(private val db: SQLiteDatabase) {

    /**
     * Updates a user's clinical conditions
     * @param userId the id of the user
     * @param conditionIds the ids of the conditions to set for the user
     * @return true if the update was successful, false otherwise
     */
    fun updateForUser(userId: Int, conditionIds: List<Int>): Boolean {
        var success = true

        db.beginTransaction()

        try {
            // Deletes all previous user's conditions
            db.delete("user_conditions", "user_id = ?", arrayOf(userId.toString()))

            // Insert if list not empty
            if (!conditionIds.isNullOrEmpty()) {
                // Dynamically builds the query & parameters from fieldsToUpdate with placeholders
                val valuesPlaceholders = conditionIds.joinToString(", ") { "(?, ?)" }
                val sql = "INSERT INTO user_conditions (user_id, condition_id) VALUES $valuesPlaceholders"

                // Builds the flat list of args
                val args = mutableListOf<String>()
                conditionIds.forEach { ccId ->
                    args.add(userId.toString())
                    args.add(ccId.toString())
                }

                // Executes query
                db.execSQL(sql, args.toTypedArray())
            }

            db.setTransactionSuccessful()

        } catch (e: Exception) {
            success = false
        } finally {
            db.endTransaction()
        }

        return success
    }

    /**
     * Gets all available conditions in the system
     * @return the list of clinical conditions
     */
    fun getAll(): List<ClinicalCondition> {
        val cursor = db.rawQuery("SELECT * FROM conditions ORDER BY name ASC", null)
        val conditions = mutableListOf<ClinicalCondition>()
        cursor.use {
            while (it.moveToNext()) {
                conditions.add(
                    ClinicalCondition(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        conditionName = it.getString(it.getColumnIndexOrThrow("name"))
                    )
                )
            }
        }
        return conditions
    }

    /**
     * Gets all conditions for a specific user
     * @param userId the id of the user
     * @return the list of clinical conditions
     */
    fun getConditionsForUser(userId: Int): List<ClinicalCondition> {
        val query = """
            SELECT c.id, c.name 
            FROM conditions c
            JOIN user_conditions uc ON c.id = uc.condition_id
            WHERE uc.user_id = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        val conditions = mutableListOf<ClinicalCondition>()

        cursor.use {
            while (it.moveToNext()) {
                conditions.add(
                    ClinicalCondition(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        conditionName = it.getString(it.getColumnIndexOrThrow("name"))
                    )
                )
            }
        }
        return conditions
    }
}
