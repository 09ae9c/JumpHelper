package tc.me.jumphelper

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import tc.me.jumphelper.util.DensityUtil

class MainActivity : AppCompatActivity() {

    companion object {
        val MODE_SIDE = 0
        val MODE_FULL = 1
    }

    private var currentMode = MODE_SIDE

    private lateinit var mSideModeParams: WindowManager.LayoutParams
    private lateinit var mFullModeParams: WindowManager.LayoutParams

    private var fullModeWidth = 0
    private var sideModeWidth = 0
    private var defaultHeight = 0

    private var touchY = 0f
    private var lastTouchY = 0f

    private var currentDistance = 0f

    private var itemExpand: ImageView? = null
    private var itemMore: ImageView? = null
    private var itemMove: ImageView? = null
    private var itemReset: ImageView? = null
    private var itemSend: ImageView? = null
    private var itemClose: ImageView? = null
    private var helperView: HelperView? = null
    private var copyright: TextView? = null

    @SuppressLint("InflateParams")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fullModeWidth = DensityUtil.getScreenWidthInPx(this)
        sideModeWidth = DensityUtil.dip2px(this, 36f)
        defaultHeight = DensityUtil.dip2px(this, 320f)

        mSideModeParams = WindowManager.LayoutParams(sideModeWidth, defaultHeight / 2,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888)

        mSideModeParams.x = fullModeWidth - sideModeWidth

        mFullModeParams = WindowManager.LayoutParams(fullModeWidth, defaultHeight,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888)

        val floatLayout = LayoutInflater.from(this).inflate(R.layout.view_panel, null, false)
        initViews(floatLayout)

        windowManager.addView(floatLayout, mSideModeParams)

        updateFloatWindow(MODE_SIDE, floatLayout)
    }

    private fun initViews(rootView: View) {
        itemExpand = rootView.findViewById(R.id.item_expand)
        itemMore = rootView.findViewById(R.id.item_more)
        itemMove = rootView.findViewById(R.id.item_move)
        itemReset = rootView.findViewById(R.id.item_reset)
        itemSend = rootView.findViewById(R.id.item_send)
        itemClose = rootView.findViewById(R.id.item_close)
        helperView = rootView.findViewById(R.id.helper_view)
        copyright = rootView.findViewById(R.id.tv_copyright)

        itemExpand?.setOnClickListener {
            updateFloatWindow(if (currentMode == MODE_SIDE) MODE_FULL else MODE_SIDE, rootView)
        }

        itemMove?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchY = if (lastTouchY == 0f) event.rawY else lastTouchY
                }
                MotionEvent.ACTION_MOVE -> {
                    mFullModeParams.y = (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(rootView, mFullModeParams)
                }
                MotionEvent.ACTION_UP -> {
                    lastTouchY = touchY
                }
            }
            return@setOnTouchListener true
        }

        itemReset?.setOnClickListener {
            helperView?.reset()
        }

        helperView?.setOnDistanceCalculatedListener {
            currentDistance = it
        }

        itemClose?.setOnClickListener {
            windowManager.removeView(rootView)
            finish()
        }

        itemSend?.setOnClickListener {

        }
    }

    private fun updateFloatWindow(mode: Int, rootView: View) {
        val visibility = if (mode == MODE_SIDE) View.GONE else View.VISIBLE
        itemMore?.visibility = visibility
        itemMove?.visibility = visibility
        itemReset?.visibility = visibility
        itemSend?.visibility = visibility
        helperView?.visibility = visibility
        copyright?.visibility = visibility

        itemClose?.visibility = if (mode == MODE_SIDE) View.VISIBLE else View.GONE

        itemExpand?.setImageResource(if (mode == MODE_SIDE) R.mipmap.ic_arrow_left else R.mipmap.ic_arrow_right)

        windowManager.updateViewLayout(rootView, if (mode == MODE_SIDE) mSideModeParams else mFullModeParams)
        currentMode = mode
    }
}
