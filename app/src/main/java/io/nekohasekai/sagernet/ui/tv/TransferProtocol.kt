package io.nekohasekai.sagernet.ui.tv

import java.io.EOFException
import java.io.InputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.security.MessageDigest
import java.security.SecureRandom

/** Pure JVM helpers, shared by the server and QR clients. */
object TransferProtocol {
    const val MAX_BODY_BYTES = 2 * 1024 * 1024
    const val SESSION_MILLIS = 10 * 60 * 1000L

    fun newToken(): String = ByteArray(32).also { SecureRandom().nextBytes(it) }
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    fun tokenMatches(expected: String, supplied: String): Boolean =
        supplied.length == expected.length && MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8), supplied.toByteArray(Charsets.UTF_8))

    fun readBody(input: InputStream, length: Int): ByteArray {
        require(length in 1..MAX_BODY_BYTES) { "Body must be between 1 byte and 2 MiB" }
        val body = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val n = input.read(body, offset, length - offset)
            if (n < 0) throw EOFException("Incomplete request body")
            if (n == 0) {
                val byte = input.read()
                if (byte < 0) throw EOFException("Incomplete request body")
                body[offset++] = byte.toByte()
            } else offset += n
        }
        return body
    }

    fun readLimited(input: InputStream): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            if (n == 0) {
                val byte = input.read()
                if (byte < 0) break
                require(output.size() < MAX_BODY_BYTES) { "Response exceeds 2 MiB" }
                output.write(byte)
            } else {
                require(output.size() + n <= MAX_BODY_BYTES) { "Response exceeds 2 MiB" }
                output.write(buffer, 0, n)
            }
        }
        return output.toByteArray()
    }

    /** Do not let a scanned QR redirect credential-bearing requests onto the public Internet. */
    fun requireLanAddress(host: String, port: Int) {
        require(port in 1..65535) { "Invalid port" }
        require(host.matches(Regex("[0-9]{1,3}(\\.[0-9]{1,3}){3}"))) { "Expected a LAN IPv4 address" }
        val address = InetAddress.getByName(host)
        require(address is Inet4Address && address.isSiteLocalAddress && !address.isLoopbackAddress) {
            "Transfer requires a private LAN address"
        }
    }
}
