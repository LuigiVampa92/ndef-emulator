package com.luigivampa92.ndefemulation.ndef

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.luigivampa92.ndefemulation.Logger
import java.io.UnsupportedEncodingException

internal class TextNdefMessageBuilder : NdefMessageBuilder {

    private companion object {
        private const val MAX_LENGTH_TEXT = 800
    }

    override fun build(ndefData: NdefData): NdefMessage? {
        if (ndefData !is TextNdefData) {
            return null
        }
        val textData: TextNdefData = ndefData
        if (!validateTextData(textData)) {
            Logger.d("Unable to create text NDEF data - text size is invalid (must be not empty and less than $MAX_LENGTH_TEXT bytes")
            return null
        }
        val ndefRecord = createTextNdefRecord(textData.text)
        return NdefMessage(ndefRecord)
    }

    private fun validateTextData(textData: TextNdefData) = textData.text.length in 1..MAX_LENGTH_TEXT

    private fun createTextNdefRecord(text: String): NdefRecord {
        val textLanguage = ""
        val languageBytes: ByteArray
        val textBytes: ByteArray
        try {
            languageBytes = textLanguage.toByteArray(charset("US-ASCII"))
            textBytes = text.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e)
        }

        val recordPayload = ByteArray(1 + (languageBytes.size and 0x03F) + textBytes.size)
        recordPayload[0] = (languageBytes.size and 0x03F).toByte()
        System.arraycopy(languageBytes, 0, recordPayload, 1, languageBytes.size and 0x03F)
        System.arraycopy(textBytes, 0, recordPayload, 1 + (languageBytes.size and 0x03F), textBytes.size)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, NdefConstants.NDEF_FID_DATA, recordPayload)
    }
}