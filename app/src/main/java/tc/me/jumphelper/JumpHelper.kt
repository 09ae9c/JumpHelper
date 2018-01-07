package tc.me.jumphelper

import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.support.annotation.RequiresApi
import tc.me.jumphelper.util.DensityUtil

/**
 * Created by classTC on 31/12/2017. JumpHelper
 */
object JumpHelper {

    // Thanks https://github.com/wangshub/wechat_jump_game/blob/master/wechat_jump.py
    private val WQHDCoefficient = 1.392
    private val defaultCoefficient = 1.35

    var accessibilityService: EventAccessibilityService? = null

    /**
     * 调用 shell 执行滑屏操作
     * @param distance 滑动距离
     * @param windowTopY 当前悬浮窗顶部坐标 Y 值
     * @param coefficient 按压系数
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun jump(context: Context, useType: Int, distance: Float, windowTopY: Float, coefficient: Double = defaultCoefficient) {
        val pressTime = getPressTime(distance, coefficient)
        val screenWidth = DensityUtil.getScreenWidthInPx(context)

        val startX = screenWidth * 0.2
        val endX = screenWidth * 0.8
        val lineY = windowTopY - 20

        val cmd = "input swipe $startX $lineY $endX $lineY $pressTime "
        if (useType == MainActivity.USE_ACCESSIBILITY) {
            swipe(startX.toInt(), lineY.toInt(), endX.toInt(), lineY.toInt(), pressTime)
        } else {
            Thread {
                CommandHelper.exec(cmd, true)
            }.start()
        }
    }

    fun getPressTime(distance: Float, coefficient: Double = defaultCoefficient): Long {
        return (distance * coefficient).toLong()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long) {
        val point1 = floatArrayOf(x1.toFloat(), y1.toFloat())
        val point2 = floatArrayOf(x2.toFloat(), y2.toFloat())

        val path = Path()
        path.moveTo(point1[0], point1[1])
        path.lineTo(point2[0], point2[1])

        val description = GestureDescription.StrokeDescription(path, 0, duration)

        val builder = GestureDescription.Builder().addStroke(description).build()
        accessibilityService?.dispatchGesture(builder, null, null)
    }
}
