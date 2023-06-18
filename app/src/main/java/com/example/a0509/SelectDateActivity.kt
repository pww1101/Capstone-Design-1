package com.example.a0509

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a0509.databinding.SelectDateBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.prolificinteractive.materialcalendarview.format.TitleFormatter

class SelectDateActivity : AppCompatActivity() {
	
	private lateinit var binding: SelectDateBinding
	
	private fun stringToDate(str: String): CalendarDay {
		val year=str.substring(0..3).toInt()
		val month=str.substring(5..6).toInt()
		val day=str.substring(8..9).toInt()
		return CalendarDay.from(year,month,day)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		binding = SelectDateBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		binding.calendarView.setTitleFormatter(object : TitleFormatter {
			override fun format(day: CalendarDay?): CharSequence {
				return "${day!!.year}년  ${day.month}월"
			}
		})
		binding.calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)))
		
		val selectedDate=intent.getStringExtra("oldDate").toString()
		binding.calendarView.selectedDate=stringToDate(selectedDate)
		
		with(binding){
			calendarView.setOnDateChangedListener { widget, date, selected ->
				calendarView.selectedDate=date
			}
			
			buttonCancel.setOnClickListener {
				finish()
			}
			
			buttonSave.setOnClickListener {
				val intent = Intent(this@SelectDateActivity, ScheduleCreateActivity::class.java).apply {
					putExtra("newDate",selectedDate)
				}
				setResult(RESULT_OK,intent)
				finish()
			}
		}
	}
}