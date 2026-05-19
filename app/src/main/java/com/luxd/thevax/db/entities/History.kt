package com.luxd.thevax.db.entities

data class History(
    val id: Long = 0,
    val userId: Long,
    val vaccineId: Long,
    val startDate: Long, 
    val lastAdministrationDate: Long, 
    val notes: String?
)