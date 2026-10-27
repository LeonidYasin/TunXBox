package io.nekohasekai.sagernet.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanProxyAddressTest {
    private fun candidate(
        name: String,
        host: String? = "192.168.1.10",
        up: Boolean = true,
        loopback: Boolean = false,
        ipv4: Boolean = true,
    ) = LanProxyAddress.Candidate(name, host, up, loopback, ipv4)

    @Test fun formatsOnlyValuesAClientCouldActuallyEnter() {
        assertEquals("192.168.1.10:2080", LanProxyAddress.format("192.168.1.10", 2080))
        assertEquals("10.0.0.2:65535", LanProxyAddress.format(" 10.0.0.2 ", 65535))
        assertNull(LanProxyAddress.format(null, 2080))
        assertNull(LanProxyAddress.format("   ", 2080))
        assertNull(LanProxyAddress.format("192.168.1.10", 0))
        assertNull(LanProxyAddress.format("192.168.1.10", 65536))
    }

    @Test fun listsEveryAddressInsteadOfGuessingTheLocalNetwork() {
        // 4G, hotspot and tunnel addresses are all kept: only loopback is useless to clients.
        val entries = LanProxyAddress.collect(listOf(
            candidate("wlan0", "192.168.43.1"),
            candidate("rmnet_data0", "10.76.41.155"),
            candidate("tun0", "172.19.0.1"),
            candidate("lo", "127.0.0.1", loopback = true),
            candidate("wlan0", "fe80::1", ipv4 = false),
            candidate("wlan0", "192.168.43.2", up = false),
        ))
        assertEquals(listOf(
            LanProxyAddress.Entry("192.168.43.1", "wlan0"),
            LanProxyAddress.Entry("10.76.41.155", "rmnet_data0"),
            LanProxyAddress.Entry("172.19.0.1", "tun0"),
        ), entries)
    }

    @Test fun ordersWifiThenEthernetThenHotspotThenEverythingElse() {
        val entries = LanProxyAddress.collect(listOf(
            candidate("rmnet_data0", "10.20.30.40"),
            candidate("tun0", "172.19.0.1"),
            candidate("ap0", "192.168.43.1"),
            candidate("wlan0", "192.168.1.42"),
            candidate("eth0", "192.168.1.43"),
        ))
        assertEquals(
            listOf("192.168.1.42", "192.168.1.43", "192.168.43.1", "10.20.30.40", "172.19.0.1"),
            entries.map { it.host },
        )
    }

    @Test fun collapsesDuplicateAndEmptyAddresses() {
        val entries = LanProxyAddress.collect(listOf(
            candidate("wlan0", "192.168.1.42"),
            candidate("eth0", "192.168.1.42"),
            candidate("eth0", null),
            candidate("eth0", "   "),
        ))
        assertEquals(listOf(LanProxyAddress.Entry("192.168.1.42", "wlan0")), entries)
        assertEquals(emptyList<LanProxyAddress.Entry>(), LanProxyAddress.collect(emptyList()))
    }

    @Test fun worstCaseAddressKeepsEveryCharacter() {
        // 15 character IPv4 + colon + 5 digit port is the longest value a client can see.
        val address = LanProxyAddress.format("255.255.255.255", 65535)
        assertEquals("255.255.255.255:65535", address)
        assertEquals(21, address!!.length)
    }
}
