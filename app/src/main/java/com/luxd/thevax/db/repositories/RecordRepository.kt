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
	fun scheduleAppointment(userId: Int, vaccineId: Int, date: Long, notes: String? = null): Boolean {
		val record = Record(
			id = 0,
			userId = userId,
			vaccineId = vaccineId,
			status = "scheduled",
			date = date,
			notes = notes
		)
		return recordDAO.add(record) > 0
	}

	/**
	 * Updates appointment status (e.g. completed, cancelled)
	 */
	fun updateAppointmentStatus(recordId: Int, status: String, notes: String? = null): Boolean {
		return recordDAO.update(recordId, status, notes)
	}

	/**
	 * Gets all scheduled appointments for the calendar/list
	 */
	fun getScheduledAppointments(userId: Int): List<Record> {
		return recordDAO.getScheduledRecordsForUser(userId)
	}

	/**
	 * Gets all history (completed, missed, cancelled)
	 */
	fun getHistoryForUser(userId: Int): List<Record> {
		return recordDAO.getRecordsForUser(userId).filter { it.status != "scheduled" }
	}

}