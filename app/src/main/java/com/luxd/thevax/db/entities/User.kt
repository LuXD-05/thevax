package com.luxd.thevax.db.entities

/**
 * User model
 */
data class User(
    val id: Int = 0,
    val email: String,
    var passwordHash: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val sex: String
)

/**
 * Used to pass register information from RegisterActivity to UserRepository
 */
data class RegisterDTO(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val sex: String,
    val therapies: List<Therapy>,
    val conditions: List<ClinicalCondition>
)