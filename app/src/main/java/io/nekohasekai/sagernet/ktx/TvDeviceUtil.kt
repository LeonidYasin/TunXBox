package io.nekohasekai.sagernet.ktx

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration

/**
 * Определяет нужно ли использовать TV-интерфейс.
 * Упрощённая надёжная логика:
 * 1. UiModeManager сообщает TV режим (официальный Android TV)
 * 2. ИЛИ конфигурация указывает UI_MODE_TYPE_TELEVISION
 */
object TvDeviceUtil {
    
    fun isTvDevice(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val currentMode = uiModeManager?.currentModeType ?: Configuration.UI_MODE_TYPE_UNDEFINED
        
        // Официальный Android TV режим
        if (currentMode == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true
        }
        
        // Проверка через ресурсы конфигурации (работает на некоторых приставках)
        val configMode = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        return configMode == Configuration.UI_MODE_TYPE_TELEVISION
    }
    
    /**
     * Принудительная проверка для отладки — всегда возвращает true
     * Использовать только для тестирования TV UI на любом устройстве
     */
    fun forceTvMode(): Boolean = true
}
