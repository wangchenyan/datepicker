package me.wcy.datepicker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import me.wcy.datepicker.databinding.WcyDatePickerViewBinding
import java.util.*
import kotlin.math.min

class DatePickerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var binding = WcyDatePickerViewBinding.inflate(LayoutInflater.from(context), this)

    private var mCurrentYear = 0
    private var mCurrentMonth = 0
    private var mCurrentDay = 0
    private var mCurrentHour = 0
    private var mCurrentMinute = 0

    private var mDateType: DateType? = null
    private var mYearRange: Pair<Int, Int>? = null
    private var mHighlightColor: Int? = null

    companion object {
        private const val SPACE = 5
    }

    init {
        orientation = HORIZONTAL
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView)
        mHighlightColor = ta.getColor(R.styleable.DatePickerView_dpvHighlightColor, Color.BLACK)
        val dateTypeId = ta.getInt(R.styleable.DatePickerView_dpvDateType, DateType.YMDHM.id)
        mDateType = DateType.fromId(dateTypeId)
        ta.recycle()
        init()
    }

    private fun init() {
        val calendar = Calendar.getInstance()

        // Year
        setYearRange(mYearRange)

        // Month+
        mCurrentMonth = calendar[Calendar.MONTH] + 1
        binding.pvMonth.setItems(getDateItems(12, 1))
        binding.pvMonth.setSelectedItem(getDateForShow(mCurrentMonth, 2))

        // Day
        mCurrentDay = calendar[Calendar.DAY_OF_MONTH]
        updateDay(mCurrentYear, mCurrentMonth)

        // Hour
        mCurrentHour = calendar[Calendar.HOUR_OF_DAY]
        binding.pvHour.setItems(getDateItems(24, 0))
        binding.pvHour.setSelectedItem(getDateForShow(mCurrentHour, 2))

        // Minute
        mCurrentMinute = calendar[Calendar.MINUTE]
        binding.pvMinute.setItems(getDateItems(60, 0))
        binding.pvMinute.setSelectedItem(getDateForShow(mCurrentMinute, 2))

        setDateType(mDateType)
        setHighlightColor(mHighlightColor)

        binding.pvYear.setOnSelect { v, item ->
            mCurrentYear = item.toInt()
            updateDay(mCurrentYear, mCurrentMonth)
        }
        binding.pvMonth.setOnSelect { v, item ->
            mCurrentMonth = item.toInt()
            updateDay(mCurrentYear, mCurrentMonth)
        }
        binding.pvDay.setOnSelect { v, item ->
            mCurrentDay = item.toInt()
        }
        binding.pvHour.setOnSelect { v, item ->
            mCurrentHour = item.toInt()
        }
        binding.pvMinute.setOnSelect { v, item ->
            mCurrentMinute = item.toInt()
        }
    }

    fun setDateType(dateType: DateType?): DatePickerView {
        mDateType = dateType
        when (mDateType) {
            DateType.HM -> {
                binding.pvYear.visibility = View.GONE
                binding.pvMonth.visibility = View.GONE
                binding.pvDay.visibility = View.GONE
            }
            DateType.YMD -> {
                binding.pvHour.visibility = View.GONE
                binding.pvMinute.visibility = View.GONE
            }
            else -> {
                (binding.pvYear.layoutParams as LayoutParams).weight = 1.67f
            }
        }
        return this
    }

    fun setYearRange(start: Int, end: Int): DatePickerView {
        setYearRange(Pair(start, end))
        return this
    }

    fun setYearRange(yearRange: Pair<Int, Int>?): DatePickerView {
        if (yearRange != null && (yearRange.first > yearRange.second)) {
            throw IllegalArgumentException("开始年份不能大于结束年份")
        }

        mYearRange = yearRange
        mCurrentYear = Calendar.getInstance()[Calendar.YEAR]
        val startYear = mYearRange?.first ?: mCurrentYear - SPACE
        val endYear = mYearRange?.second ?: mCurrentYear + SPACE
        val yearItems = mutableListOf<String>()
        for (i in startYear..endYear) {
            yearItems.add(i.toString())
        }
        binding.pvYear.setItems(yearItems)
        if (mCurrentYear !in startYear..endYear) {
            mCurrentYear = endYear
        }
        binding.pvYear.setSelectedItem(getDateForShow(mCurrentYear, 4))
        return this
    }

    fun setHighlightColor(@ColorInt color: Int?): DatePickerView {
        mHighlightColor = color
        val highlightColor = mHighlightColor ?: Color.BLACK
        binding.pvYear.setHighlightColor(highlightColor)
        binding.pvMonth.setHighlightColor(highlightColor)
        binding.pvDay.setHighlightColor(highlightColor)
        binding.pvHour.setHighlightColor(highlightColor)
        binding.pvMinute.setHighlightColor(highlightColor)
        return this
    }

    fun getSelectedDate(): Long {
        return when (mDateType) {
            DateType.HM -> getDateMills(hour = mCurrentHour, minute = mCurrentMinute)
            DateType.YMD -> getDateMills(year = mCurrentYear, month = mCurrentMonth, day = mCurrentDay)
            else -> getDateMills(mCurrentYear, mCurrentMonth, mCurrentDay, mCurrentHour, mCurrentMinute)
        }
    }

    private fun updateDay(year: Int, month: Int) {
        val daySize = getDaySize(year, month)
        val dayItems = getDateItems(daySize, 1)
        binding.pvDay.setItems(dayItems)
        // fix day
        mCurrentDay = min(mCurrentDay, daySize)
        binding.pvDay.setSelectedItem(getDateForShow(mCurrentDay, 2))
    }

    private fun getDaySize(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month - 1
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getDateItems(size: Int, add: Int): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until size) {
            val real = i + add
            list.add(getDateForShow(real, 2))
        }
        return list
    }

    private fun getDateForShow(num: Int, length: Int): String {
        var res = num.toString()
        while (res.length < length) {
            res = "0$res"
        }
        return res
    }

    private fun getDateMills(
            year: Int? = null,
            month: Int? = null,
            day: Int? = null,
            hour: Int? = null,
            minute: Int? = null
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        if (year != null) {
            calendar.set(Calendar.YEAR, year)
        }
        if (month != null) {
            calendar.set(Calendar.MONTH, month - 1)
        }
        if (day != null) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
        }
        if (hour != null) {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
        }
        if (minute != null) {
            calendar.set(Calendar.MINUTE, minute)
        }
        return calendar.timeInMillis
    }
}