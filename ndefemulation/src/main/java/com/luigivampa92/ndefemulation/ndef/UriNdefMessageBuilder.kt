package com.luigivampa92.ndefemulation.ndef

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.luigivampa92.ndefemulation.Logger
import java.io.UnsupportedEncodingException
import java.net.URI

internal class UriNdefMessageBuilder : NdefMessageBuilder {

    private companion object {
        private const val MAX_LENGTH_URI_TEXT = 800
    }

    override fun build(ndefData: NdefData): NdefMessage? {
        if (ndefData !is UriNdefData) {
            return null
        }
        val uriData: UriNdefData = ndefData
        if (!validateUriData(uriData)) {
            return null
        }
        val ndefRecord = createUriNdefRecord(uriData.uri)
        return NdefMessage(ndefRecord)
    }

    private fun validateUriData(uriData: UriNdefData) =
        try {
            URI.create(uriData.uri)
            val uriHasValidLength = uriData.uri.length in 1..MAX_LENGTH_URI_TEXT
            if (!uriHasValidLength) {
                Logger.d("Unable to create URI NDEF data - URI size is invalid (must be not empty and less than $MAX_LENGTH_URI_TEXT bytes")
            }
            uriHasValidLength
        } catch (e: Throwable) {
            Logger.d("Unable to create URI NDEF data - URI is not valid")
            false
        }

    private fun createUriNdefRecord(uriText: String): NdefRecord {
        val uriLanguage = ""
        val languageBytes: ByteArray
        val textBytes: ByteArray
        try {
            languageBytes = uriLanguage.toByteArray(charset("US-ASCII"))
            textBytes = uriText.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e)
        }

        val recordPayload = ByteArray(1 + (languageBytes.size and 0x03F) + textBytes.size)
        recordPayload[0] = (languageBytes.size and 0x03F).toByte()
        System.arraycopy(languageBytes, 0, recordPayload, 1, languageBytes.size and 0x03F)
        System.arraycopy(textBytes, 0, recordPayload, 1 + (languageBytes.size and 0x03F), textBytes.size)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, NdefConstants.NDEF_FID_DATA, recordPayload)
    }
}