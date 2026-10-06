package io.nekohasekai.sagernet.ui.lan

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

/** Synthetic local fixtures. No provider URLs, credentials, public scans or native core. */
class LanDiscoveryTest {
    @Test fun numericPrivateAddressesOnlyAndNoDns() {
        for (bad in listOf("example.com", "8.8.8.8", "100.64.0.1", "127.0.0.1", "169.254.1.1", "192.168.001.1", "192.168.1.256", "::1", "192.168.1.1.evil"))
            assertNull(bad, LanScope.create(bad, 24))
        for (good in listOf("10.1.2.3", "172.16.1.2", "172.31.255.2", "192.168.1.3")) assertNotNull(LanScope.create(good, 24))
        assertNull(LanScope.create("172.32.0.1", 24))
    }
    @Test fun largeNetworkIsNarrowedAndLocalGatewayBroadcastExcluded() {
        val scope = LanScope.create("10.45.3.12", 8, setOf("10.45.3.1", "10.45.3.13"))!!
        assertEquals("10.45.3.0/24", scope.cidr); assertEquals(251, scope.hosts.size)
        for (excluded in listOf("10.45.3.0", "10.45.3.255", "10.45.3.12", "10.45.3.1", "10.45.3.13", "10.45.4.1")) assertFalse(scope.contains(excluded))
    }
    @Test fun smallSubnetNeverBroadens() {
        val scope = LanScope.create("192.168.1.9", 30)!!
        assertEquals(listOf("192.168.1.10"), scope.hosts)
        assertNull(LanScope.create("192.168.1.9", 31)); assertNull(LanScope.create("192.168.1.9", 0))
    }
    @Test fun portsRejectDuplicatesAmbiguityAndUnboundedInput() {
        assertEquals(listOf(1, 1080, 65535), LanScope.ports("1, 1080, 65535"))
        for (bad in listOf("", "80,80", "0", "65536", "80;1080", "80,", "http", "1,2,3,4,5,6,7,8,9", "-1")) assertNull(bad, LanScope.ports(bad))
    }
    private fun fixture(response: ByteArray, test: suspend (ProxyProbe, Int) -> Unit) = runBlocking {
        ServerSocket(0, 8, java.net.InetAddress.getByName("127.0.0.1")).use { server ->
            val thread = Thread {
                try { while (!server.isClosed) server.accept().use { socket ->
                    socket.soTimeout = 1000
                    val input = socket.getInputStream()
                    val bytes = ByteArray(4); repeat(4) { bytes[it] = input.read().toByte() }
                    if (String(bytes, Charsets.US_ASCII) == "CONN") {
                        val tail = StringBuilder()
                        while (tail.length < 256 && !tail.endsWith("\r\n\r\n")) {
                            val byte = input.read(); if (byte < 0) break; tail.append(byte.toChar())
                        }
                    }
                    socket.getOutputStream().write(response); socket.getOutputStream().flush()
                } } catch (_: java.io.IOException) { }
            }.apply { isDaemon = true; start() }
            try { test(ProxyProbe({ Socket() }, 200, 200), server.localPort) }
            finally { server.close(); thread.join(1000) }
        }
    }
    @Test fun socks5NoAuthIsVerifiedOnAnyPort() = fixture(byteArrayOf(5, 0)) { probe, port ->
        assertEquals(ProbeKind.SOCKS5, probe.probe("127.0.0.1", port)!!.kind)
    }
    @Test fun socks5AuthIsNotMistakenForReadyConnection() = fixture(byteArrayOf(5, 2)) { probe, port ->
        assertEquals(ProbeKind.SOCKS5_AUTH, probe.probe("127.0.0.1", port)!!.kind)
    }
    @Test fun rejectedOrTruncatedGreetingDoesNotVerifySocks() = fixture(byteArrayOf(5, -1)) { probe, port ->
        assertEquals(ProbeKind.TCP_UNVERIFIED, probe.probe("127.0.0.1", port)!!.kind)
    }
    @Test fun ordinaryHttp200IsNotAConfirmedProxy() = fixture("HTTP/1.1 200 OK\r\n\r\n".toByteArray()) { probe, port ->
        assertEquals(ProbeKind.HTTP_UNVERIFIED, probe.probe("127.0.0.1", port)!!.kind)
    }
    @Test fun proxy407IsExplicitlyAuthenticationRequired() = fixture("HTTP/1.1 407 Proxy Authentication Required\r\n\r\n".toByteArray()) { probe, port ->
        assertEquals(ProbeKind.HTTP_AUTH, probe.probe("127.0.0.1", port)!!.kind)
    }
    @Test fun malformedHttpAndSocksAreUnverified() = fixture("HTTP/2 200\r\n".toByteArray()) { probe, port ->
        assertEquals(ProbeKind.TCP_UNVERIFIED, probe.probe("127.0.0.1", port)!!.kind)
    }
    @Test fun closedPortReturnsNoCandidate() = runBlocking {
        val port = ServerSocket(0).use { it.localPort }
        assertNull(ProxyProbe({ Socket() }, 100, 100).probe("127.0.0.1", port))
    }
    @Test fun cancellationClosesBlockingReadPromptly() = runBlocking {
        ServerSocket(0).use { server ->
            val accepted = java.util.concurrent.CountDownLatch(1)
            val closed = java.util.concurrent.CountDownLatch(1)
            val thread = Thread {
                try { server.accept().use { socket ->
                    repeat(4) { socket.getInputStream().read() }; accepted.countDown()
                    if (socket.getInputStream().read() == -1) closed.countDown()
                } } catch (_: java.io.IOException) { closed.countDown() }
            }.apply { isDaemon = true; start() }
            val job = launch(Dispatchers.Default) { ProxyProbe({ Socket() }, 200, 700).probe("127.0.0.1", server.localPort) }
            assertTrue(accepted.await(2, java.util.concurrent.TimeUnit.SECONDS))
            withTimeout(500) { job.cancelAndJoin() }
            assertTrue(closed.await(500, java.util.concurrent.TimeUnit.MILLISECONDS)); thread.join(1000)
        }
    }
    @Test fun workersAreBoundedAndTargetsStayInScope() = runBlocking {
        val scope = LanScope.create("192.168.1.1", 24)!!
        val active = AtomicInteger(); val maximum = AtomicInteger(); val calls = AtomicInteger()
        val report = LanScanner({ host, port ->
            assertTrue(scope.contains(host)); assertEquals(1080, port)
            val concurrent = active.incrementAndGet(); maximum.updateAndGet { maxOf(it, concurrent) }
            calls.incrementAndGet(); delay(2); active.decrementAndGet(); null
        }).scan(scope, listOf(1080))
        assertTrue(maximum.get() in 1..8); assertEquals(scope.hosts.size, calls.get()); assertFalse(report.timedOut)
    }
    @Test fun deadlineReturnsPartialAndStopsWorkers() = runBlocking {
        val active = AtomicInteger()
        val report = LanScanner({ _, _ -> active.incrementAndGet(); try { delay(1000); null } finally { active.decrementAndGet() } }, deadlineMs = 50)
            .scan(LanScope.create("192.168.1.1", 24)!!, listOf(1080))
        assertTrue(report.timedOut); assertEquals(0, active.get())
    }
    @Test fun explicitCancellationPropagatesAndResultsAreCapped() = runBlocking {
        val scope = LanScope.create("192.168.1.1", 24)!!
        val report = LanScanner({ host, port -> ProxyCandidate(host, port, ProbeKind.SOCKS5) }, maxResults = 3).scan(scope, listOf(1080))
        assertEquals(3, report.candidates.size); assertTrue(report.limited)
        val job = async { LanScanner({ _, _ -> delay(1000); null }).scan(scope, listOf(1080)) }
        yield(); job.cancel(); try { job.await(); fail("Cancellation must propagate") } catch (_: CancellationException) { }
    }
    @Test fun probeCannotSubstituteAnOutOfScopeEndpoint() = runBlocking {
        val report = LanScanner({ _, _ -> ProxyCandidate("8.8.8.8", 80, ProbeKind.SOCKS5) })
            .scan(LanScope.create("192.168.1.9", 30)!!, listOf(1080))
        assertTrue(report.candidates.isEmpty())
    }
}
