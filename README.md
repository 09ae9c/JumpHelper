# JumpHelper

微信小游戏 跳一跳 辅助工具\[ No root required on Android 7.0 and later \]

# LIMITS

 **Android 7.0 and later** or **Any Android System with rooted**

# How to use
![how to use](http://7xu736.com1.z0.glb.clouddn.com/nufmi-cvmgi.gif)

# 原理

使用 AccessibilityService 在 Android 7.0 中新增的方法: `dispatchGesture` 模拟触摸事件
![IMAGE](http://7xu736.com1.z0.glb.clouddn.com/8CD10A4E3646DA1A30A3B76A4F3F2291.jpg)


## Step 1 计算两点距离
```
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
```

## Step 2 根据按压系数计算按压时间

```
// Thanks https://github.com/wangshub/wechat_jump_game/blob/master/wechat_jump.py
val defaultCoefficient = 1.35

fun getPressTime(distance: Float, coefficient: Double): Long {
      return (distance * coefficient).toLong()
}
```

## Step 3 执行按压操作

不关心滑动的起点和终点位置(要在游戏界面内)，只关心在屏幕上的按压时间 duration

```
    val path = Path()
    path.moveTo(point1[0], point1[1])
    path.lineTo(point2[0], point2[1])

    val description = GestureDescription.StrokeDescription(path, 0, duration)
    
    // AccessibilityService 实例
    service.dispatchGesture(GestureDescription.Builder().addStroke(description).build(), null, null)
```

Done!

以上实现的优点是
- 用户操作简单，不需要 PC 端配置，下载安装即可使用

限制是
- 并不是全自动，需要用户手动确定起点和终点
- 必须在 Android 7.0 及以上的设备上运行

# Android 设备上实现自动触发模拟事件的方法

- MotionEvent(必须在当前进程内使用)

- InputManager(必须在当前进程内使用)

- 测试框架(必须有开发工具)

- input 命令控制(必须连接安装有 ADB 的 PC / Root 后也可以直接在设备上执行)

- sendevent 命令(存在很大的兼容性问题，不考虑)

# 总结

对于 MotionEvent 和 InputManager 必须在当前进程内的限制，可以将应用伪装成系统应用，绕过权限，但是不普适，有一定操作难度

测试框架同理，有一定操作难度，不普适

input 命令可以作为 Android 7.0 以下的解决方案（前提是必须 root）

```
    val cmd = "input swipe $startX $lineY $endX $lineY $pressTime "
    Thread {
        CommandHelper.exec(cmd, true)
    }.start()

```