package com.luxd.thevax.db.entities

data class User(
    val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val firstName: String?,
    val lastName: String?,
    val age: Int,
    val sex: String
)