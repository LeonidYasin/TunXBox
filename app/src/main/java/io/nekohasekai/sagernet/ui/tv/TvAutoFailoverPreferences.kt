package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.database.DataStore

/**
 * Persisted auto-failover settings: an explicit opt-in flag plus the user-ordered list of
 * profiles that may be selected automatically. Storage mirrors [TvUiPreferences]; the raw
 * encoding stays in [TvAutoFailoverPolicy] so it can be covered by JVM tests.
 */
object TvAutoFailoverPreferences {
    private const val KEY_ENABLED = "tv_auto_failover_enabled"
    private const val KEY_FALLBACK_ORDER = "tv_auto_failover_order"

    var enabled: Boolean
        get() = DataStore.configurationStore.getBoolean(KEY_ENABLED, false)
        set(value) = DataStore.configurationStore.putBoolean(KEY_ENABLED, value)

    var fallbackOrder: List<Long>
        get() = TvAutoFailoverPolicy.decodeFallbackOrder(
            DataStore.configurationStore.getString(KEY_FALLBACK_ORDER),
        )
        set(value) {
            DataStore.configurationStore.putString(
                KEY_FALLBACK_ORDER,
                TvAutoFailoverPolicy.encodeFallbackOrder(value),
            )
        }

    /** Clears the opt-in and the list; never touches the currently active profile. */
    fun reset() {
        enabled = false
        fallbackOrder = emptyList()
    }
}
