# datepicker

[![](https://jitpack.io/v/wangchenyan/datepicker.svg)](https://jitpack.io/#wangchenyan/datepicker)

> 站在巨人的肩膀上 [MDatePickerSample](https://github.com/jzmanu/MDatePickerSample)

时间选择器，滚动列表选择器

![](https://raw.githubusercontent.com/wangchenyan/datepicker/master/art/screenshot1.png)
![](https://raw.githubusercontent.com/wangchenyan/datepicker/master/art/screenshot2.png)

## Dependency

1. Add gradle dependency

```
// root project build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add library dependency

```
// module build.gradle

...

dependencies {
    ...
    implementation 'com.github.wangchenyan:datepicker:Tag'
}
```

## Usage

1. DatePickerDialog

```kotlin
DatePickerDialog.Builder(this)
        .setDateType(DateType.YMDHM)
        .setTitle("自定义标题")
        .setHighlightColor(ContextCompat.getColor(this, R.color.colorPrimary))
        .setCanceledTouchOutside(true)
        .setOnDateResultListener { date ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            binding.tvSelect.text = "当前选中: ${dateFormat.format(Date(date))}"
        }
        .build()
        .show()
```

2. DatePickerView

```xml
<me.wcy.datepicker.DatePickerView
    android:id="@+id/datePickerView"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:dpvHighlightColor="#FF0000" />
```

```kotlin
datePickerView.setDateType(dateType)
datePickerView.setYearRange(yearRange)
datePickerView.setHighlightColor(highlightColor)

val date = datePickerView.getSelectDate()
```

3. PickerView

```xml
<me.wcy.datepicker.PickerView
    android:id="@+id/pickerView"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:pvHighlightColor="#FF0000" />
```

```kotlin
pickerView.setItems(items)
pickerView.setSelectPosition(position)
pickerView.setText(text)
pickerView.setHighlightColor(color)

val selectPosition = pickerView.getSelectPosition()
```

**更多用法请参考 [sample](https://github.com/wangchenyan/crouter/tree/datepicker/sample) 代码**

## ProGuard

无

## About me

掘金：https://juejin.im/user/2313028193754168

微博：https://weibo.com/wangchenyan1993
