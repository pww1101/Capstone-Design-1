package com.example.a0509

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.a0509.databinding.ActivityScheduleCreateBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale

class ScheduleCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleCreateBinding
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var dbHelper: ScheduleDatabaseHelper
    
    // 프로그램 상에서 사용되는 날짜 형식은 yyyy-MM-dd, 시간 형식은 hh:mm
    // 화면에 표시되는 날짜 형식은 yyyy년 MM월 dd일, 시간 형식은 hh시 mm분
    
    private fun dateToString(date: LocalDate): String {
        return "${"%04d".format(date.getYear())}-${"%02d".format(date.getMonthValue())}-${"%02d".format(date.getDayOfMonth())}"
    }
    
    private fun timeToString(date: LocalDateTime): String {
        return "${"%02d".format(date.getHour())}:${"%02d".format(date.getMinute())}"
    }
    
    private fun convertDateString(date: String): String {
        return "${date.substring(0..3)}년 ${date.substring(5..6)}월 ${date.substring(8..9)}일"
    }
    
    private fun convertTimeString(time: String): String {
        return "${time.substring(0..1)}시 ${time.substring(3..4)}분"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper= ScheduleDatabaseHelper(this)
        scheduleRepository = ScheduleRepository(dbHelper)
        
        val from=intent.getStringExtra("from")
        
        var scheduleId: String=""
        var scheduleName: String=""
        var startDate=dateToString(LocalDate.now())
        var startTime=timeToString(LocalDateTime.now())
        var endDate=startDate
        var endTime=startTime
        var place: String?=""
        var memo: String?=""
        var alarmDate: String?=""
        var alarmTime: String?=""
        
        if(from=="read") {
            val db=dbHelper.readableDatabase
            scheduleId = intent.getStringExtra("schedule_id").toString()
            val cursor=db.rawQuery("""SELECT * FROM schedule WHERE schedule_id="${scheduleId}"""",null)
            while(cursor.moveToNext()) {
                scheduleName=cursor.getString(1)
                startDate=cursor.getString(2)
                startTime=cursor.getString(3)
                endDate=cursor.getString(4)
                endTime=cursor.getString(5)
                place=cursor.getString(6)
                memo=cursor.getString(7)
                alarmDate=cursor.getString(8)
                alarmTime=cursor.getString(9)
                if(place==null) place=""
                if(memo==null) memo=""
                if(alarmDate==null) alarmDate=""
                if(alarmTime==null) alarmTime=""
            }
        }
    
        if(scheduleName.isNotEmpty()) binding.editTextName.setText(scheduleName)
        binding.buttonStartDate.text=convertDateString(startDate)
        binding.buttonStartTime.text=convertTimeString(startTime)
        binding.buttonEndDate.text=convertDateString(endDate)
        binding.buttonEndTime.text=convertTimeString(endTime)
        if(!place.isNullOrEmpty()) binding.editTextPlace.setText(place)
        if(!memo.isNullOrEmpty()) binding.editTextMemo.setText(memo)
        if(!alarmDate.isNullOrEmpty()) {
            binding.switchAlarm.setChecked(true)
            binding.switchAlarm.setText("설정")
            binding.buttonAlarmDate.text=convertDateString(alarmDate)
            binding.buttonAlarmDate.setVisibility(VISIBLE)
            binding.buttonAlarmTime.setVisibility(VISIBLE)
        } else {
            binding.switchAlarm.setChecked(false)
            binding.switchAlarm.setText("설정 안 함")
            binding.buttonAlarmDate.setVisibility(INVISIBLE)
            binding.buttonAlarmTime.setVisibility(INVISIBLE)
        }
        if(!alarmTime.isNullOrEmpty()) binding.buttonAlarmTime.text=convertTimeString(alarmTime)
    
        val startDateResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                startDate = result.data?.getStringExtra("newDate").toString()
                binding.buttonStartDate.text=convertDateString(startDate)
                if(startDate.compareTo(endDate)>0) { // 시작 일자가 종료 일자보다 늦을 경우 종료 일자를 시작 일자에 맞춤
                    endDate=startDate
                    binding.buttonEndDate.text=convertDateString(endDate)
                }
                if(startDate.equals(endDate) && startTime.compareTo(endTime)>0) { // 시작 일자와 종료 일자가 같고 시작 시간이 종료 시간보다 늦을 경우 종료 시간을 시작 시간에 맞춤
                    endTime=startTime
                    binding.buttonEndTime.text=convertTimeString(endTime)
                }
            }
        }
    
        val startTimeResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                startTime = result.data?.getStringExtra("newTime").toString()
                binding.buttonStartTime.text=convertTimeString(startTime)
                if(startDate.equals(endDate) && startTime.compareTo(endTime)>0) { // 시작 일자와 종료 일자가 같고 시작 시간이 종료 시간보다 늦을 경우 종료 시간을 시작 시간에 맞춤
                    endTime=startTime
                    binding.buttonEndTime.text=convertTimeString(endTime)
                }
            }
        }
    
        val endDateResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val tmpEndDate = result.data?.getStringExtra("newDate").toString()
                if(startDate.compareTo(tmpEndDate)>0) { // 시작 일자가 종료 일자보다 늦을 경우 종료 일자를 업데이트하지 않음
                    Toast.makeText(this@ScheduleCreateActivity, "시작 일자가 종료 일자보다 일러야 합니다.", Toast.LENGTH_SHORT).show()
                }
                else if(startDate.equals(tmpEndDate) && startTime.compareTo(endTime)>0) { // 시작 일자와 종료 일자가 같고 시작 시간이 종료 시간보다 늦을 경우 종료 시간을 업데이트하지 않음
                    Toast.makeText(this@ScheduleCreateActivity, "시작 시간이 종료 시간보다 빨라야 합니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    endDate=tmpEndDate
                    binding.buttonEndDate.text=convertDateString(endDate)
                }
            }
        }
    
        val endTimeResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val tmpEndTime = result.data?.getStringExtra("newTime").toString()
                if(startDate.equals(endDate) && startTime.compareTo(tmpEndTime)>0) { // 시작 일자와 종료 일자가 같고 시작 시간이 종료 시간보다 늦을 경우 종료 시간을 업데이트하지 않음
                    Toast.makeText(this@ScheduleCreateActivity, "시작 시간이 종료 시간보다 빨라야 합니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    endTime=tmpEndTime
                    binding.buttonEndTime.text=convertTimeString(endTime)
                }
            }
        }
    
        val alarmDateResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val tmpAlarmDate = result.data?.getStringExtra("newDate").toString()
                if(startDate.compareTo(tmpAlarmDate)<0) { // 시작 일자가 알람 일자보다 이를 경우 알람 일자를 업데이트하지 않음
                    Toast.makeText(this@ScheduleCreateActivity, "알람 일자가 시작 일자보다 일러야 합니다.", Toast.LENGTH_SHORT).show()
                }
                else if(startDate.equals(tmpAlarmDate) && !alarmTime.equals("") && startTime.compareTo(alarmTime!!)<0) { // 시작 일자와 알람 일자가 같고 시작 시간이 알람 시간보다 이를 경우 알람 시간을 업데이트하지 않음
                    Toast.makeText(this@ScheduleCreateActivity, "알람 시간이 시작 시간보다 빨라야 합니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    alarmDate=tmpAlarmDate
                    binding.buttonAlarmDate.text=convertDateString(alarmDate!!)
                }
            }
        }
    
        val alarmTimeResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val tmpAlarmTime = result.data?.getStringExtra("newTime").toString()
                if(startDate.equals(alarmDate) && !tmpAlarmTime.equals("") && startTime.compareTo(tmpAlarmTime)<0) { // 시작 일자와 종료 일자가 같고 시작 시간이 종료 시간보다 늦을 경우 종료 시간을 업데이트하지 않음
                    Toast.makeText(this@ScheduleCreateActivity, "알람 시간이 시작 시간보다 빨라야 합니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    alarmTime=tmpAlarmTime
                    binding.buttonAlarmTime.text=convertTimeString(alarmTime!!)
                }
            }
        }
        
        with(binding) {
            editTextName.addTextChangedListener {
                scheduleName= binding.editTextName.text.toString()
            }
            
            buttonStartDate.setOnClickListener {
                val intent = Intent(this@ScheduleCreateActivity, SelectDateActivity::class.java)
                intent.putExtra("oldDate", startDate)
                startDateResult.launch(intent)
            }
    
            buttonStartTime.setOnClickListener {
                val intent = Intent(this@ScheduleCreateActivity, SelectTimeActivity::class.java)
                intent.putExtra("oldTime", startTime)
                startTimeResult.launch(intent)
            }
    
            buttonEndDate.setOnClickListener {
                val intent = Intent(this@ScheduleCreateActivity, SelectDateActivity::class.java)
                intent.putExtra("oldDate", endDate)
                endDateResult.launch(intent)
            }
    
            buttonEndTime.setOnClickListener {
                val intent = Intent(this@ScheduleCreateActivity, SelectTimeActivity::class.java)
                intent.putExtra("oldTime", endTime)
                endTimeResult.launch(intent)
            }
    
            editTextPlace.addTextChangedListener {
                place= binding.editTextPlace.text.toString()
            }
    
            editTextMemo.addTextChangedListener {
                memo= binding.editTextMemo.text.toString()
            }
            
            switchAlarm.setOnCheckedChangeListener { p0, isChecked ->
                if(isChecked) {
                    switchAlarm.setText("설정")
                    binding.buttonAlarmDate.setVisibility(VISIBLE)
                    binding.buttonAlarmTime.setVisibility(VISIBLE)
                } else {
                    switchAlarm.setText("설정 안 함")
                    binding.buttonAlarmDate.setVisibility(INVISIBLE)
                    binding.buttonAlarmTime.setVisibility(INVISIBLE)
                }
            }
            
            buttonAlarmDate.setOnClickListener {
                val intent=Intent(this@ScheduleCreateActivity,SelectDateActivity::class.java)
                if(alarmDate.equals("")) intent.putExtra("oldDate",startDate)
                else intent.putExtra("oldDate",alarmDate)
                alarmDateResult.launch(intent)
            }
            
            buttonAlarmTime.setOnClickListener {
                val intent=Intent(this@ScheduleCreateActivity,SelectTimeActivity::class.java)
                if(alarmTime.equals("")) intent.putExtra("oldTime","00:00")
                else intent.putExtra("oldTime",alarmTime)
                alarmTimeResult.launch(intent)
            }
    
            // 취소 버튼
            buttonCancel.setOnClickListener {
                finish()
            }
    
            // 저장 버튼
            buttonSave.setOnClickListener {
                if(scheduleName.equals("")) {
                    Toast.makeText(this@ScheduleCreateActivity, "일정 이름을 입력해 주십시오.", Toast.LENGTH_SHORT).show()
                } else if(switchAlarm.isChecked() && alarmDate.equals("")) {
                    Toast.makeText(this@ScheduleCreateActivity, "알람 일자를 입력해 주십시오.", Toast.LENGTH_SHORT).show()
                } else if(switchAlarm.isChecked() && alarmTime.equals("")) {
                    Toast.makeText(this@ScheduleCreateActivity, "알람 시간을 입력해 주십시오.", Toast.LENGTH_SHORT).show()
                } else {
                    if(from=="home") scheduleId = SimpleDateFormat("yyyyMMdd_hhmmss", Locale.getDefault()).format(Calendar.getInstance().timeInMillis)
                    if(place.equals("")) place=null
                    if(memo.equals("")) memo=null
                    if(!switchAlarm.isChecked() || alarmDate.equals("") || alarmTime.equals("")) {
                        alarmDate=null
                        alarmTime=null
                    }
                    
                    if(from=="home") { // 생성
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
                        scheduleRepository.insertSchedule(schedule)
                    } else if(from=="read") { // 수정
                        var sql="""
                            UPDATE schedule
                            SET
                            schedule_name="$scheduleName",
                            start_date="$startDate",
                            start_time="$startTime",
                            end_date="$endDate",
                            end_time="$endTime",
                        """
                        if(place==null) sql+="place=null," else sql+="""place="$place","""
                        if(memo==null) sql+="memo=null," else sql+="""memo="$memo","""
                        if(alarmDate==null) sql+="alarm_date=null," else sql+="""alarm_date="$alarmDate","""
                        if(alarmTime==null) sql+="alarm_time=null " else sql+="""alarm_time="$alarmTime """"
                        sql+="""WHERE schedule_id="$scheduleId""""
                        sql=sql.trimIndent()
                        val db=dbHelper.getDatabase()
                        db.execSQL(sql)
                    }

                    if (switchAlarm.isChecked && alarmDate != null && alarmTime != null) {
                        val alarmCalendar = Calendar.getInstance().apply {
                            time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                .parse("$alarmDate $alarmTime")!!
                        }

                        val alarmIntent = Intent(this@ScheduleCreateActivity, AlarmReceiver::class.java)
                        alarmIntent.putExtra("title", scheduleName)
                        alarmIntent.putExtra("date", alarmDate)
                        alarmIntent.putExtra("time", alarmTime)
                        alarmIntent.putExtra("schedule_id", scheduleId)

                        val pendingIntent = PendingIntent.getBroadcast(
                            this@ScheduleCreateActivity,
                            0,
                            alarmIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            alarmCalendar.timeInMillis,
                            pendingIntent
                        )

                    }

                    Toast.makeText(this@ScheduleCreateActivity, "일정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}