package hkucs.example.comp3330project

import android.content.Intent
import android.os.Bundle
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
class RecordCalendar : AppCompatActivity() {
    private var calendar: CalendarView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_calendar)
        getSupportActionBar()!!.setTitle("Calendar")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        calendar = findViewById(R.id.Calendar) as CalendarView

        calendar!!.setOnDateChangeListener(object : CalendarView.OnDateChangeListener{
            override fun onSelectedDayChange(
                view: CalendarView,
                year: Int,
                month: Int,
                dayOfMonth: Int
            )
            {
                val date: String = (dayOfMonth.toString() + "_" + (month + 1).toString() + "_" + year.toString())
                val intent = Intent(this@RecordCalendar, MainActivity::class.java)
                intent.putExtra("to","SummaryFragment")
                intent.putExtra("date",date.toString())
                startActivity(intent)
            }

        })
    }
}