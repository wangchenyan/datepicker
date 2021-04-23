package me.wcy.datepicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.ColorInt
import me.wcy.datepicker.databinding.WcyDialogDatePickerBinding

/**
 * Powered by jzman.
 * Created on 2019/1/3 0003.
 */
class DatePickerDialog private constructor(context: Context) : Dialog(context, R.style.DatePicker_Dialog) {
    private val binding = WcyDialogDatePickerBinding.inflate(LayoutInflater.from(context))
    private var mDateType: DateType? = null
    private var mYearRange: Pair<Int, Int>? = null
    private var mTitle: String? = null
    private var mHighlightColor: Int? = null
    private var isCanceledTouchOutside: Boolean? = null
    private var mOnDateResult: ((date: Long) -> Unit)? = null

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowParams()
        init()
    }

    private fun setWindowParams() {
        val params = window!!.attributes
        params.gravity = Gravity.BOTTOM
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        window!!.attributes = params
    }

    private fun init() {
        binding.datePickerView.setDateType(mDateType)
        binding.datePickerView.setYearRange(mYearRange)
        binding.tvDialogTitle.text = mTitle ?: context.getString(R.string.strDateSelect)
        val highlightColor = mHighlightColor ?: Color.BLACK
        binding.datePickerView.setHighlightColor(highlightColor)
        binding.tvDialogTopConfirm.setTextColor(highlightColor)
        setCanceledOnTouchOutside(isCanceledTouchOutside ?: true)

        binding.tvDialogTopCancel.setOnClickListener {
            dismiss()
        }
        binding.tvDialogTopConfirm.setOnClickListener {
            dismiss()
            if (mOnDateResult != null) {
                mOnDateResult?.invoke(binding.datePickerView.getSelectDate())
            }
        }
    }

    class Builder(private val context: Context) {
        private var dateType: DateType? = null
        private var yearRange: Pair<Int, Int>? = null
        private var title: String? = null
        private var highlightColor: Int? = null
        private var isCanceledTouchOutside: Boolean? = null
        private var onDateResult: ((date: Long) -> Unit)? = null

        fun setDateType(dateType: DateType): Builder {
            this.dateType = dateType
            return this
        }

        fun setYearRange(start: Int, end: Int): Builder {
            if (start > end) {
                throw IllegalArgumentException("开始年份不能大于结束年份")
            }
            this.yearRange = Pair(start, end)
            return this
        }

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setHighlightColor(@ColorInt color: Int): Builder {
            this.highlightColor = color
            return this
        }

        fun setCanceledTouchOutside(canceledTouchOutside: Boolean): Builder {
            this.isCanceledTouchOutside = canceledTouchOutside
            return this
        }

        fun setOnDateResultListener(onDateResult: ((date: Long) -> Unit)): Builder {
            this.onDateResult = onDateResult
            return this
        }

        fun build(): DatePickerDialog {
            val dialog = DatePickerDialog(context)
            dialog.mDateType = dateType
            dialog.mYearRange = yearRange
            dialog.mTitle = title
            dialog.mHighlightColor = highlightColor
            dialog.isCanceledTouchOutside = isCanceledTouchOutside
            dialog.mOnDateResult = onDateResult
            return dialog
        }
    }
}