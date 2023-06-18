package com.example.a0509

import android.content.ContentValues

class ScheduleRepository(private val dbHelper: ScheduleDatabaseHelper) {
    private val db = dbHelper.getDatabase()

    fun insertSchedule(schedule: Schedule) {
        val values = ContentValues().apply {
            put("schedule_id", schedule.scheduleId)
            put("schedule_name", schedule.scheduleName)
            put("start_date", schedule.startDate)
            put("start_time", schedule.startTime)
            put("end_date", schedule.endDate)
            put("end_time", schedule.endTime)
            put("place", schedule.place)
            put("memo", schedule.memo)
            put("alarm_date", schedule.alarmDate)
            put("alarm_time", schedule.alarmTime)
        }

        db.insert("schedule", null, values)
    }

    fun getAllSchedules(): List<Schedule> {
        return dbHelper.getAllSchedules()
    }
}