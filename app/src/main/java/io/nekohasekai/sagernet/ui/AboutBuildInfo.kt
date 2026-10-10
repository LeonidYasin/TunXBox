package io.nekohasekai.sagernet.ui

/** Public build facts only; never attach provider URLs, profile names or raw logs. */
object AboutBuildInfo {
    fun format(facts: List<Pair<String, String>>): String = facts.joinToString("\n\n") { (label, value) ->
        "${label.replace('\n', ' ').replace('\r', ' ')}: ${value.replace('\n', ' ').replace('\r', ' ')}"
    }
}
