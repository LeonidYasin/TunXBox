package io.nekohasekai.sagernet.ui.tv

/**
 * Opt-in automatic failover for the active TV/phone profile.
 *
 * Concept: when the current connection fails or is lost and the user enabled the
 * auto-switch mode with a fallback list, the active profile moves to the next entry of the
 * user-ordered list. Every profile of the list is tried at most once per failover chain, so a
 * dead provider cannot bounce the connection between the same two profiles: the chain walks the
 * whole list and then stops for manual diagnosis. A successful check or a manual selection
 * restarts the chain.
 *
 * Safety rails: manual override wins, cooldown plus hysteresis, and a per-session switch cap.
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
    val triedIds: List<Long> = emptyList(),
) {
    fun canSwitch(nowMs: Long): Boolean = enabled && fallbackOrder.isNotEmpty() && switchesThisSession < maxSwitchesPerSession && nowMs >= cooldownUntilMs
}

object TvAutoFailoverPolicy {
    const val DEFAULT_COOLDOWN_MS: Long = 30_000L

    /**
     * Next profile in the user's order after [currentId], wrapping around and skipping every
     * profile already tried in this chain. The just-failed profile is always in [triedIds]; the
     * explicit check keeps it out even if the caller forgot to add it.
     *
     * Rotation instead of "lowest saved ping" is deliberate: saved pings go stale and the lowest
     * one is usually the profile that just failed, which made two profiles trade places forever.
     */
    fun selectNextFallback(fallbackOrder: List<Long>, currentId: Long, triedIds: Set<Long>): Long? {
        if (fallbackOrder.isEmpty()) return null
        val start = fallbackOrder.indexOf(currentId)
        val rotated = if (start >= 0) fallbackOrder.drop(start + 1) + fallbackOrder.take(start + 1) else fallbackOrder
        return rotated.firstOrNull { it != currentId && it !in triedIds }
    }

    /** Stable preference encoding: positive profile ids only, user order preserved, no duplicates. */
    fun encodeFallbackOrder(ids: List<Long>): String = ids.filter { it > 0L }.distinct().joinToString(",")

    fun decodeFallbackOrder(raw: String?): List<Long> = raw
        ?.split(',')
        ?.mapNotNull { it.trim().toLongOrNull() }
        ?.filter { it > 0L }
        ?.distinct()
        ?: emptyList()

    fun decide(currentHealthy: Boolean, state: TvFailoverState, candidateId: Long, nowMs: Long): TvFailoverDecision {
        if (!state.enabled) return TvFailoverDecision(TvFailoverAction.NONE, reason = "disabled")
        if (state.fallbackOrder.isEmpty()) return TvFailoverDecision(TvFailoverAction.NONE, reason = "no-fallback")
        if (currentHealthy) return TvFailoverDecision(TvFailoverAction.NONE, reason = "healthy")
        if (candidateId <= 0L || candidateId !in state.fallbackOrder) return TvFailoverDecision(TvFailoverAction.NONE, reason = "not-enabled")
        if (!state.canSwitch(nowMs)) return TvFailoverDecision(TvFailoverAction.NONE, reason = "cooldown-or-cap")
        return TvFailoverDecision(TvFailoverAction.SWITCH_TO, candidateId, reason = "connection-lost")
    }

    fun applySwitch(state: TvFailoverState, candidateId: Long, nowMs: Long, cooldownMs: Long = DEFAULT_COOLDOWN_MS): TvFailoverState =
        state.copy(
            lastSwitchAtMs = nowMs,
            cooldownUntilMs = nowMs + cooldownMs,
            switchesThisSession = state.switchesThisSession + 1,
            triedIds = (state.triedIds + candidateId).distinct(),
        )
}
