package com.luxd.thevax.db.DAOs

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.luxd.thevax.db.entities.Record

class RecordDAO(private val db: SQLiteDatabase) {

	fun add(record: Record): Int {
		val cv = ContentValues().apply {
			put("user_id", record.userId)
			put("vaccine_id", record.vaccineId)
			put("status", record.status)
			put("date", record.date)
			put("notes", record.notes)
		}
		return db.insert("records", null, cv).toInt()
	}

	fun update(record: Record): Boolean {
		val cv = ContentValues().apply {
			put("status", record.status)
			put("date", record.date)
			put("notes", record.notes)
		}
		return db.update("records", cv, "id = ?", arrayOf(record.id.toString())) > 0
	}

	fun delete(recordId: Int) {
		db.delete("records", "id = ?", arrayOf(recordId.toString()))
	}

	fun getRecordsForUser(userId: Int): List<Record> {
		val cursor = db.rawQuery("SELECT * FROM records WHERE user_id = ? ORDER BY date ASC", arrayOf(userId.toString()))
		val records = mutableListOf<Record>()
		cursor.use {
			while (it.moveToNext()) {
				records.add(it.toRecord())
			}
		}
		return records
	}

	fun getScheduledRecordsForUser(userId: Int): List<Record> {
		val cursor = db.rawQuery("SELECT * FROM records WHERE user_id = ? AND status = ? ORDER BY date ASC", arrayOf(userId.toString(), "scheduled"))
		val records = mutableListOf<Record>()
		cursor.use {
			while (it.moveToNext()) {
				records.add(it.toRecord())
			}
		}
		return records
	}

	// Sets 'missed' records with status 'missed' (scheduled and not completed today)
	fun markMissedRecords(userId: Int, todayStart: Long) {
		val cv = ContentValues().apply {
			put("status", "missed")
		}
		db.update(
			"records",
			cv,
			"user_id = ? AND status = ? AND date < ?",
			arrayOf(userId.toString(), "scheduled", todayStart.toString())
		)
	}

	private fun Cursor.toRecord() = Record(
		id = getInt(getColumnIndexOrThrow("id")),
		userId = getInt(getColumnIndexOrThrow("user_id")),
		vaccineId = getInt(getColumnIndexOrThrow("vaccine_id")),
		status = getString(getColumnIndexOrThrow("status")),
		date = getLong(getColumnIndexOrThrow("date")),
		notes = if (isNull(getColumnIndexOrThrow("notes"))) null else getString(getColumnIndexOrThrow("notes"))
	)
}