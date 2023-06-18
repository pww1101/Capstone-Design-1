package com.example.a0509.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a0509.DateDecorator
import com.example.a0509.R
import com.example.a0509.ScheduleCreateActivity
import com.example.a0509.ScheduleRepository
import com.example.a0509.databinding.FragmentHomeBinding
import com.example.a0509.ScheduleDatabaseHelper
import com.example.a0509.Schedule
import com.example.a0509.ScheduleReadActivity
import com.example.myapplication.ScheduleAdapter
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.prolificinteractive.materialcalendarview.format.TitleFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var calendarView: com.prolificinteractive.materialcalendarview.MaterialCalendarView
    private lateinit var schedulesRecyclerView: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var allSchedules: List<Schedule>
    private lateinit var scheduleRepository: ScheduleRepository

    override fun onCreateView (
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }
    
    private fun stringToDate(str: String): CalendarDay {
        val year=str.substring(0..3).toInt()
        val month=str.substring(5..6).toInt()
        val day=str.substring(8..9).toInt()
        return CalendarDay.from(year,month,day)
    }
    
    private fun dateToString(date: CalendarDay): String {
        val year=date.year
        val month=date.month
        val day=date.day
        return "%04d-%02d-%02d".format(year,month,day)
    }

    override fun onResume() {
        super.onResume()

        val currentDate = calendarView.selectedDate
        updateRecyclerViewForSelectedDate(currentDate!!)
        decorateDates()
        calendarView.selectedDate=currentDate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleRepository = ScheduleRepository(ScheduleDatabaseHelper(requireContext()))

        allSchedules = scheduleRepository.getAllSchedules()

        calendarView = binding.calendarView
        schedulesRecyclerView = binding.schedulesRecyclerView
        schedulesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        scheduleAdapter = ScheduleAdapter(emptyList())
        schedulesRecyclerView.adapter = scheduleAdapter
    
        calendarView.setTitleFormatter(object : TitleFormatter {
            override fun format(day: CalendarDay?): CharSequence {
                return "${day!!.year}년  ${day.month}월"
            }
        })
        calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)))
    
        decorateDates()
        calendarView.selectedDate=CalendarDay.today()
        val currentDate=calendarView.selectedDate
        updateRecyclerViewForSelectedDate(currentDate!!)

        calendarView.setOnDateChangedListener { widget, date, selected ->
            calendarView.selectedDate=date
            updateRecyclerViewForSelectedDate(date)
        }
        
        var selectedDate=calendarView.selectedDate

        binding.fab.setOnClickListener {
            val intent = Intent(requireActivity(), ScheduleCreateActivity::class.java)
            intent.putExtra("from","home")
            startActivity(intent)
            selectedDate=calendarView.selectedDate
            updateRecyclerViewForSelectedDate(selectedDate!!)
        }
        
        scheduleAdapter.setOnItemClickListener(object: ScheduleAdapter.OnItemClickListener {
            override fun onItemClick(v: View, pos: Int) {
                val intent=Intent(requireActivity(),ScheduleReadActivity::class.java)
                intent.putExtra("schedule_id",allSchedules[pos].scheduleId)
                startActivity(intent)
                selectedDate=calendarView.selectedDate
                updateRecyclerViewForSelectedDate(selectedDate!!)
            }
        })
    }

    private fun updateRecyclerViewForSelectedDate(selectedDate: CalendarDay) {
        allSchedules = scheduleRepository.getAllSchedules()
        val selectedDateString=dateToString(calendarView.selectedDate!!)
        val filteredSchedules = allSchedules.filter {
            it.startDate <= selectedDateString
        }.filter{
            it.endDate >= selectedDateString
        }
        scheduleAdapter = ScheduleAdapter(filteredSchedules)
        scheduleAdapter.setOnItemClickListener(object: ScheduleAdapter.OnItemClickListener {
            override fun onItemClick(v: View, pos: Int) {
                val intent=Intent(requireActivity(),ScheduleReadActivity::class.java)
                intent.putExtra("schedule_id",filteredSchedules[pos].scheduleId)
                startActivity(intent)
            }
        })
        schedulesRecyclerView.adapter = scheduleAdapter
    }
    
    private fun decorateDates() {
        calendarView.removeDecorators()
        val datesList=ArrayList<CalendarDay>()
        for(schedule in allSchedules) {
            val startDate=stringToDate(schedule.startDate)
            val endDate=stringToDate(schedule.endDate)
            calendarView.selectRange(startDate,endDate)
            datesList.addAll(calendarView.selectedDates)
        }
        val dateDecorator=DateDecorator(requireActivity(),datesList)
        calendarView.addDecorator(dateDecorator)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}