package com.luigivampa92.ndefemulation

import android.annotation.SuppressLint
import android.content.Context
import com.luigivampa92.ndefemulation.ndef.NdefData
import com.luigivampa92.ndefemulation.ndef.NdefDataSerializer

/**
 * Main interface class to interact with the NDEF emulation library
 *
 * The public API consists of only 1 variable "currentEmulatedNdefData" (for Kotlin)
 * Or 2 methods "getCurrentEmulatedNdefData()" and "setCurrentEmulatedNdefData(NdefData)" respectively (for Java)
 *
 * Usage:
 * - Create an instance of the "NdefEmulation" class anywhere where you have access to an Android Context object
 * - Set the value of "currentEmulatedNdefData" to one of the "NdefData" types of objects
 * - Done! From now on, your device will emulate an NDEF tag over the NFC in the background, no extra interactions are needed
 *
 * The usage is very simple and takes just one line of code. Here is an example (kotlin):
 * NdefEmulation(this).currentEmulatedNdefData = UriNdefData("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
 *
 * Once the NDEF message is assigned, it can be read by various NFC readers that support NDEF, or Android and iOS smartphones
 * The emulated NdefData value persists over launches. If you close the app, it will still emulate the tag once the device enters the near field
 *
 * If you need to stop the emulation, set "currentEmulatedNdefData" to null. Here is an example (kotlin):
 * NdefEmulation(this).currentEmulatedNdefData = null
 *
 * List of supported emulated NdefData types:
 * - UriNdefData         - Emulates an URI. Most useful and often case. Think of it as a QR-code but via NFC. It can be a URL to a webpage or an application deeplink. Works well with both Android and iOS natively
 * - LocationNdefData    - Emulates a geolocation point. Works well on both Android and iOS but on iOS it might require an app that supports the "geo" deeplink schema
 * - WifiNetworkNdefData - Emulates a wifi hotspot connection data (SSID, password). Works natively on Android but not on iOS
 * - ContactNdefData     - Emulates a VCard contact card. Works natively on Android but not on iOS
 * - TextNdefData        - Emulates a raw text message. Does not work natively neither on Android nor on iOS. Only for applications that expect certain texts via NDEF
 * - NdefRecordData      - Emulates an arbitrary android.nfc.NdefRecord object. For instance, you can read another NDEF tag via the Android NFC API and then send it to be emulated by the library
 *
 * @see com.luigivampa92.ndefemulation.ndef.UriNdefData
 * @see com.luigivampa92.ndefemulation.ndef.LocationNdefData
 * @see com.luigivampa92.ndefemulation.ndef.WifiNetworkNdefData
 * @see com.luigivampa92.ndefemulation.ndef.ContactNdefData
 * @see com.luigivampa92.ndefemulation.ndef.TextNdefData
 * @see com.luigivampa92.ndefemulation.ndef.NdefRecordData
 *
 */
@SuppressLint("ApplySharedPref")
class NdefEmulation(context: Context) {

    private companion object {
        private const val PREF_FILE_NAME = "nfc_ndef_emulation"
        private const val PREF_KEY_EMULATION_DATA = "emulation_ndef_data"
    }

    private val storage = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    /**
     * The variable that represents the current data that is emulated by the library.
     * This value persists over the launches and is still emulated if the application process is closed.
     * Set the NdefData object you need to emulate to start the emulation.
     * Set null to stop the emulation.
     */
    var currentEmulatedNdefData: NdefData?
        get() = NdefDataSerializer.deserializeData(storage.getString(PREF_KEY_EMULATION_DATA, null))
        set(value) {
            if (value != null) {
                storage.edit().putString(PREF_KEY_EMULATION_DATA, NdefDataSerializer.serializeData(value)).commit()
            } else {
                storage.edit().clear().commit()
            }
        }
}