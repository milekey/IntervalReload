package com.scaredeer.intervalreload

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.ParseException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/*
 * Original work Copyright (C) 2008 The Android Open Source Project
 * Modified work Copyright (C) 2019 Aslam Anver (https://github.com/aslamanver)
 * Modified work Copyright (C) 2023 milekey (https://github.com/milekey)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

private const val ORIGINATE_TIME_OFFSET = 24
private const val RECEIVE_TIME_OFFSET = 32
private const val TRANSMIT_TIME_OFFSET = 40
private const val NTP_PACKET_SIZE = 48

private const val NTP_PORT = 123
private const val NTP_MODE_CLIENT = 3
private const val NTP_VERSION = 3

// Number of seconds between Jan 1, 1900 and Jan 1, 1970
// 70 years plus 17 leap days
private const val OFFSET_1900_TO_1970 = (365L * 70L + 17L) * 24L * 60L * 60L

/**
 * {@hide}
 *
 * Simple SNTP client class for retrieving network time.
 *
 * Sample usage:
 * <pre>SntpClient client = new SntpClient();
 * if (client.requestTime("time.foo.com")) {
 * long now = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
 * }
 * </pre>
 */
class SNTPClient {

    class TimeResponse(val datetimeString: String, val zonedDateTime: ZonedDateTime)

    companion object {
        const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

        @Throws(ParseException::class, Exception::class)
        suspend fun getDate(zoneId: ZoneId = ZoneId.systemDefault()): TimeResponse {
            return withContext(Dispatchers.IO) {
                val sntpClient = SNTPClient()

                sntpClient.requestTime("time.google.com", 5000)

                val zonedDateTime =
                    Instant.ofEpochSecond(sntpClient.ntpTime / 1000L).atZone(zoneId)
                val datetimeString = zonedDateTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT))

                TimeResponse(datetimeString, zonedDateTime)
            }
        }
    }

    /**
     * Returns the time computed from the NTP transaction.
     *
     * @return time value computed from NTP server response.
     */
    var ntpTime: Long = 0
        private set

    /**
     * Returns the reference clock value (value of SystemClock.elapsedRealtime())
     * corresponding to the NTP time.
     *
     * @return reference clock corresponding to the NTP time.
     */
    private var ntpTimeReference: Long = 0

    /**
     * Returns the round trip time of the NTP transaction
     *
     * @return round trip time in milliseconds.
     */
    private var roundTripTime: Long = 0

    /**
     * Sends an SNTP request to the given host and processes the response.
     *
     * @param host    host name of the server.
     * @param timeout network timeout in milliseconds.
     * @return true if the transaction was successful.
     */
    @Throws(Exception::class)
    fun requestTime(host: String, timeout: Int) {
        DatagramSocket().use { socket ->
            socket.soTimeout = timeout
            val address = InetAddress.getByName(host)
            val buffer = ByteArray(NTP_PACKET_SIZE)
            val request = DatagramPacket(buffer, buffer.size, address, NTP_PORT)

            // set mode = 3 (client) and version = 3
            // mode is in low 3 bits of first byte
            // version is in bits 3-5 of first byte
            buffer[0] = (NTP_MODE_CLIENT or (NTP_VERSION shl 3)).toByte()

            // get current time and write it to the request packet
            val requestTime = System.currentTimeMillis()
            val requestTicks = SystemClock.elapsedRealtime()
            writeTimeStamp(buffer, TRANSMIT_TIME_OFFSET, requestTime)
            socket.send(request)

            // read the response
            val response = DatagramPacket(buffer, buffer.size)
            socket.receive(response)
            val responseTicks = SystemClock.elapsedRealtime()
            val responseTime = requestTime + (responseTicks - requestTicks)

            // extract the results
            val originateTime = readTimeStamp(buffer, ORIGINATE_TIME_OFFSET)
            val receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET)
            val transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET)
            val roundTripTime = responseTicks - requestTicks - (transmitTime - receiveTime)
            val clockOffset = (receiveTime - originateTime + (transmitTime - responseTime)) / 2

            // save our results - use the times on this side of the network latency
            // (response rather than request time)
            ntpTime = responseTime + clockOffset
            ntpTimeReference = responseTicks
            this.roundTripTime = roundTripTime
        }
    }

    /**
     * Reads an unsigned 32 bit big endian number from the given offset in the buffer.
     */
    private fun read32(buffer: ByteArray, offset: Int): Long {
        val b0 = buffer[offset]
        val b1 = buffer[offset + 1]
        val b2 = buffer[offset + 2]
        val b3 = buffer[offset + 3]

        // convert signed bytes to unsigned values
        val i0 = if (b0.toInt() and 0x80 == 0x80) (b0.toInt() and 0x7F) + 0x80 else b0.toInt()
        val i1 = if (b1.toInt() and 0x80 == 0x80) (b1.toInt() and 0x7F) + 0x80 else b1.toInt()
        val i2 = if (b2.toInt() and 0x80 == 0x80) (b2.toInt() and 0x7F) + 0x80 else b2.toInt()
        val i3 = if (b3.toInt() and 0x80 == 0x80) (b3.toInt() and 0x7F) + 0x80 else b3.toInt()
        return (i0.toLong() shl 24) + (i1.toLong() shl 16) + (i2.toLong() shl 8) + i3.toLong()
    }

    /**
     * Reads the NTP time stamp at the given offset in the buffer and returns
     * it as a system time (milliseconds since January 1, 1970).
     */
    private fun readTimeStamp(buffer: ByteArray, offset: Int): Long {
        val seconds = read32(buffer, offset)
        val fraction = read32(buffer, offset + 4)
        return (seconds - OFFSET_1900_TO_1970) * 1000 + fraction * 1000L / 0x100000000L
    }

    /**
     * Writes system time (milliseconds since January 1, 1970) as an NTP time stamp
     * at the given offset in the buffer.
     */
    private fun writeTimeStamp(buffer: ByteArray, givenOffset: Int, time: Long) {
        var offset = givenOffset
        var seconds = time / 1000L
        val milliseconds = time - seconds * 1000L
        seconds += OFFSET_1900_TO_1970

        // write seconds in big endian format
        buffer[offset++] = (seconds shr 24).toByte()
        buffer[offset++] = (seconds shr 16).toByte()
        buffer[offset++] = (seconds shr 8).toByte()
        buffer[offset++] = (seconds shr 0).toByte()

        val fraction = milliseconds * 0x100000000L / 1000L
        // write fraction in big endian format
        buffer[offset++] = (fraction shr 24).toByte()
        buffer[offset++] = (fraction shr 16).toByte()
        buffer[offset++] = (fraction shr 8).toByte()
        // low order bits should be random data
        buffer[offset] = (Math.random() * 255.0).toInt().toByte()
    }
}