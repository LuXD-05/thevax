package com.luxd.thevax.db.repositories

import android.content.Context
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.db.entities.RegisterDTO
import com.luxd.thevax.db.DAOs.UserDAO
import com.luxd.thevax.db.DAOs.TherapyDAO
import com.luxd.thevax.db.DAOs.ClinicalConditionDAO
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.services.SessionService
import com.luxd.thevax.services.PasswordService

class UserRepository(app: Context, db: DatabaseHelper) {

    // TODO: handle operations in a background thread (like async/await, in order to not make the app crash & keep the main thread for the UI)

    private val database = db.writableDatabase
    private val userDAO = UserDAO(database)
    private val therapyDAO = TherapyDAO(database)
    private val conditionDAO = ClinicalConditionDAO(database)
    private val session = SessionService.getInstance(app)

    /**
     * Registers a user (+ eventual therapies & conditions)
     * @param registerDTO the registration info object (User + Therapies + ClinicalConditions)
     * @return true if the user was registered, false otherwise
     */
    fun register(registerDTO: RegisterDTO): User? {
        // Check for duplicate user (if email exists)
        if (userDAO.findByEmail(registerDTO.email) != null)
            return null

        // Creates an user with hashed password
        val user = User(
            email = registerDTO.email,
            passwordHash = PasswordService.hash(registerDTO.password),
            firstName = registerDTO.firstName,
            lastName = registerDTO.lastName,
            age = registerDTO.age,
            sex = registerDTO.sex
        )

        database.beginTransaction()
        val userId: Int
        try {
            userId = userDAO.add(user)
            if (userId <= 0) return null

            // If there are any, saves all therapies
            registerDTO.therapies.forEach { therapy ->
                if (therapyDAO.addForUser(userId, therapy) <= 0) return null
            }

            //Same for clinical conditions
            registerDTO.conditions.forEach { condition ->
                if (conditionDAO.addForUser(userId, condition) <= 0) return null
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }

        // Saves user in session
        session.saveUserId(userId)

        return user.copy(id = userId)
    }

    /**
     * Logs in a user
     * @param email the email of the user
     * @param password the password of the user
     * @return the user if found & password matches, otherwise null
     */
    fun login(email: String, password: String): User? {
        // Checks if user exists by email (otherwise returns null)
        val user = userDAO.findByEmail(email) ?: return null

        // Checks if password matches
        val passwordMatches = PasswordService.verify(password, user.passwordHash)

        // Returns user (& saves its id in session) if password matches, otherwise returns null
        if (passwordMatches) {
            // Saves user in session
            session.saveUserId(user.id)

            return user

        } else return null
    }

}
