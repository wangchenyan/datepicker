package me.wcy.datepicker

import android.content.res.Resources
import android.util.TypedValue

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}

fun Number.dp2px(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()
}

fun Number.px2dp(): Float {
    return (this.toFloat() / Resources.getSystem().displayMetrics.density + 0.5f)
}
