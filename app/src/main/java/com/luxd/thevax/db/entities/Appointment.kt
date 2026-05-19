package com.luxd.thevax.db.entities

data class Appointment(
    val id: Long = 0,
    val vaxEvaluationId: Int,
    val scheduledDate: Long, 
    val status: String,
    val notes: String?
)