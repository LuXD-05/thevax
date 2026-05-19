package com.luxd.thevax.db.entities

data class ClinicalCondition(
    val id: Long = 0,
    val userId: Long,
    val conditionName: String,
)