package com.example.a0509

import android.app.Activity
import android.graphics.Color
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import com.prolificinteractive.materialcalendarview.spans.DotSpan.DEFAULT_RADIUS

class DateDecorator(context: Activity?, dates: ArrayList<CalendarDay>) : DayViewDecorator {
	val datelist=dates
	
	override fun shouldDecorate(day: CalendarDay): Boolean {
		return day in datelist
	}
	
	override fun decorate(view: DayViewFacade?) {
		view?.addSpan(DotSpan(8.0f,Color.parseColor("#FF0000")))
	}
}