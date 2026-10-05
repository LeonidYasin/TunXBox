package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.database.DataStore

/** UI choice is a global preference, not part of the temporary profile editor cache. */
object TvUiPreferences {
    private const val KEY = "ui_mode_override"
    var phoneMode: Boolean
        get() {
            val saved = DataStore.configurationStore.getString(KEY)
            if (saved != null) return saved == "phone"
            // Migrate the previous PR implementation without losing an existing choice.
            val cached = DataStore.profileCacheStore.getString(KEY)
            if (cached != null) {
                DataStore.configurationStore.putString(KEY, cached)
                DataStore.profileCacheStore.remove(KEY)
            }
            return cached == "phone"
        }
        set(value) {
            DataStore.configurationStore.putString(KEY, if (value) "phone" else "tv")
            DataStore.profileCacheStore.remove(KEY)
        }
}
