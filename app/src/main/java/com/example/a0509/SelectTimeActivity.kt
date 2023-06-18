package com.example.a0509

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a0509.databinding.SelectTimeBinding
import java.time.LocalDate
import java.time.LocalDateTime

class SelectTimeActivity : AppCompatActivity() {
	
	private lateinit var binding: SelectTimeBinding
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		binding = SelectTimeBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		var selectedTime=intent.getStringExtra("oldTime").toString()
		
		binding.numberPickerHour.minValue=0
		binding.numberPickerHour.maxValue=23
		binding.numberPickerMinute.minValue=0
		binding.numberPickerMinute.maxValue=59
		binding.numberPickerHour.value=selectedTime.substring(0..1).toInt()
		binding.numberPickerMinute.value=selectedTime.substring(3..4).toInt()
		
		with(binding){
			numberPickerHour.setOnValueChangedListener { numberPicker, i1, i2 ->
				selectedTime="${"%02d".format(binding.numberPickerHour.value)}:${"%02d".format(binding.numberPickerMinute.value)}"
			}
			
			numberPickerMinute.setOnValueChangedListener { numberPicker, i1, i2 ->
				selectedTime="${"%02d".format(binding.numberPickerHour.value)}:${"%02d".format(binding.numberPickerMinute.value)}"
			}
			
			buttonCancel.setOnClickListener {
				finish()
			}
			
			buttonSave.setOnClickListener {
				val intent=Intent(this@SelectTimeActivity, ScheduleCreateActivity::class.java).apply {
					putExtra("newTime",selectedTime)
				}
				setResult(RESULT_OK,intent)
				finish()
			}
		}
	}
}