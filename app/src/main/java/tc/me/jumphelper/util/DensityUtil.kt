package tc.me.jumphelper.util

import android.content.Context

/**
 * Created by classTC on 30/12/2017. DensityUtil
 */
object DensityUtil {

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getScreenWidthInPx(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeightInPx(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }
}