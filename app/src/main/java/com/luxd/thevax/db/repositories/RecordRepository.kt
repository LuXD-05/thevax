package com.luxd.thevax.db.repositories

import com.luxd.thevax.db.DAOs.RecordDAO
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.Record

class RecordRepository(db: DatabaseHelper) {

	private val database by lazy { db.writableDatabase }
	private val recordDAO by lazy { RecordDAO(database) }

	/**
	 * Schedules a vaccination appointment
	 */
	fun add(record: Record): Boolean {
		return recordDAO.add(record) > 0
	}

	/**
	 * Updates appointment status
	 */
	fun update(record: Record): Boolean {
		return recordDAO.update(record)
	}

	/**
	 * Deletes an appointment
	 */
	fun delete(recordId: Int): Boolean {
		recordDAO.delete(recordId)
		return true
	}

	/**
	 * Gets all scheduled appointments for the calendar/list
	 */
	fun getRecordsForUser(userId: Int): List<Record> {
		return recordDAO.getRecordsForUser(userId)
	}

	fun markMissedRecords(userId: Int, todayStart: Long) = recordDAO.markMissedRecords(userId, todayStart)

}