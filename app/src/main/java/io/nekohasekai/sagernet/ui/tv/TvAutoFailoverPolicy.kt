package io.nekohasekai.sagernet.ui.tv

/**
 * Opt-in automatic failover for the active TV/phone profile.
 *
 * Concept: when the current connection fails or is lost and the user enabled the
 * auto-switch mode with a fallback list, the active profile is switched to the best
 * (lowest measured ping) enabled fallback. Safety rails: manual override wins, cooldown
 * plus hysteresis, per-session switch cap, only confirmed-healthy candidates.
 */
enum class TvFailoverAction { NONE, SWITCH_TO }

const val DEFAULT_MAX_SWITCHES: Int = 2

data class TvFailoverDecision(val action: TvFailoverAction, val candidateId: Long = -1L, val reason: String? = null)

data class TvFailoverState(
    val enabled: Boolean = false,
    val fallbackOrder: List<Long> = emptyList(),
    val cooldownUntilMs: Long = 0L,
    val lastSwitchAtMs: Long = 0L,
    val switchesThisSession: Int = 0,
    val maxSwitchesPerSession: Int = DEFAULT_MAX_SWITCHES,
) {
    fun canSwitch(nowMs: Long): Boolean = enabled && fallbackOrder.isNotEmpty() && switchesThisSession < maxSwitchesPerSession && nowMs >= cooldownUntilMs
}

object TvAutoFailoverPolicy {
    const val DEFAULT_COOLDOWN_MS: Long = 30_000L

    fun selectFallback(fallbackOrder: List<Long>, healthyIds: Set<Long>): Long? = fallbackOrder.firstOrNull { it in healthyIds }

    fun decide(currentHealthy: Boolean, state: TvFailoverState, candidateId: Long, nowMs: Long): TvFailoverDecision {
        if (!state.enabled) return TvFailoverDecision(TvFailoverAction.NONE, reason = "disabled")
        if (state.fallbackOrder.isEmpty()) return TvFailoverDecision(TvFailoverAction.NONE, reason = "no-fallback")
        if (currentHealthy) return TvFailoverDecision(TvFailoverAction.NONE, reason = "healthy")
        if (candidateId <= 0L || candidateId !in state.fallbackOrder) return TvFailoverDecision(TvFailoverAction.NONE, reason = "not-enabled")
        if (!state.canSwitch(nowMs)) return TvFailoverDecision(TvFailoverAction.NONE, reason = "cooldown-or-cap")
        return TvFailoverDecision(TvFailoverAction.SWITCH_TO, candidateId, reason = "connection-lost")
    }

    fun applySwitch(state: TvFailoverState, candidateId: Long, nowMs: Long, cooldownMs: Long = DEFAULT_COOLDOWN_MS): TvFailoverState =
        state.copy(lastSwitchAtMs = nowMs, cooldownUntilMs = nowMs + cooldownMs, switchesThisSession = state.switchesThisSession + 1)
}
