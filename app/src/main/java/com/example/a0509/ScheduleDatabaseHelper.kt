package com.example.a0509

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScheduleDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "schedules.db"
        private const val DATABASE_VERSION = 1
    }

    private lateinit var mydb: SQLiteDatabase

    override fun onCreate(db: SQLiteDatabase) {
        mydb = db

        val createTableQuery = """
            CREATE TABLE "schedule" (
                "schedule_id"   TEXT NOT NULL,
                "schedule_name"   TEXT NOT NULL,
                "start_date"   TEXT NOT NULL,
                "start_time"   TEXT NOT NULL,
                "end_date"   TEXT NOT NULL,
                "end_time"   TEXT NOT NULL,
                "place"   TEXT,
                "memo"   TEXT,
                "alarm_date"   TEXT,
                "alarm_time"   TEXT,
                PRIMARY KEY("schedule_id")
            );
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("""DROP TABLE IF EXISTS "schedule"""")
        onCreate(db);
    }

    fun getDatabase(): SQLiteDatabase {
        if(!::mydb.isInitialized) {
            mydb = this.writableDatabase
        }
        return mydb
    }

    fun getAllSchedules(): List<Schedule> {
        val schedules = mutableListOf<Schedule>()

        val query = "SELECT * FROM schedule"
        val cursor = mydb.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val scheduleIdIndex = cursor.getColumnIndex("schedule_id")
            val scheduleId = if (scheduleIdIndex != -1) cursor.getString(scheduleIdIndex) else ""

            val scheduleNameIndex = cursor.getColumnIndex("schedule_name")
            val scheduleName = if (scheduleNameIndex != -1) cursor.getString(scheduleNameIndex) else ""

            val startDateIndex = cursor.getColumnIndex("start_date")
            val startDate = if (startDateIndex != -1) cursor.getString(startDateIndex) else ""

            val startTimeIndex = cursor.getColumnIndex("start_time")
            val startTime = if (startTimeIndex != -1) cursor.getString(startTimeIndex) else ""

            val endDateIndex = cursor.getColumnIndex("end_date")
            val endDate = if (endDateIndex != -1) cursor.getString(endDateIndex) else ""

            val endTimeIndex = cursor.getColumnIndex("end_time")
            val endTime = if (endTimeIndex != -1) cursor.getString(endTimeIndex) else ""

            val placeIndex = cursor.getColumnIndex("place")
            val place = if (placeIndex != -1) cursor.getString(placeIndex) else ""

            val memoIndex = cursor.getColumnIndex("memo")
            val memo = if (memoIndex != -1) cursor.getString(memoIndex) else ""

            val alarmDateIndex = cursor.getColumnIndex("alarm_date")
            val alarmDate = if (alarmDateIndex != -1) cursor.getString(alarmDateIndex) else ""

            val alarmTimeIndex = cursor.getColumnIndex("alarm_time")
            val alarmTime = if (alarmTimeIndex != -1) cursor.getString(alarmTimeIndex) else ""

            val schedule = Schedule(
                scheduleId,
                scheduleName,
                startDate,
                startTime,
                endDate,
                endTime,
                place,
                memo,
                alarmDate,
                alarmTime
            )
            schedules.add(schedule)
        }

        cursor.close()
        return schedules
    }
}