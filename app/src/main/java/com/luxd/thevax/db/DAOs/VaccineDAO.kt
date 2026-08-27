package com.luxd.thevax.db.DAOs

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.luxd.thevax.db.entities.Vaccine

class VaccineDAO(private val db: SQLiteDatabase) {

	/**
	 * Gets all vaccines
	 * @return the list of vaccines
	 */
	fun getAll(): List<Vaccine> {
		val cursor = db.rawQuery("SELECT * FROM vaccines ORDER BY name ASC", null)
		val vaccines = mutableListOf<Vaccine>()
		cursor.use {
			while (it.moveToNext()) vaccines.add(it.toVaccine())
		}
		return vaccines
	}

	/**
	 * Gets the therapy rules for a therapy
	 * @param therapyId the id of the user's therapy (null = no therapy = no rules)
	 * @return Map<VaccineId, Status>
	 */
	fun getStatusesForTherapy(therapyId: Int?): Map<Int, String> {
		if (therapyId == null) return emptyMap()

		val cursor = db.rawQuery(
			"SELECT vaccine_id, recommendation_status FROM therapy_vaccines WHERE therapy_id = ?",
			arrayOf(therapyId.toString())
		)
		val statuses = mutableMapOf<Int, String>()
		cursor.use {
			while (it.moveToNext()) {
				statuses[it.getInt(it.getColumnIndexOrThrow("vaccine_id"))] =
					it.getString(it.getColumnIndexOrThrow("recommendation_status"))
			}
		}
		return statuses
	}

	/**
	 * Gets the condition rules for a set of conditions
	 * @param conditionIds the ids of the user's conditions
	 * @return Map<VaccineId, List<Status>>
	 */
	fun getStatusesForConditions(conditionIds: List<Int>): Map<Int, List<String>> {
		if (conditionIds.isEmpty()) return emptyMap()

		val placeholders = conditionIds.joinToString(", ") { "?" }
		val cursor = db.rawQuery(
			"SELECT vaccine_id, recommendation_status FROM vaccine_conditions WHERE condition_id IN ($placeholders)",
			conditionIds.map { it.toString() }.toTypedArray()
		)
		val statuses = mutableMapOf<Int, MutableList<String>>()
		cursor.use {
			while (it.moveToNext()) {
				val vaccineId = it.getInt(it.getColumnIndexOrThrow("vaccine_id"))
				val status = it.getString(it.getColumnIndexOrThrow("recommendation_status"))
				statuses.getOrPut(vaccineId) { mutableListOf() }.add(status)
			}
		}
		return statuses
	}

	private fun Cursor.toVaccine() = Vaccine(
		id = getInt(getColumnIndexOrThrow("id")),
		name = getString(getColumnIndexOrThrow("name")),
		vaccineType = getString(getColumnIndexOrThrow("vaccine_type")),
		minAge = if (isNull(getColumnIndexOrThrow("min_age"))) null else getInt(getColumnIndexOrThrow("min_age")),
		maxAge = if (isNull(getColumnIndexOrThrow("max_age"))) null else getInt(getColumnIndexOrThrow("max_age"))
	)
}