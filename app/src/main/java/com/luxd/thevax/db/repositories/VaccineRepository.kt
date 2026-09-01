package com.luxd.thevax.db.repositories

import com.luxd.thevax.db.DAOs.VaccineDAO
import com.luxd.thevax.db.DAOs.RecordDAO
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.db.entities.Vaccine

class VaccineRepository(db: DatabaseHelper) {

	private val database by lazy { db.writableDatabase }
	private val vaccineDAO by lazy { VaccineDAO(database) }
	private val recordDAO by lazy { RecordDAO(database) }

	/**
	 * Gets all vaccines
	 * @return the list of vaccines
	 */
	fun getAll(): List<Vaccine> {
		return vaccineDAO.getAll()
	}

	/**
	 * Evaluates all vaccines in db for a user (therapy + conditions + age rules)
	 * @param user the user to evaluate for
	 * @param userConditions the user's clinical conditions
	 * @return the list of evaluations, ordered: recommended -> optional -> contraindicated
	 */
	fun evaluateVaccinesForUser(user: User, userConditions: List<ClinicalCondition>): List<Pair<Vaccine, String>> {
		// Loads all vaccines + the rules involving the user's therapy & conditions (one query each)
		val vaccines = vaccineDAO.getAll()
		val scheduledVaccineIds = recordDAO.getRecordsForUser(user.id).map { it.vaccineId } //TODO: capire record status (cosa fai se record cancelled?)
		val therapyStatuses = vaccineDAO.getStatusesForTherapy(user.therapyId)
		val conditionStatuses = vaccineDAO.getStatusesForConditions(userConditions.map { it.id })

		val evaluations = mutableListOf<Pair<Vaccine, String>>()

		for (vaccine in vaccines) {
			// RECORD CHECK: if already scheduled --> skipped
			if (scheduledVaccineIds.contains(vaccine.id)) continue

			// AGE CHECK: if out of range --> skipped
			if (vaccine.minAge != null && user.age < vaccine.minAge) continue
			if (vaccine.maxAge != null && user.age > vaccine.maxAge) continue

			// THERAPY CHECK: if user's therapy contraindicates it --> contraindicated
			if (therapyStatuses[vaccine.id] == "contraindicated") {
				evaluations.add(Pair(vaccine, "contraindicated"))
				continue
			}

			// CONDITION CHECK: any user condition contraindicates it --> contraindicated
			if (conditionStatuses[vaccine.id].orEmpty().any { it == "contraindicated" }) {
				evaluations.add(Pair(vaccine, "contraindicated"))
				continue
			}

			// RECOMMENDED CHECK: if recommended for therapy or any condition --> recommended
			val isRecommended = therapyStatuses[vaccine.id] == "recommended"
				|| conditionStatuses[vaccine.id].orEmpty().any { it == "recommended" }

			// otherwise --> optional
			val status = if (isRecommended) "recommended" else "optional"

			evaluations.add(Pair(vaccine, status))
		}

		// Orders the list: recommended --> optional --> contraindicated
		val statusOrder = mapOf("recommended" to 0, "optional" to 1, "contraindicated" to 2)
		return evaluations.sortedBy { statusOrder[it.second] ?: 3 }
	}

}