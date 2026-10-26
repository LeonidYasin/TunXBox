package io.nekohasekai.sagernet.ui.tv

/** Pure policy: focus/selection never silently changes an already running connection. */
enum class TvVpnPhase { IDLE, CONNECTING, CONNECTED, STOPPING, STOPPED;
    companion object {
        fun fromServiceName(name: String): TvVpnPhase = values().firstOrNull { it.name.equals(name, true) } ?: IDLE
    }
}
enum class TvVpnCommand { START, STOP, RELOAD, NONE }
object TvInteractionPolicy {
    fun primary(phase: TvVpnPhase, selectedId: Long, activeId: Long, ready: Boolean): TvVpnCommand {
        if (!ready) return TvVpnCommand.NONE
        return when (phase) {
            TvVpnPhase.CONNECTING -> TvVpnCommand.STOP
            TvVpnPhase.CONNECTED -> if (selectedId > 0 && selectedId != activeId) TvVpnCommand.RELOAD else TvVpnCommand.STOP
            TvVpnPhase.STOPPING -> TvVpnCommand.NONE
            else -> if (selectedId > 0) TvVpnCommand.START else TvVpnCommand.NONE
        }
    }
    fun media(phase: TvVpnPhase, selectedId: Long, ready: Boolean = true): TvVpnCommand {
        if (!ready) return TvVpnCommand.NONE
        return when (phase) {
            TvVpnPhase.CONNECTING, TvVpnPhase.CONNECTED -> TvVpnCommand.STOP
            TvVpnPhase.STOPPING -> TvVpnCommand.NONE
            else -> if (selectedId > 0) TvVpnCommand.START else TvVpnCommand.NONE
        }
    }
    fun connectOnly(phase: TvVpnPhase, selectedId: Long, activeId: Long, ready: Boolean = true): TvVpnCommand {
        if (!ready || selectedId <= 0 || phase == TvVpnPhase.CONNECTING || phase == TvVpnPhase.STOPPING) return TvVpnCommand.NONE
        return if (phase == TvVpnPhase.CONNECTED) {
            if (selectedId == activeId) TvVpnCommand.NONE else TvVpnCommand.RELOAD
        } else TvVpnCommand.START
    }
    fun canSelect(phase: TvVpnPhase) = phase != TvVpnPhase.CONNECTING && phase != TvVpnPhase.STOPPING
    fun canMutate(profileId: Long, activeId: Long, phase: TvVpnPhase) =
        !(profileId == activeId && phase in setOf(TvVpnPhase.CONNECTING, TvVpnPhase.CONNECTED, TvVpnPhase.STOPPING))
    fun focusIndex(ids: List<Long>, savedId: Long): Int = ids.indexOf(savedId).takeIf { it >= 0 } ?: 0
}

/** Immutable rows allow DiffUtil to update labels without destroying focused views. */
data class TvAction(val id: Long, val title: String, val subtitle: String, val icon: Int, val available: Boolean = true)
data class TvProfileCard(val id: Long, val name: String, val protocol: String, val address: String,
                         val selected: Boolean, val activePhase: TvVpnPhase?,val groupId: Long=0)
data class TvEmptyHint(val message: String,val groupId: Long=0)
data class TvGroupCard(val id: Long,val name: String,val count: Int,val selected: Boolean,val collapsed: Boolean=false)
data class TvFunctionCard(val id: Int,val title: String)

data class TvQualityCard(val id: Long,val title: String,val result: String,val age: String,val traffic: String)

data class TvConnectionCard(val id: Long,val name: String,val sent: String,val received: String,val result: String)
