package com.luigivampa92.ndefemulation.ndef

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.luigivampa92.ndefemulation.Logger
import java.io.UnsupportedEncodingException
import java.util.Locale

internal class LocationNdefMessageBuilder : NdefMessageBuilder {

    override fun build(ndefData: NdefData): NdefMessage? {
        if (ndefData !is LocationNdefData) {
            return null
        }
        val locationData: LocationNdefData = ndefData
        if (!validateLocationData(locationData)) {
            Logger.d("Unable to create location NDEF data - location coordinates values are out of range (latitude must be within range -90.0 to 90.0, longitude must be within range -180.0 to 180.0")
            return null
        }
        val ndefRecord = createLocationNdefRecord(locationData)
        return NdefMessage(ndefRecord)
    }

    private fun validateLocationData(locationData: LocationNdefData) =
        locationData.latitude <= 90.0 && locationData.latitude >= -90.0 &&
        locationData.longitude <= 180.0 && locationData.longitude >= -180.0

    private fun createLocationNdefRecord(locationData: LocationNdefData): NdefRecord {
        val locationTextLanguage = ""
        val languageBytes: ByteArray
        val textBytes: ByteArray
        try {
            languageBytes = locationTextLanguage.toByteArray(charset("US-ASCII"))
            val locationString = String.format(Locale.US, "geo:%.08f,%.08f", locationData.latitude, locationData.longitude)
            textBytes = locationString.toByteArray(charset("UTF-8"))
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