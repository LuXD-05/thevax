package com.luxd.thevax.db.repositories

import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.Vaccine
import com.luxd.thevax.db.DAOs.UserDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VaccineRepository(dbHelper: DatabaseHelper) {

    private val database = dbHelper.writableDatabase
    private val userDAO = UserDAO(database)

    /**
     * Valuta lo stato di tutti i vaccini per l'utente attivo.
     * Ritorna una mappa o lista di coppie: Vaccino e il suo Stato stringa.
     */
    suspend fun evaluate(userId: Int): List<Pair<Vaccine, String>> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<Pair<Vaccine, String>>()

        // 1. Recupera l'utente usando il tuo UserDAO ufficiale
        val user = userDAO.findById(userId) ?: return@withContext emptyList()

        // 2. Query diretta per estrarre tutti i vaccini puri dalla tabella vaccines
        val vaccineQuery = "SELECT id, name, vaccine_type, min_age, max_age FROM vaccines"

        database.rawQuery(vaccineQuery, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val vaxId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val type = cursor.getString(cursor.getColumnIndexOrThrow("vaccine_type"))

                    val minAge = if (cursor.isNull(cursor.getColumnIndexOrThrow("min_age"))) null else cursor.getInt(cursor.getColumnIndexOrThrow("min_age"))
                    val maxAge = if (cursor.isNull(cursor.getColumnIndexOrThrow("max_age"))) null else cursor.getInt(cursor.getColumnIndexOrThrow("max_age"))

                    val vaccine = Vaccine(vaxId, name, type, minAge, maxAge)
                    var calculatedStatus = "Opzionale" // Default se non ci sono vincoli (General Vaccine)

                    // VERIFICA 1: Vincolo di Età
                    if ((minAge != null && user.age < minAge) || (maxAge != null && user.age > maxAge)) {
                        calculatedStatus = "Controindicato"
                    } else {
                        // VERIFICA 2: Incrocio condizioni utente (user_conditions + vaccine_conditions)
                        val condQuery = """
                            SELECT vc.recommendation_status 
                            FROM vaccine_conditions vc
                            JOIN user_conditions uc ON vc.condition_id = uc.condition_id
                            WHERE uc.user_id = ? AND vc.vaccine_id = ?
                        """
                        var isRecommended = false
                        var isContraindicated = false

                        database.rawQuery(condQuery, arrayOf(userId.toString(), vaxId.toString())).use { condCursor ->
                            if (condCursor.moveToFirst()) {
                                do {
                                    val status = condCursor.getString(condCursor.getColumnIndexOrThrow("recommendation_status"))
                                    if (status == "contraindicated") isContraindicated = true
                                    if (status == "recommended") isRecommended = true
                                } while (condCursor.moveToNext())
                            }
                        }

                        // VERIFICA 3: Vincolo Terapia (therapy_vaccines)
                        var isLinkedToTherapy = false
                        if (user.therapyId != null && user.therapyId > 0) {
                            val therapyQuery = "SELECT 1 FROM therapy_vaccines WHERE therapy_id = ? AND vaccine_id = ?"
                            database.rawQuery(therapyQuery, arrayOf(user.therapyId.toString(), vaxId.toString())).use { thCursor ->
                                isLinkedToTherapy = thCursor.count > 0
                            }
                        }

                        // Gerarchia decisionale basata sul tuo DB
                        if (isContraindicated) {
                            calculatedStatus = "Controindicato"
                        } else if (isRecommended || isLinkedToTherapy) {
                            calculatedStatus = "Raccomandato"
                        }
                    }

                    // Aggiunge la coppia reale (Entità + Stato stringa) alla lista
                    resultList.add(Pair(vaccine, calculatedStatus))

                } while (cursor.moveToNext())
            }
        }
        return@withContext resultList
    }
}