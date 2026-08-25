package com.luxd.thevax.db.DAOs

import android.database.Cursor
import com.luxd.thevax.db.entities.Therapy
import android.database.sqlite.SQLiteDatabase

class TherapyDAO(private val db: SQLiteDatabase) {

    fun getAll(): List<Therapy> {
        val therapies = mutableListOf<Therapy>()
        val cursor = db.rawQuery("SELECT * FROM therapies", null)
        cursor.use {
            while (it.moveToNext()) {
                therapies.add(it.toTherapy())
            }
        }
        return therapies
    }

    fun findById(id: Int): Therapy? {
        val cursor = db.rawQuery("SELECT * FROM therapies WHERE id = ?", arrayOf(id.toString()))
        return cursor.use {
            if (it.moveToFirst()) it.toTherapy() else null
        }
    }

    private fun Cursor.toTherapy() = Therapy(
        id = getInt(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        description = getString(getColumnIndexOrThrow("description"))
    )
}
