package tc.me.jumphelper

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import br.tiagohm.markdownview.MarkdownView
import tc.me.jumphelper.util.DensityUtil
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    companion object {
        val MODE_SIDE = 0
        val MODE_FULL = 1

        val USE_ACCESSIBILITY = 0
        val USE_ROOT_SHELL = 1
        val USE_NOTHING = -1
    }

    private lateinit var mFloatView: View

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
    private var pressTime: TextView? = null

    private val timeFormat = DecimalFormat("0.0")

    private var useType = USE_NOTHING

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

        mFloatView = LayoutInflater.from(this).inflate(R.layout.view_panel, null, false)
        initViews(mFloatView)

        windowManager.addView(mFloatView, mSideModeParams)

        updateFloatWindow(MODE_SIDE, mFloatView)
        loadUserGuideData()
    }

    override fun onResume() {
        super.onResume()
        chooseAndConfigEnvironment()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mFloatView.isInitialized) {
            windowManager.removeViewImmediate(mFloatView)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun initViews(rootView: View) {
        itemExpand = rootView.findViewById(R.id.item_expand)
        itemMore = rootView.findViewById(R.id.item_more)
        itemMove = rootView.findViewById(R.id.item_move)
        itemReset = rootView.findViewById(R.id.item_reset)
        itemSend = rootView.findViewById(R.id.item_send)
        itemClose = rootView.findViewById(R.id.item_close)
        helperView = rootView.findViewById(R.id.helper_view)
        copyright = rootView.findViewById(R.id.tv_copyright)
        pressTime = rootView.findViewById(R.id.tv_show_press_time)

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
            hidePredictPressTime()
        }

        helperView?.setOnDistanceCalculatedListener {
            showPredictPressTime(JumpHelper.getPressTime(it))
            currentDistance = it
        }

        itemClose?.setOnClickListener {
            windowManager.removeView(rootView)
            finish()
        }

        itemSend?.setOnClickListener {
            if (useType == USE_NOTHING) {
                return@setOnClickListener
            }
            PressCountDownTimer(JumpHelper.getPressTime(currentDistance), 10).start()
            val rootViewLocation = IntArray(2)
            rootView.getLocationOnScreen(rootViewLocation)
            JumpHelper.jump(this, useType, currentDistance, rootViewLocation[1].toFloat())
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
        pressTime?.visibility = visibility

        itemClose?.visibility = if (mode == MODE_SIDE) View.VISIBLE else View.GONE

        itemExpand?.setImageResource(if (mode == MODE_SIDE) R.mipmap.ic_arrow_left else R.mipmap.ic_arrow_right)

        windowManager.updateViewLayout(rootView, if (mode == MODE_SIDE) mSideModeParams else mFullModeParams)
        currentMode = mode
    }

    private fun loadUserGuideData() {
        val markdownView = findViewById<MarkdownView>(R.id.markdown_view)
        markdownView.loadMarkdownFromUrl("https://raw.githubusercontent.com/classTC/JumpHelper/master/README.md")
    }

    private fun showPredictPressTime(pressTimeInMillis: Long) {
        pressTime?.text = String.format(getString(R.string.text_time_format),
                timeFormat.format(pressTimeInMillis / 1000f))
        pressTime?.visibility = View.VISIBLE
    }

    private fun hidePredictPressTime() {
        pressTime?.text = ""
        pressTime?.visibility = View.GONE
    }

    private fun chooseAndConfigEnvironment() {
        useType = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {// if N and later, use AccessibilityService
                if (!isAccessibilitySettingsOn(this)) {
                    guideToOpenAccessibilityService()
                }
                USE_ACCESSIBILITY
            }
            CommandHelper.isDeviceRoot() -> // if device is rooted, use root shell
                USE_ROOT_SHELL
            else -> {
                Toast.makeText(this, "当前设备不支持", Toast.LENGTH_SHORT).show()
                USE_NOTHING
            }
        }
    }

    private fun guideToOpenAccessibilityService() {
        AlertDialog.Builder(this)
                .setTitle("开启辅助功能")
                .setMessage("使用此功能需要开启辅助服务，是否前往设置开启？")
                .setPositiveButton("前往设置", { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                })
                .create()
                .show()
    }

    inner class PressCountDownTimer(totalTimeInMillis: Long, intervalInMillis: Long)
        : CountDownTimer(totalTimeInMillis, intervalInMillis) {
        override fun onFinish() {
        }

        override fun onTick(millisUntilFinished: Long) {
            runOnUiThread {
                pressTime?.text = String.format(getString(R.string.text_time_format),
                        timeFormat.format(millisUntilFinished / 1000f))
            }
        }
    }

    private fun isAccessibilitySettingsOn(mContext: Context): Boolean {
        val service = packageName + "/" + EventAccessibilityService::class.java.canonicalName
        val accessibilityEnabled = try {
            Settings.Secure.getInt(mContext.contentResolver,
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            0
        }

        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')

        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(mContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
