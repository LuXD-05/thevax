package com.luxd.thevax.db.repositories

import com.luxd.thevax.db.entities.User
import com.luxd.thevax.db.entities.Therapy
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.RegisterDTO
import com.luxd.thevax.db.DAOs.UserDAO
import com.luxd.thevax.db.DAOs.TherapyDAO
import com.luxd.thevax.db.DAOs.ClinicalConditionDAO
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.services.SessionService
import com.luxd.thevax.services.PasswordService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(db: DatabaseHelper) {

	private val database by lazy { db.writableDatabase }
	private val userDAO by lazy { UserDAO(database) }
	private val therapyDAO by lazy { TherapyDAO(database) }
	private val conditionDAO by lazy { ClinicalConditionDAO(database) }

	/**
	 * Registers a user (+ eventual therapies & conditions)
	 * @param registerDTO the registration info object (User + Therapies + ClinicalConditions)
	 * @return true if the user was registered, false otherwise
	 */
	suspend fun register(registerDTO: RegisterDTO): User? {

		return withContext(Dispatchers.IO) {

			// Check for duplicate user (if email exists)
			if (userDAO.findByEmail(registerDTO.email) != null) return@withContext null

			// Creates an user with hashed password
			var user = User(
				email = registerDTO.email,
				passwordHash = PasswordService.hash(registerDTO.password),
				firstName = registerDTO.firstName,
				lastName = registerDTO.lastName,
				age = registerDTO.age,
				sex = registerDTO.sex,
				therapyId = registerDTO.therapyId
			)

			database.beginTransaction()

			try {
				// Adds user to db
				val userId = userDAO.add(user)
				if (userId <= 0) return@withContext null // fail check

				// Updates the local user object with its db id
				user = user.copy(id = userId)

				// Adds its clinical conditions in bulk
				if (registerDTO.conditions.isNotEmpty()) {
					// Rollback if fails
					if (!conditionDAO.updateForUser(userId, registerDTO.conditions.map { it.id }))
						return@withContext null
				}

				database.setTransactionSuccessful()

			} catch (e: Exception) {
				return@withContext null
			} finally {
				database.endTransaction()
			}

			// Saves user in session
			SessionService.getInstance().saveUser(user)
			user

		}
	}

	/**
	 * Logs in a user
	 * @param email the email of the user
	 * @param password the password of the user
	 * @return the user if found & password matches, otherwise null
	 */
	suspend fun login(email: String, password: String): User? {

		return withContext(Dispatchers.IO) {

			// Checks if user exists by email (otherwise returns null)
			val user = userDAO.findByEmail(email) ?: return@withContext null

			// Checks if password matches
			val passwordMatches = PasswordService.verify(password, user.passwordHash)

			// Returns user (& saves its id in session) if password matches, otherwise returns null
			if (passwordMatches) {
				// Saves user in session
				SessionService.getInstance().saveUser(user)
				user

			} else null

		}
	}

	/**
	 * Updates a user's profile information
	 * @param userId the id of the user to update
	 * @param fields the map of fields to update (column_name to value)
	 * @return true if the updated successfully, otherwise false
	 */
	@Suppress("UNCHECKED_CAST")
	suspend fun update(userId: Int, fields: Map<String, Any?>): Boolean {

		return withContext(Dispatchers.IO) {

			val userFields = fields.toMutableMap()
			val conditions = userFields.remove("conditions") as? List<ClinicalCondition>

			// Replaces password with hashed version (if modified)
			val password = userFields.remove("password") as? String
			if (!password.isNullOrBlank()) {
				userFields["password_hash"] = PasswordService.hash(password)
			}

			// Contains nested transacts --> each needs to be set succesful, else rollback
			database.beginTransaction()

			try {
				// Updates user in DB | if fails --> rollback
				if (userFields.isNotEmpty() && !userDAO.update(userId, userFields)) return@withContext false

				// Updates conditions | if fails --> rollback
				if (conditions != null && !conditionDAO.updateForUser(userId, conditions.map { it.id })) return@withContext false

				database.setTransactionSuccessful()

				// Updates user in session
				userDAO.findById(userId)?.let { updatedUser ->
					SessionService.getInstance().saveUser(updatedUser)
				}
				true

			} catch (e: Exception) {
				false
			} finally {
				database.endTransaction()
			}

		}
	}

	/**
	 * Gets a user by id
	 * @param userId the id of the user
	 * @return the user if found, null otherwise
	 */
	suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) { userDAO.findById(userId) }

	/**
	 * Gets all therapies
	 * @return the list of therapies
	 */
	suspend fun getTherapies(): List<Therapy> = withContext(Dispatchers.IO) { therapyDAO.getAll() }

	/**
	 * Gets all available conditions in the system
	 * @return the list of clinical conditions
	 */
	suspend fun getConditions(): List<ClinicalCondition> = withContext(Dispatchers.IO) { conditionDAO.getAll() }

	/**
	 * Gets a specific user's selected therapy
	 * @param userId the id of the user
	 * @return the user's selected therapy if found, "Nessuna" otherwise
	 */
	suspend fun getTherapyForUser(userId: Int): Therapy = withContext(Dispatchers.IO) { therapyDAO.getTherapyForUser(userId) }

	/**
	 * Gets all conditions for a specific user
	 * @param userId the id of the user
	 * @return the list of clinical conditions
	 */
	suspend fun getConditionsForUser(userId: Int): List<ClinicalCondition> = withContext(Dispatchers.IO) { conditionDAO.getConditionsForUser(userId) }
}
