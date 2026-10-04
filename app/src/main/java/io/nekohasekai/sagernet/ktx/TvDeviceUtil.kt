package io.nekohasekai.sagernet.ktx

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.InputDevice
import android.view.KeyEvent

/**
 * Определяет нужно ли использовать TV-интерфейс.
 * Работает на:
 * - Android TV (Leanback launcher)
 * - Приставки с планшетным Android + пульт (даже если touchscreen=true)
 * - Эмуляторы Android TV
 */
object TvDeviceUtil {
    
    /**
     * true если устройство требует TV-интерфейса:
     * 1. UiModeManager сообщает TV режим, ИЛИ
     * 2. Есть DPAD navigation + нет multi-touch (приставка с пультом)
     */
    fun isTvDevice(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTvMode = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        
        // Проверяем наличие DPAD navigation
        val hasDpad = hasDpadNavigation()
        
        // Проверяем multi-touch (реальные тач-устройства поддерживают multi-touch)
        val hasMultiTouch = context.packageManager.hasSystemFeature("android.hardware.touchscreen.multitouch")
        
        // TV mode ИЛИ (есть DPAD и нет multi-touch = приставка с пультом)
        return isTvMode || (hasDpad && !hasMultiTouch)
    }
    
    /**
     * Проверяет есть ли у устройства DPAD navigation через InputDevice
     */
    private fun hasDpadNavigation(): Boolean {
        val deviceIds = InputDevice.getDeviceIds()
        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            
            // Устройство с DPAD navigation (не тачскрин)
            if ((sources and InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD &&
                (sources and InputDevice.SOURCE_TOUCHSCREEN) != InputDevice.SOURCE_TOUCHSCREEN) {
                return true
            }
            
            // Клавиатура с directional pad (пульты часто эмулируют как keyboard)
            if (device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC &&
                device.hasKeys(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN)[0]) {
                return true
            }
        }
        return false
    }
    
    /**
     * Более мягкая проверка для включения focus-highlight
     */
    fun hasHardwareNavigation(context: Context): Boolean {
        return isTvDevice(context) || hasDpadNavigation()
    }
}
