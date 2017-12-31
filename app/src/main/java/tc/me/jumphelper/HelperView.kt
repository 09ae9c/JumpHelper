package tc.me.jumphelper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import tc.me.jumphelper.util.DensityUtil

/**
 * Created by classTC on 30/12/2017. HelperView
 */
class HelperView(context: Context, attr: AttributeSet) : View(context, attr) {

    private val mStartPaint = Paint()
    private val mEndPaint = Paint()

    private var mStartPosition = Pair(0f, 0f)
    private var mEndPosition = Pair(0f, 0f)

    private var mPointRadius: Float

    private var mStartPressed = false
    private var mEndPressed = false

    private var onDistanceCalculated: ((distance: Float) -> Unit)? = null

    init {
        mStartPaint.isAntiAlias = true
        mStartPaint.color = Color.rgb(14, 182, 246)

        mEndPaint.isAntiAlias = true
        mEndPaint.color = Color.rgb(239, 83, 80)

        mPointRadius = DensityUtil.dip2px(context, 12f).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mStartPressed) {
            canvas?.drawCircle(mStartPosition.first, mStartPosition.second, mPointRadius, mStartPaint)
        }

        if (mEndPressed) {
            canvas?.drawCircle(mEndPosition.first, mEndPosition.second, mPointRadius, mEndPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mStartPressed) {
                    mEndPosition = event.x to event.y
                    mEndPressed = true
                } else {
                    mStartPosition = event.x to event.y
                    mStartPressed = !mStartPressed
                    mEndPressed = false
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                if (mEndPressed) {
                    val distance = calculateDistance(mStartPosition, mEndPosition)
                    onDistanceCalculated?.invoke(distance)
                }
            }
        }
        return true
    }

    private fun calculateDistance(startPosition: Pair<Float, Float>, endPosition: Pair<Float, Float>): Float {

        val x1 = startPosition.first.toDouble()
        val y1 = startPosition.second.toDouble()

        val x2 = endPosition.first.toDouble()
        val y2 = endPosition.second.toDouble()

        val distance = Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2) + Math.abs(y1 - y2) * Math.abs(y1 - y2))
        return distance.toFloat()
    }

    fun setOnDistanceCalculatedListener(listener: ((distance: Float) -> Unit)) {
        onDistanceCalculated = listener
    }

    fun reset() {
        mStartPressed = false
        mEndPressed = false
        invalidate()
    }
}