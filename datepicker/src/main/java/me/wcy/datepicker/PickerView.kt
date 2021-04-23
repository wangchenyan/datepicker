package me.wcy.datepicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs

/**
 * Powered by jzman.
 * Created on 2018/12/25 0025.
 */
class PickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private val mPaintNormal = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaintSelect = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaintLine = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTextSizeSelect = 22.dp2px().toFloat()
    private val mTextSizeNormal = 14.dp2px().toFloat()
    private val mTextAlphaSelect = 255f
    private val mTextAlphaNormal = 120f
    private var mText: String? = null
    private val mTimer = Timer()
    private var mTask: MTimerTask? = null
    private val mHandler = MHandler(this)
    private val mItems = mutableListOf<String>()
    private val mRawItems = mutableListOf<String>()
    private var mOnSelect: ((v: View, data: String) -> Unit)? = null
    private var mHighlightColor = Color.BLACK

    // 选中的位置
    private var mSelectPosition = -1

    // 开始触摸的位置
    private var mStartTouchY = 0f

    // 手指滑动的距离
    private var mMoveDistance = 0f

    companion object {
        private const val TAG = "MPickerView"
        private const val SPEED = 5f

        // 行距与mTextSizeNormal之比，保证View内显示的内容在适当的位置
        private const val RATE = 2.7f

        // '2021' 与 '年' 之间的距离
        private val TEXT_MARGIN = 2.dp2px()
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PickerView)
        mHighlightColor = ta.getColor(R.styleable.PickerView_pvHighlightColor, mHighlightColor)
        mText = ta.getString(R.styleable.PickerView_pvText)
        ta.recycle()

        mPaintSelect.textAlign = Paint.Align.CENTER
        mPaintSelect.textSize = mTextSizeSelect
        mPaintSelect.color = mHighlightColor
        mPaintNormal.textAlign = Paint.Align.CENTER
        mPaintNormal.textSize = mTextSizeNormal
        mPaintNormal.color = 0xFF999999.toInt()
        mPaintText.textSize = 15.dp2px().toFloat()
        mPaintText.color = mHighlightColor
        mPaintLine.strokeWidth = 0.5.dp2px().toFloat()
        mPaintLine.color = 0xFFDDDDDD.toInt()
    }

    fun setItems(items: List<String>): PickerView {
        mItems.clear()
        mItems.addAll(items)
        mRawItems.clear()
        mRawItems.addAll(items)
        mSelectPosition = items.size / 2
        requestLayout()
        return this
    }

    fun setText(text: String?): PickerView {
        mText = text
        requestLayout()
        return this
    }

    fun setHighlightColor(@ColorInt color: Int): PickerView {
        mHighlightColor = color
        mPaintSelect.color = mHighlightColor
        mPaintText.color = mHighlightColor
        postInvalidate()
        return this
    }

    fun setOnSelect(onSelect: ((v: View, item: String) -> Unit)): PickerView {
        mOnSelect = onSelect
        return this
    }

    fun getSelectedPosition(): Int {
        return mRawItems.indexOf(getSelectedItem())
    }

    fun setSelectedPosition(position: Int): PickerView {
        if (mRawItems.isNotEmpty()) {
            setSelectedItem(mRawItems[position])
        }
        return this
    }

    fun getSelectedItem(): String {
        return if (mItems.size > 0) mItems[mSelectPosition] else ""
    }

    fun setSelectedItem(item: String): PickerView {
        if (mItems.size > 0) {
            for (i in mItems.indices) {
                if (item == mItems[i]) {
                    setSelectedPositionInternal(i)
                    break
                }
            }
        }
        return this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // 默认宽高
        val mDefaultWidth = (mPaintSelect.measureText(getMaxLengthItem())
                + mPaintText.measureText(mText ?: "")
                + TEXT_MARGIN).toInt()
        val mAnIntSelect = mPaintSelect.fontMetricsInt
        val mAnIntNormal = mPaintNormal.fontMetricsInt
        val mDefaultHeight = (mAnIntSelect.bottom - mAnIntSelect.top) + (mAnIntNormal.bottom - mAnIntNormal.top) * 4
        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT
                && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mDefaultWidth, mDefaultHeight)
        } else if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mDefaultWidth, heightSize)
        } else if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, mDefaultHeight)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mItems.isEmpty()) {
            return
        }
        // 绘制中间位置
        draw(canvas, 1, 0, mPaintSelect)
        // 绘制上方数据
        for (i in 1..mSelectPosition) {
            draw(canvas, -1, i, mPaintNormal)
        }
        // 绘制下方数据
        for (i in 1 until mItems.size - mSelectPosition) {
            draw(canvas, 1, i, mPaintNormal)
        }
    }

    private fun draw(canvas: Canvas, type: Int, position: Int, paint: Paint) {
        val space = RATE * mTextSizeNormal * position + type * mMoveDistance
        val scale = parabola(height / 4f, space)
        val size = (mTextSizeSelect - mTextSizeNormal) * scale + mTextSizeNormal
        val alpha = ((mTextAlphaSelect - mTextAlphaNormal) * scale + mTextAlphaNormal).toInt()
        paint.textSize = size
        paint.alpha = alpha
        val x = width / 2f
        val y = height / 2f + type * space
        val fontMetrics = paint.fontMetricsInt
        val baseline = y + (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.descent
        val textWidth = mPaintText.measureText(mText ?: "")
        // 超出 view 区域部分不绘制
        if ((baseline + fontMetrics.ascent) > 0 && (baseline + fontMetrics.descent) < height) {
            canvas.drawText(mItems[mSelectPosition + type * position], x - textWidth / 2 + (paddingStart - paddingEnd), baseline, paint)
        }
        if (position == 0) {
            mPaintSelect.textSize = mTextSizeSelect
            if (!TextUtils.isEmpty(mText)) {
                val startX = mPaintSelect.measureText(getMaxLengthItem()) / 2 + x - textWidth / 2 + TEXT_MARGIN + (paddingStart - paddingEnd)
                val anInt = mPaintText.fontMetricsInt
                canvas.drawText(mText!!, startX, height / 2f + (anInt.bottom - anInt.top) / 2f - anInt.descent, mPaintText)
            }
            val line = height / 2f + (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.descent
            canvas.drawLine(0f, line + fontMetrics.ascent - 2.dp2px(), width.toFloat(), line + fontMetrics.ascent - 2.dp2px(), mPaintLine)
            canvas.drawLine(0f, line + fontMetrics.descent + 2.dp2px(), width.toFloat(), line + fontMetrics.descent + 2.dp2px(), mPaintLine)
        }
    }

    private fun parabola(zero: Float, x: Float): Float {
        val y = (1 - Math.pow((x / zero).toDouble(), 2.0)).toFloat()
        return if (y < 0) 0f else y
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                mMoveDistance += event.y - mStartTouchY
                if (mMoveDistance > RATE * mTextSizeNormal / 2) {
                    // 向下滑动
                    moveTailToHead()
                    mMoveDistance -= RATE * mTextSizeNormal
                } else if (mMoveDistance < -RATE * mTextSizeNormal / 2) {
                    // 向上滑动
                    moveHeadToTail()
                    mMoveDistance += RATE * mTextSizeNormal
                }
                mStartTouchY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (abs(mMoveDistance) < 0.0001) {
                    mMoveDistance = 0f
                    return true
                }
                if (mTask != null) {
                    mTask!!.cancel()
                    mTask = null
                }
                mTask = MTimerTask(mHandler)
                mTimer.schedule(mTask, 0, 10)
            }
        }
        return true
    }

    private fun moveHeadToTail() {
        if (mItems.isNotEmpty()) {
            val head = mItems[0]
            mItems.removeAt(0)
            mItems.add(head)
        }
    }

    private fun moveTailToHead() {
        if (mItems.isNotEmpty()) {
            val tail = mItems[mItems.size - 1]
            mItems.removeAt(mItems.size - 1)
            mItems.add(0, tail)
        }
    }

    private fun setSelectedPositionInternal(position: Int) {
        mSelectPosition = position
        val value = mItems.size / 2 - mSelectPosition
        if (value < 0) {
            for (i in 0 until -value) {
                moveHeadToTail()
                mSelectPosition--
            }
        } else if (value > 0) {
            for (i in 0 until value) {
                moveTailToHead()
                mSelectPosition++
            }
        }
        invalidate()
    }

    private fun getMaxLengthItem(): String {
        var maxLengthItem = ""
        mItems.forEach {
            if (it.length > maxLengthItem.length) {
                maxLengthItem = it
            }
        }
        return maxLengthItem
    }

    internal class MHandler(view: View) : Handler() {
        private val mWeakReference = WeakReference(view)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val pickerView = mWeakReference.get() as PickerView? ?: return
            if (abs(pickerView.mMoveDistance) < SPEED) {
                pickerView.mMoveDistance = 0f
                if (pickerView.mTask != null) {
                    pickerView.mTask!!.cancel()
                    pickerView.mTask = null
                    pickerView.mOnSelect?.invoke(pickerView, pickerView.mItems[pickerView.mSelectPosition])
                }
            } else {
                pickerView.mMoveDistance = pickerView.mMoveDistance -
                        pickerView.mMoveDistance / abs(pickerView.mMoveDistance) * SPEED
            }
            pickerView.invalidate()
        }
    }

    internal class MTimerTask(private var handler: Handler) : TimerTask() {
        override fun run() {
            handler.sendMessage(handler.obtainMessage())
        }
    }
}