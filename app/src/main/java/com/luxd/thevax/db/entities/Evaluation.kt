package com.luxd.thevax.db.entities

data class Evaluation(
    val id: Long = 0,
    val userId: Long,
    val vaccineId: Long,
    val status: String,
    val notes: String?
)