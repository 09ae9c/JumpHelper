package tc.me.jumphelper

import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import tc.me.jumphelper.JumpHelper.accessibilityService

/**
 * Created by classTC on 03/01/2018. EventAccessibilityService
 */
class EventAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        accessibilityService = this
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}