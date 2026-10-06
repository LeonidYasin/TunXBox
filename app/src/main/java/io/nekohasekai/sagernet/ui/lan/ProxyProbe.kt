package io.nekohasekai.sagernet.ui.lan

import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.*

/** Protocol evidence, NOT Internet reachability. An ordinary HTTP server must not become a proxy. */
enum class ProbeKind { SOCKS5, SOCKS5_AUTH, HTTP_AUTH, HTTP_UNVERIFIED, TCP_UNVERIFIED }
data class ProxyCandidate(val host: String, val port: Int, val kind: ProbeKind)
class ProxyProbe(private val socketFactory: () -> Socket, private val connectMs: Int = 250, private val readMs: Int = 700) {
    private suspend fun <T> exchange(host: String, port: Int, request: ByteArray, read: (Socket) -> T): T? = coroutineScope {
        val socket = socketFactory()
        // Closing the bound socket interrupts blocking connect/read immediately on cancellation.
        val closer = launch(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
            try { awaitCancellation() } finally { runCatching { socket.close() } }
        }
        try {
            withContext(Dispatchers.IO) {
                socket.connect(InetSocketAddress(host, port), connectMs)
                socket.soTimeout = readMs
                socket.getOutputStream().write(request)
                socket.getOutputStream().flush()
                read(socket)
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: java.io.IOException) { null }
        finally { closer.cancelAndJoin(); runCatching { socket.close() } }
    }
    suspend fun probe(host: String, port: Int): ProxyCandidate? {
        var opened = false
        val socks = exchange(host, port, byteArrayOf(5, 2, 0, 2)) { socket ->
            opened = true
            val input = socket.getInputStream()
            if (input.read() != 5) null else when (input.read()) {
                0 -> ProbeKind.SOCKS5
                2 -> ProbeKind.SOCKS5_AUTH
                else -> null
            }
        }
        if (socks != null) return ProxyCandidate(host, port, socks)
        // Invalid loopback port: no external traffic, DNS lookup, credentials or tunnel payload.
        // HTTP status alone (including 200) cannot prove this listener forwards proxy traffic.
        val request = "CONNECT 127.0.0.1:0 HTTP/1.1\r\nHost: 127.0.0.1:0\r\nConnection: close\r\n\r\n".toByteArray(Charsets.US_ASCII)
        val http = exchange(host, port, request) { socket ->
            opened = true
            val line = StringBuilder()
            val input = socket.getInputStream()
            while (line.length < 128) {
                val byte = input.read()
                if (byte < 0 || byte == 10) break
                line.append(byte.toChar())
            }
            val status = Regex("^HTTP/1\\.[01] ([0-9]{3})(?:[ \\r].*)?$").matchEntire(line.toString().removeSuffix("\r"))?.groupValues?.get(1)
            when (status) {
                "407" -> ProbeKind.HTTP_AUTH
                null -> null
                else -> ProbeKind.HTTP_UNVERIFIED
            }
        }
        return (http ?: if (opened) ProbeKind.TCP_UNVERIFIED else null)?.let { ProxyCandidate(host, port, it) }
    }
}

data class ScanReport(val candidates: List<ProxyCandidate>, val timedOut: Boolean, val limited: Boolean)
class LanScanner(private val probe: suspend (String, Int) -> ProxyCandidate?, private val workers: Int = 8,
                 private val deadlineMs: Long = 45_000, private val maxResults: Int = 64) {
    init { require(workers in 1..8 && deadlineMs in 1..45_000 && maxResults in 1..64) }
    suspend fun scan(scope: LanScope, ports: List<Int>, progress: suspend (Int, Int) -> Unit = { _, _ -> }): ScanReport {
        require(ports.isNotEmpty() && ports.size <= 8 && ports.distinct().size == ports.size && ports.all { it in 1..65535 })
        val tasks = scope.hosts.flatMap { host -> ports.map { host to it } }
        val next = java.util.concurrent.atomic.AtomicInteger()
        val finished = java.util.concurrent.atomic.AtomicInteger()
        val results = java.util.concurrent.ConcurrentLinkedQueue<ProxyCandidate>()
        val done = withTimeoutOrNull(deadlineMs) {
            coroutineScope {
                repeat(workers) { launch {
                    while (isActive && results.size < maxResults) {
                        val index = next.getAndIncrement()
                        if (index >= tasks.size) break
                        val (host, port) = tasks[index]
                        probe(host, port)?.let { candidate ->
                            // Defensive boundary even if a probe implementation returns a different endpoint.
                            if (candidate.host == host && candidate.port == port) results.add(candidate)
                        }
                        val completed = finished.incrementAndGet()
                        if (completed % 8 == 0 || completed == tasks.size) progress(completed, tasks.size)
                    }
                } }
            }
            true
        }
        return ScanReport(results.toList().distinct().sortedWith(compareBy({ LanScope.ipv4(it.host) }, { it.port })).take(maxResults), done == null, results.size >= maxResults)
    }
}
