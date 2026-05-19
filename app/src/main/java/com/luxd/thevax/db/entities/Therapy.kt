package com.luxd.thevax.db.entities

data class Therapy(
    val id: Long = 0,
    val userId: Long,
    val drugName: String,
    val drugCategory: String,
)