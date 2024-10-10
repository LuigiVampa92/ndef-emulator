package com.luigivampa92.ndefemulation.hce

import android.nfc.NdefMessage
import com.luigivampa92.ndefemulation.DataUtil
import com.luigivampa92.ndefemulation.Logger
import com.luigivampa92.ndefemulation.defineByteArrayOf
import com.luigivampa92.ndefemulation.ndef.NdefData
import com.luigivampa92.ndefemulation.ndef.NdefConstants
import com.luigivampa92.ndefemulation.ndef.NdefMessageFactory
import com.luigivampa92.ndefemulation.toPositiveInt

internal class NfcType4TagNdefEmulator (ndefData: NdefData) : ApduExecutor {

    private companion object {
        private val NDEF_AID = defineByteArrayOf(0xD2, 0x76, 0x00, 0x00, 0x85, 0x01, 0x01)
        private val NDEF_FID_CC = defineByteArrayOf(0xE1, 0x03)
        private val NDEF_CC_READ_MODE_GRANTED = defineByteArrayOf(0x00)
        private val NDEF_CC_WRITE_MODE_FORBIDDEN = defineByteArrayOf(0xFF)
        private const val NDEF_FID_DATA_SIZE_RESERVE_BYTE_COUNT = 2
    }

    private val ndefMessage: NdefMessage
    private val ndefMessageBinary: ByteArray
    private val ndefMessageBinarySize: ByteArray
    private val ndefMessageMaxSize: ByteArray

    private var adfSelected = false
    private var efCcSelected = false
    private var efNdefSelected = false

    init {
        val ndefMessageBuilder = NdefMessageFactory.getMessageBuilder(ndefData)
        ndefMessage = ndefMessageBuilder.build(ndefData) ?: NdefMessage(defineByteArrayOf())
        ndefMessageBinary = ndefMessage.toByteArray()
        ndefMessageBinarySize = DataUtil.shortToBytes(ndefMessageBinary.size.toShort())
        ndefMessageMaxSize = DataUtil.shortToBytes((ndefMessageBinary.size + NDEF_FID_DATA_SIZE_RESERVE_BYTE_COUNT).toShort())
        Logger.d("ndef emulator started")
        Logger.d("ndefData: $ndefData")
        Logger.d("ndefMessage: $ndefMessage")
        Logger.d("ndefMessageBinary: " + DataUtil.toHexString(ndefMessageBinary, ""))
    }

    override fun transmitApdu(apdu: ByteArray): ByteArray {
        try {
            val cApdu = DataUtil.parseCommandApdu(apdu)

            // SELECT ADF NFC forum type 4 tag
            if (cApdu.cla == 0x00.toByte()
                && cApdu.ins == 0xA4.toByte()
                && cApdu.p1 == 0x04.toByte()
                && cApdu.p2 == 0x00.toByte()
                && cApdu.lc != null && cApdu.lc.size == 1 && cApdu.lc[0] == 0x07.toByte()
                && cApdu.data != null && cApdu.data.size == cApdu.lc[0].toPositiveInt()
            ) {
                return if (NDEF_AID.contentEquals(cApdu.data)) {
                    adfSelected = true
                    ApduConstants.SW_OK
                } else {
                    ApduConstants.SW_ERROR_NO_SUCH_DF
                }
            }

            // SELECT EF NFC forum type 4 tag CC
            if (cApdu.cla == 0x00.toByte()
                && cApdu.ins == 0xA4.toByte()
                && cApdu.p1 == 0x00.toByte()
                && cApdu.p2 == 0x0C.toByte()
                && cApdu.lc != null && cApdu.lc.size == 1 && cApdu.lc[0] == 0x02.toByte()
                && cApdu.le == null
                && cApdu.data != null && cApdu.data.size == cApdu.lc[0].toPositiveInt()
                && adfSelected && !efCcSelected && !efNdefSelected
            ) {
                return if (NDEF_FID_CC.contentEquals(cApdu.data)) {
                    efCcSelected = true
                    ApduConstants.SW_OK
                } else {
                    ApduConstants.SW_ERROR_NO_SUCH_DF
                }
            }

            // READ EF NFC forum type 4 tag CC
            if (cApdu.cla == 0x00.toByte()
                && cApdu.ins == 0xB0.toByte()
                && cApdu.p1 == 0x00.toByte()
                && cApdu.p2 == 0x00.toByte()
                && cApdu.lc == null
                && cApdu.data == null
                && cApdu.le != null && cApdu.le.size == 1 && cApdu.le[0] == 0x0F.toByte()
                && adfSelected && efCcSelected && !efNdefSelected
            ) {
                val ccPrefix = defineByteArrayOf(0x00, 0x0F, 0x20, 0xFF, 0xFF, 0xFF, 0xFF, 0x04, 0x06)
                return DataUtil.concatByteArrays(ccPrefix, NdefConstants.NDEF_FID_DATA, ndefMessageMaxSize, NDEF_CC_READ_MODE_GRANTED, NDEF_CC_WRITE_MODE_FORBIDDEN, ApduConstants.SW_OK)
            }

            // SELECT EF NDEF data
            // * checking only for adf selection for simplicity and not CC or NDEF selection because ios tend to duplicate select fid commands multiple times
            if (cApdu.cla == 0x00.toByte()
                && cApdu.ins == 0xA4.toByte()
                && cApdu.p1 == 0x00.toByte()
                && cApdu.p2 == 0x0C.toByte()
                && cApdu.lc != null && cApdu.lc.size == 1 && cApdu.lc[0] == 0x02.toByte()
                && cApdu.le == null
                && cApdu.data != null && cApdu.data.size == cApdu.lc[0].toPositiveInt()
                && adfSelected
            ) {
                return if (NdefConstants.NDEF_FID_DATA.contentEquals(cApdu.data)) {
                    efCcSelected = false
                    efNdefSelected = true
                    ApduConstants.SW_OK
                } else {
                    ApduConstants.SW_ERROR_NO_SUCH_DF
                }
            }

            // READ EF NDEF data
            // * taking multiple last bytes as Le here because android can sometimes send 3-bytes Le in extended apdu format instead of standard 1 byte
            // * returning content in slices here because one response may not be enough to transfer the entire data, also ios requests NDEF file data in multiple steps: first it requests TLV length of data (first 2 bytes), then the data but with incorrect offset (0 instead of 2) and then the last two bytes of content (offset == TLV length of data)
            if (cApdu.cla == 0x00.toByte()
                && cApdu.ins == 0xB0.toByte()
                && cApdu.le != null && cApdu.le.size > 0 && cApdu.le.size <= 2
                && adfSelected && !efCcSelected && efNdefSelected
            ) {
                val offset = DataUtil.bytesToShort(byteArrayOf(cApdu.p1, cApdu.p2))
                val length = DataUtil.bytesToShort(cApdu.le)
                val responseFull = DataUtil.concatByteArrays(ndefMessageBinarySize, ndefMessageBinary)
                val responseChunk = responseFull.sliceArray(offset until offset + length)
                val response = ByteArray(responseChunk.size + ApduConstants.SW_OK.size)
                System.arraycopy(responseChunk, 0, response, 0, responseChunk.size)
                System.arraycopy(ApduConstants.SW_OK, 0, response, responseChunk.size, ApduConstants.SW_OK.size)
                return response
            }

            return ApduConstants.SW_ERROR_GENERAL
        }
        catch (e: Throwable) {
            return ApduConstants.SW_ERROR_GENERAL
        }
    }
}
