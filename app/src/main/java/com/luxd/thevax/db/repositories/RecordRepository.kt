package com.luxd.thevax.db.repositories

import com.luxd.thevax.db.DAOs.RecordDAO
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordRepository(db: DatabaseHelper) {

	private val database by lazy { db.writableDatabase }
	private val recordDAO by lazy { RecordDAO(database) }

	/**
	 * Schedules a vaccination appointment
	 * @param record the record to add
	 * @return true if the record was added, false otherwise
	 */
	suspend fun add(record: Record): Boolean = withContext(Dispatchers.IO) { recordDAO.add(record) > 0 }

	/**
	 * Updates appointment status
	 * @param record the record to update
	 * @return true if the record was updated, false otherwise
	 */
	suspend fun update(record: Record): Boolean = withContext(Dispatchers.IO) { recordDAO.update(record) }

	/**
	 * Deletes an appointment
	 * @param recordId the id of the record to delete
	 * @return true if the record was deleted, false otherwise
	 */
	suspend fun delete(recordId: Int): Boolean = withContext(Dispatchers.IO) { recordDAO.delete(recordId); true }

	/**
	 * Gets all scheduled appointments for the calendar/list
	 * @param userId the id of the user
	 * @return the list of records
	 */
	suspend fun getRecordsForUser(userId: Int): List<Record> = withContext(Dispatchers.IO) { recordDAO.getRecordsForUser(userId) }

	/**
	 * Marks all missed records as cancelled
	 * @param userId the id of the user
	 * @param todayStart the start of the current day
	 */
	suspend fun markMissedRecords(userId: Int, todayStart: Long) = withContext(Dispatchers.IO) { recordDAO.markMissedRecords(userId, todayStart) }

}