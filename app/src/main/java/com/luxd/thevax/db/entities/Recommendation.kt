package com.luxd.thevax.db.entities

data class Recommendation(
    val id: Long = 0,
    val vaccineId: Long,
    val minAge: Int?,
    val maxAge: Int?,
    val targetConditionId: Int?,
    val recommendationStatus: String,
    val notes: String?
)