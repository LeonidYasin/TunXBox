package io.nekohasekai.sagernet.ui.tv

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.InputStream

class TransferProtocolTest {
    @Test fun readsPartialStreamUntilComplete() {
        val expected = "{\"profiles\":\"vless://example\"}".toByteArray()
        val stream = object : ByteArrayInputStream(expected) {
            override fun read(b: ByteArray, off: Int, len: Int) = super.read(b, off, minOf(len, 3))
        }
        assertArrayEquals(expected, TransferProtocol.readBody(stream, expected.size))
    }
    @Test fun handlesZeroLengthReadWithoutSpinning() {
        val stream = object : InputStream() {
            override fun read() = 42
            override fun read(b: ByteArray, off: Int, len: Int) = 0
        }
        assertArrayEquals(byteArrayOf(42, 42), TransferProtocol.readBody(stream, 2))
    }
    @Test(expected = EOFException::class) fun rejectsTruncatedBody() {
        TransferProtocol.readBody(ByteArrayInputStream(byteArrayOf(1)), 10)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsOversizedBodyBeforeAllocation() {
        TransferProtocol.readBody(ByteArrayInputStream(byteArrayOf()), TransferProtocol.MAX_BODY_BYTES + 1)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsNegativeLength() {
        TransferProtocol.readBody(ByteArrayInputStream(byteArrayOf()), -1)
    }
    @Test fun createsLongRandomTokensAndChecksThem() {
        val token = TransferProtocol.newToken()
        assertEquals(64, token.length)
        assertTrue(token.matches(Regex("[0-9a-f]{64}")))
        assertNotEquals(token, TransferProtocol.newToken())
        assertTrue(TransferProtocol.tokenMatches(token, token))
        assertFalse(TransferProtocol.tokenMatches(token, ""))
        assertFalse(TransferProtocol.tokenMatches(token, "0".repeat(64)))
    }
    @Test fun acceptsPrivateLanAddress() {
        TransferProtocol.requireLanAddress("192.168.1.2", 8765)
        TransferProtocol.requireLanAddress("10.0.0.2", 8765)
        TransferProtocol.requireLanAddress("172.16.0.2", 8765)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsPublicAddress() {
        TransferProtocol.requireLanAddress("8.8.8.8", 8765)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsHostname() {
        TransferProtocol.requireLanAddress("example.com", 8765)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsLoopback() {
        TransferProtocol.requireLanAddress("127.0.0.1", 8765)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidPort() {
        TransferProtocol.requireLanAddress("192.168.1.2", 0)
    }
}
