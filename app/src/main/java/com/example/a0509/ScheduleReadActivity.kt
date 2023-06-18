package com.example.a0509

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.a0509.databinding.ScheduleReadBinding
import androidx.appcompat.app.AlertDialog

class ScheduleReadActivity : AppCompatActivity() {
	
	private lateinit var binding: ScheduleReadBinding
	
	private fun convertDateString(date: String): String {
		return "${date.substring(0..3)}년 ${date.substring(5..6)}월 ${date.substring(8..9)}일"
	}
	
	private fun convertTimeString(time: String): String {
		return "${time.substring(0..1)}시 ${time.substring(3..4)}분"
	}
	
	private fun setTextView(schedule_id: String) {
		val scheduleDatabaseHelper=ScheduleDatabaseHelper(this)
		val scheduleDatabase=scheduleDatabaseHelper.readableDatabase
		val cursor=scheduleDatabase.rawQuery("""SELECT * FROM schedule WHERE schedule_id="${schedule_id}"""",null)
		with(binding) {
			while(cursor.moveToNext()) {
				viewScheduleName.text = cursor.getString(1)
				viewScheduleDateTime.text = "${convertDateString(cursor.getString(2))} ${convertTimeString(cursor.getString(3))}~${convertDateString(cursor.getString(4))} ${convertTimeString(cursor.getString(5))}"
				viewPlace.text=cursor.getString(6)
				if(viewPlace.text=="") viewPlace.text="장소가 없습니다."
				viewMemo.text=cursor.getString(7)
				if(viewMemo.text=="") viewMemo.text="메모가 없습니다."
				if (!cursor.isNull(8)) {
					viewAlarm.text="${convertDateString(cursor.getString(8))} ${convertTimeString(cursor.getString(9))}"
				} else {
					viewAlarm.text="알림이 설정되지 않았습니다."
				}
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		binding = ScheduleReadBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		
		val schedule_id = intent.getStringExtra("schedule_id").toString()
		setTextView(schedule_id)
		
		binding.editBtn.setOnClickListener {
			val intent= Intent(this@ScheduleReadActivity,ScheduleCreateActivity::class.java)
			intent.putExtra("from","read")
			intent.putExtra("schedule_id",schedule_id)
			startActivity(intent)
			finish()
		}

		binding.deleteBtn.setOnClickListener {
			val builder = AlertDialog.Builder(this@ScheduleReadActivity)
			val scheduleDatabaseHelper=ScheduleDatabaseHelper(this)
			val scheduleDatabase=scheduleDatabaseHelper.readableDatabase

			with(builder) {
				setTitle("삭제하시겠습니까?")
				setPositiveButton("확인") { dialog, which ->
					// Delete the schedule from the database
					scheduleDatabase.delete("schedule", "schedule_id=?", arrayOf(schedule_id))
					// Navigate back to the previous screen
					finish()
				}
				setNegativeButton("취소") { dialog, which ->
					// Just dismiss the dialog
					dialog.dismiss()
				}
				show()
			}
		}
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when(item.itemId) {
			android.R.id.home -> {
				finish()
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}
}