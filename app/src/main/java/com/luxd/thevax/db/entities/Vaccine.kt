package com.luxd.thevax.db.entities

data class Vaccine(
    val id: Long = 0,
    val name: String,
    val vaccineType: String,
    val targetTherapyId: Int?
)