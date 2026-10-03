package io.nekohasekai.sagernet.ktx

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build

/**
 * Определяет нужно ли использовать TV-интерфейс.
 * Работает на:
 * - Android TV (Leanback launcher)
 * - Приставки с планшетным Android + пульт (no touchscreen + DPAD)
 * - Эмуляторы Android TV
 */
object TvDeviceUtil {
    
    /**
     * true если устройство требует TV-интерфейса:
     * 1. UiModeManager сообщает TV режим, ИЛИ
     * 2. Нет тачскрина + есть hardware DPAD (приставка с планшетным Android)
     */
    fun isTvDevice(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTvMode = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        
        val pm = context.packageManager
        val hasTouchscreen = pm.hasSystemFeature("android.hardware.touchscreen")
        val hasDpad = pm.hasSystemFeature("android.hardware.faketouch") || 
                      !hasTouchscreen // нет тача = скорее всего пульт
        
        // TV mode ИЛИ (нет тачскрина и вероятно есть D-pad)
        return isTvMode || (!hasTouchscreen && Build.VERSION.SDK_INT >= 21)
    }
    
    /**
     * Более мягкая проверка: есть ли hardware навигация (D-pad/пульт)?
     * Используется для включения focus-highlight даже на гибридных устройствах.
     */
    fun hasHardwareNavigation(context: Context): Boolean {
        val pm = context.packageManager
        return !pm.hasSystemFeature("android.hardware.touchscreen") ||
               pm.hasSystemFeature("android.hardware.faketouch")
    }
}
