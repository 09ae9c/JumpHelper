package tc.me.jumphelper

import android.content.Context
import android.util.Log
import tc.me.jumphelper.util.DensityUtil

/**
 * Created by classTC on 31/12/2017. JumpHelper
 */
object JumpHelper {
    private val LOG_TAG = "JumpHelper"

    // Thanks https://github.com/wangshub/wechat_jump_game/blob/master/wechat_jump.py
    private val WQHDCoefficient = 1.392
    private val defaultCoefficient = 1.35

    /**
     * 调用 shell 执行滑屏操作
     * @param distance 滑动距离
     * @param windowTopY 当前悬浮窗顶部坐标 Y 值
     * @param coefficient 按压系数
     */
    fun jump(context: Context, distance: Float, windowTopY: Float, coefficient: Double = defaultCoefficient) {
        val pressTime = getPressTime(distance, coefficient)
        val screenWidth = DensityUtil.getScreenWidthInPx(context)

        val startX = screenWidth * 0.2
        val endX = screenWidth * 0.8
        val lineY = windowTopY - 20
        val cmd = "input swipe $startX $lineY $endX $lineY $pressTime "

        Log.i(LOG_TAG, "jump cmd is: $cmd")
        Thread {
            CommandHelper.exec(cmd, true)
        }.start()
    }

    fun getPressTime(distance: Float, coefficient: Double = defaultCoefficient): Int {
        return (distance * coefficient).toInt()
    }
}