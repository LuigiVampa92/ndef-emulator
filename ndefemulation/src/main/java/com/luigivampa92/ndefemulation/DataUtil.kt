package com.luigivampa92.ndefemulation

import com.luigivampa92.ndefemulation.hce.ApduCommand
import java.util.Arrays
import kotlin.experimental.and

internal object DataUtil {

    internal fun toHexString(array: ByteArray?, delimeter: String = " "): String {
        val builder = StringBuilder()
        if (array != null) {
            for (i in array.indices) {
                builder.append(String.format("%02X%s", array[i], delimeter))
            }
        }
        return builder.toString()
    }

    internal fun toHexStringLowercase(array: ByteArray?, delimeter: String = " "): String {
        return toHexString(array, delimeter).lowercase()
    }

    internal fun hexStringToByteArray(hexString: String): ByteArray {
        return hexString.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    internal fun parseCommandApdu(apdu: ByteArray): ApduCommand {
        val origLen = apdu.size
        if (origLen < 4) {
            throw IllegalArgumentException("Invalid command length. Command is lesser than 4 bytes")
        }

        val cla = apdu[0]
        val ins = apdu[1]
        val p1 = apdu[2]
        val p2 = apdu[3]
        var Lc: ByteArray? = null
        var Le: ByteArray? = null
        var data: ByteArray? = null

        if (origLen == 4) {
            Lc = null
            Le = null
            data = null
        } else if (origLen == 5) {
            Lc = null
            Le = ByteArray(1)
            Le[0] = apdu[4]
            data = null
        } else if (origLen > 5) {
            val lcv = apdu[4].toPositiveInt()
            val fullApduTrailer = Arrays.copyOfRange(apdu, 5, origLen)

            // Lc is correct, Le is presented
            if (fullApduTrailer.size == lcv + 1) {
                Lc = ByteArray(1)
                Lc[0] = apdu[4]
                Le = ByteArray(1)
                Le[0] = fullApduTrailer[fullApduTrailer.size - 1]
                data = Arrays.copyOfRange(fullApduTrailer, 0, fullApduTrailer.size - 1)
                if (data!!.size != Lc[0].toPositiveInt()) { // foolproof
                    throw RuntimeException("ApduCommand data corrupted")
                }
            }
            // Lc is correct, Le is not presented
            else if (fullApduTrailer.size == lcv) {
                Lc = ByteArray(1)
                Lc[0] = apdu[4]
                Le = null
                data = fullApduTrailer
                if (data!!.size != Lc[0].toPositiveInt()) { // foolproof
                    throw RuntimeException("ApduCommand data corrupted")
                }
            }
            // special case for android - Lc is not present and Le is sent in extended apdu 3-byte-format
            // lets assume that message will always be shorter than 010000 and take last 2 bytes as Le
            else if (cla == 0x00.toByte() && ins == 0xb0.toByte() && origLen == 7 && apdu[4] == 0x00.toByte()) {
                Lc = null
                data = null
                Le = ByteArray(2)
                Le[0] = apdu[5]
                Le[1] = apdu[6]
            } else {
                throw IllegalArgumentException("Invalid command length. Invalid Lc field")
            }
        }

        return ApduCommand(cla, ins, p1, p2, Lc, data, Le)
    }

    internal fun concatByteArrays(vararg arrays: ByteArray): ByteArray {
        var len = 0
        var currentIndex = arrays.size

        for (var4 in 0 until currentIndex) {
            val array1 = arrays[var4]
            len += array1.size
        }

        val result = ByteArray(len)
        currentIndex = 0
        val var10 = arrays.size

        for (var6 in 0 until var10) {
            val array = arrays[var6]
            System.arraycopy(array, 0, result, currentIndex, array.size)
            currentIndex += array.size
        }

        return result
    }

    internal fun shortToBytes(value: Short): ByteArray {
        return byteArrayOf((value.toInt() shr 8 and 255).toByte(), (value and 255).toByte())
    }

    internal fun bytesToShort(bytes: ByteArray): Short {
        when (bytes.size) {
            2 -> {
                val b1 = (bytes[0].toPositiveInt() and 0xFF)
                val b2 = (bytes[1].toPositiveInt() and 0xFF)
                return ((b1 shl 8) or (b2)).toShort()
            }
            1 -> {
                val b1 = 0x00
                val b2 = (bytes[0].toPositiveInt() and 0xFF)
                return ((b1 shl 8) or (b2)).toShort()
            }
            0 -> {
                return 0
            }
            else -> throw RuntimeException("unexpected bytearray size (must be <= 2)")
        }
    }
}
