package com.example.a0509.ui.bachelor_sch

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.a0509.R
import com.example.a0509.databinding.FragmentBachelorschBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class BachelorSch(val date: CalendarDay, val event: String)
val monthSchedules = mutableMapOf<Int, MutableList<BachelorSch>>()  // [1],[2] - 다음 년도

class BachelorSchFragment : Fragment() {
    private var _binding: FragmentBachelorschBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarView: com.prolificinteractive.materialcalendarview.MaterialCalendarView

    private val allSchedules = mutableListOf<BachelorSch>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBachelorschBinding.inflate(inflater, container, false)
        calendarView = binding.calendarView
        calendarView.setTitleFormatter(object : TitleFormatter {
            override fun format(day: CalendarDay?): CharSequence {
                return "${day!!.year}년  ${day.month}월"
            }
        })
        calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)))

        fetchSchedules()

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            val eventsOnDate = allSchedules.filter { it.date == date }
            if (eventsOnDate.isNotEmpty()) {
                val eventsText = eventsOnDate.joinToString("\n") { it.event }
                AlertDialog.Builder(requireContext())
                    .setTitle("[ ${date.month}월 ${date.day}일의 학사 일정 ]")
                    .setMessage(eventsText)
                    .setPositiveButton("확인", null)
                    .show()
            }
        }

        return binding.root
    }

    private fun fetchSchedules() {
        CoroutineScope(Dispatchers.IO).launch {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
            })

            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, java.security.SecureRandom())
            }

            val sslSocketFactory = sslContext.socketFactory

            val client = OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()

            val currentYear = CalendarDay.today().year
            val doc = Jsoup.connect("https://www.sogang.ac.kr/bachelor/sch_$currentYear.html")
                .sslSocketFactory(sslSocketFactory)
                .get()
            val tds = doc.select("td:not([class])") // class가 없는 <td>만 선택

            for(i in 1..12){
                monthSchedules[i] = mutableListOf()
            }

            for (i in tds.indices step 2) {
                val rawDate = tds[i].text() // "6. 30(금)" or "7. 26(수)~8. 9(수)" or "8. 10(목)~11(금)"
                val rawDates: List<String>
                var flag = false    // 일정이 기간인 경우 flag = true
                val event = tds[i+1].text()

                if(rawDate.contains("~")){
                    flag = true
                    rawDates = rawDate.split("~")   // "7. 26(수)", "8. 9(수)"
                }
                else{
                    rawDates = listOf(rawDate)
                }

                var dateParts = rawDates[0].split(".")  // [ 7, 26(수) ]
                val month1 = dateParts[0].toInt()
                var dateWithExtra = dateParts[1].trim()
                val date1 = Regex("\\d+").find(dateWithExtra)?.value?.toInt()

                if(flag) {
                    if (rawDates[1].contains(".")) { // 기간 중 마지막 날짜에 월이 포함된 경우
                        dateParts = rawDates[1].split(".")  // [ 8, 9(수) ]
                        val month2 = dateParts[0].toInt()
                        dateWithExtra = dateParts[1].trim()
                        val date2 = Regex("\\d+").find(dateWithExtra)?.value?.toInt()

                        if(month1 == month2){
                            if (month1 == 1 || month1 == 2) {
                                if (date1 != null && date2 != null)
                                    for (j in date1..date2)
                                        monthSchedules[month1]?.add(
                                            BachelorSch(
                                                CalendarDay.from(
                                                    2024,
                                                    month1,
                                                    j
                                                ), event
                                            )
                                        )
                            } else {
                                if (date1 != null && date2 != null)
                                    for (j in date1..date2) {
                                        monthSchedules[month1]?.add(
                                            BachelorSch(
                                                CalendarDay.from(
                                                    2023,
                                                    month1,
                                                    j
                                                ), event
                                            )
                                        )
                                    }
                            }
                        }
                        else {
                            if (month1 == 1 || month1 == 2) {
                                if (date1 != null)
                                    for (j in date1..getDaysInMonth(2024, month1))
                                        monthSchedules[month1]?.add(
                                            BachelorSch(
                                                CalendarDay.from(
                                                    2024,
                                                    month1,
                                                    j
                                                ), event
                                            )
                                        )
                            } else {
                                if (date1 != null)
                                    for (j in date1..getDaysInMonth(2023, month1))
                                        monthSchedules[month1]?.add(
                                            BachelorSch(
                                                CalendarDay.from(
                                                    2023,
                                                    month1,
                                                    j
                                                ), event
                                            )
                                        )
                            }

                            if (month2 == 1 || month2 == 2) {
                                if (date2 != null)
                                    for (j in 1..date2)
                                        monthSchedules[month2]?.add(
                                            BachelorSch(
                                                CalendarDay.from(
                                                    2024,
                                                    month2,
                                                    j
                                                ), event
                                            )
                                        )
                            } else {
                                if (date2 != null)
                                    for (j in 1..date2)
                                        monthSchedules[month1]?.add(
                                            BachelorSch(
                                                CalendarDay.from(
                                                    2023,
                                                    month2,
                                                    j
                                                ), event
                                            )
                                        )
                            }
                        }
                    } else {   // 기간 중 마지막 날짜에 월 포함 X
                        dateWithExtra = rawDates[1].trim()
                        val date2 = Regex("\\d+").find(dateWithExtra)?.value?.toInt()

                        if (month1 == 1 || month1 == 2) {
                            if (date1 != null && date2 != null)
                                for (j in date1..date2)
                                    monthSchedules[month1]?.add(
                                        BachelorSch(
                                            CalendarDay.from(
                                                2024,
                                                month1,
                                                j
                                            ), event
                                        )
                                    )
                        } else {
                            if (date1 != null && date2 != null)
                                for (j in date1..date2) {
                                    monthSchedules[month1]?.add(
                                        BachelorSch(
                                            CalendarDay.from(
                                                2023,
                                                month1,
                                                j
                                            ), event
                                        )
                                    )
                                }
                        }
                    }
                }

                else {   // 기간 X
                    if (month1 == 1 || month1 == 2) {
                        if (date1 != null)
                            monthSchedules[month1]?.add(
                                BachelorSch(
                                    CalendarDay.from(
                                        2024,
                                        month1,
                                        date1
                                    ), event
                                )
                            )
                    } else {
                        if (date1 != null)
                            monthSchedules[month1]?.add(
                                BachelorSch(
                                    CalendarDay.from(
                                        2023,
                                        month1,
                                        date1
                                    ), event
                                )
                            )
                    }
                }
            }

            withContext(Dispatchers.Main) {
                for(i in 1..12) monthSchedules[i]?.let {
                    setupCalendar(it)
                    allSchedules.addAll(it) // 스케줄 리스트에 해당 월의 모든 스케줄 추가
                }
            }
        }
    }

    private fun setupCalendar(schedules: MutableList<BachelorSch>) {
        val calendarDays = schedules.map { it.date }

        binding.calendarView.addDecorator(EventDecorator(Color.RED, calendarDays))
    }

    // 일정이 있는 날짜를 강조하는 DayViewDecorator
    class EventDecorator(private val color: Int, private val dates: List<CalendarDay>) :
        DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(5F, color))  // 날짜 아래에 색상이 있는 점을 추가
        }
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when(month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if(isLeapYear(year)) 29 else 28
            else -> throw IllegalArgumentException("Invalid month: $month")
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return when {
            year % 400 == 0 -> true
            year % 100 == 0 -> false
            year % 4 == 0 -> true
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}