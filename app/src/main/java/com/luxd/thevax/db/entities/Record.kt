package com.luxd.thevax.db.entities

data class Record(
	val id: Int = 0,
	val userId: Int,
	val vaccineId: Int,
	var status: String,
	var date: Long,
	val notes: String?
)
