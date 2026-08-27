package com.luxd.thevax.db.entities

data class Vaccine(
    val id: Int = 0,
    val name: String,
    val vaccineType: String,
    val minAge: Int?,
    val maxAge: Int?
) : java.io.Serializable