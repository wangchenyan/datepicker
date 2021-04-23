package me.wcy.datepicker.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import me.wcy.datepicker.DatePickerDialog
import me.wcy.datepicker.DateType
import me.wcy.datepicker.sample.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnYmdhm.setOnClickListener {
            DatePickerDialog.Builder(this)
                    .setDateType(DateType.YMDHM)
                    .setTitle("自定义标题")
                    .setHighlightColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setCanceledTouchOutside(true)
                    .setOnDateResult { date ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        binding.tvSelect.text = "当前选中: ${dateFormat.format(Date(date))}"
                    }
                    .build()
                    .show()
        }

        binding.btnYmd.setOnClickListener {
            DatePickerDialog.Builder(this)
                    .setDateType(DateType.YMD)
                    .setYearRange(1900, 2018)
                    .setTitle("选择日期")
                    .setHighlightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .setCanceledTouchOutside(false)
                    .setOnDateResult { date ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        binding.tvSelect.text = "当前选中: ${dateFormat.format(Date(date))}"
                    }
                    .build()
                    .show()
        }

        binding.btnHm.setOnClickListener {
            DatePickerDialog.Builder(this)
                    .setDateType(DateType.HM)
                    .setTitle("选择时间")
                    .setHighlightColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .setCanceledTouchOutside(true)
                    .setOnDateResult { date ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        binding.tvSelect.text = "当前选中: ${dateFormat.format(Date(date))}"
                    }
                    .build()
                    .show()
        }

        binding.btnDatePicker.setOnClickListener {
            val date = binding.datePickerView.getSelectedDate()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            binding.tvSelect.text = "当前选中: ${dateFormat.format(Date(date))}"
        }

        val list = listOf("张三", "李四", "王五", "六麻子", "小明")
        binding.pickerView.setItems(list)
        binding.btnItemPicker.setOnClickListener {
            val select = binding.pickerView.getSelectedPosition()
            Toast.makeText(this, "当前选中: ${list[select]}", Toast.LENGTH_LONG).show()
        }
    }
}