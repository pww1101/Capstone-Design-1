package com.example.a0509

data class Schedule(
    val scheduleId: String,
    val scheduleName: String,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String,
    val place: String?,
    val memo: String?,
    val alarmDate: String?,
    val alarmTime: String?
)
