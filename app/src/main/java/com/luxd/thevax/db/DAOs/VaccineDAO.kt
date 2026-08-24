package com.luxd.thevax.db.DAOs

import android.database.sqlite.SQLiteDatabase
import com.luxd.thevax.db.entities.Vaccine

class VaccineDAO(private val db: SQLiteDatabase) {

	/**
	 * Gets all vaccines from the DB
	 */
	fun findAll(): List<Vaccine> {
		val cursor = db.rawQuery("SELECT id, name, vaccine_type, min_age, max_age FROM vaccines", null)
		return cursor.use {
			val list = mutableListOf<Vaccine>()
			while (cursor.moveToNext()) {
				list.add(
					Vaccine(
						id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
						name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
						vaccineType = cursor.getString(cursor.getColumnIndexOrThrow("vaccine_type")),
						minAge = if (cursor.isNull(cursor.getColumnIndexOrThrow("min_age"))) null else cursor.getInt(cursor.getColumnIndexOrThrow("min_age")),
						maxAge = if (cursor.isNull(cursor.getColumnIndexOrThrow("max_age"))) null else cursor.getInt(cursor.getColumnIndexOrThrow("max_age"))
					)
				)
			}
			list
		}
	}

}