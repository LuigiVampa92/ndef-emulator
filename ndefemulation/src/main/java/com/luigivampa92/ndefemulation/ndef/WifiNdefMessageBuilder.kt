package com.luigivampa92.ndefemulation.ndef

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import com.luigivampa92.ndefemulation.Logger
import java.nio.ByteBuffer
import java.util.Arrays

internal class WifiNdefMessageBuilder : NdefMessageBuilder {

    private companion object {
        private const val CREDENTIAL_FIELD_ID: Short = 0x100E
        private const val SSID_FIELD_ID: Short = 0x1045
        private const val NETWORK_KEY_FIELD_ID: Short = 0x1027
        private const val AUTH_TYPE_FIELD_ID: Short = 0x1003
        private const val AUTH_TYPE_EXPECTED_SIZE: Short = 2
        private const val AUTH_TYPE_OPEN: Short = 0x0001
        private const val AUTH_TYPE_WPA_PSK: Short = 0x0002
        private const val AUTH_TYPE_WPA_EAP: Short = 0x0008
        private const val AUTH_TYPE_WPA2_EAP: Short = 0x0010
        private const val AUTH_TYPE_WPA2_PSK: Short = 0x0020
        private const val AUTH_TYPE_WPA_AND_WPA2_PSK: Short = 0x0022
        private const val MAX_MAC_ADDRESS_SIZE_BYTES = 6
        private const val MAX_SSID_SIZE_BYTES = 32
        private const val MIN_NETWORK_KEY_SIZE_BYTES = 8
        private const val MAX_NETWORK_KEY_SIZE_BYTES = 64

        private const val NFC_TOKEN_MIME_TYPE = "application/vnd.wfa.wsc"
        private val NDEF_MIME_TYPE_WIFI = NFC_TOKEN_MIME_TYPE.toByteArray()
    }

    override fun build(ndefData: NdefData): NdefMessage? {
        if (ndefData !is WifiNetworkNdefData) {
            return null
        }
        val wifiNetworkData: WifiNetworkNdefData = ndefData
        if (!validateWifiNetworkData(wifiNetworkData)) {
            return null
        }
        val recordPayload = generateNdefWifiNetworkPayload(wifiNetworkData.wifiName, wifiNetworkData.wifiPassword!!, wifiNetworkData.wifiProtection)
        val ndefRecord = NdefRecord(NdefRecord.TNF_MIME_MEDIA, NDEF_MIME_TYPE_WIFI, NdefConstants.NDEF_FID_DATA, recordPayload)
        return NdefMessage(ndefRecord)
    }

    private fun validateWifiNetworkData(wifiNetworkData: WifiNetworkNdefData): Boolean {
        if (wifiNetworkData.wifiName.isEmpty() || wifiNetworkData.wifiName.toByteArray().size > MAX_SSID_SIZE_BYTES) {
            Logger.d("Unable to create wifi NDEF data - SSID size is invalid (must be not empty and less than $MAX_SSID_SIZE_BYTES bytes")
            return false
        }
        if (WifiNetworkNdefDataProtectionType.PASSWORD == wifiNetworkData.wifiProtection && (
                    wifiNetworkData.wifiPassword.isNullOrEmpty() || wifiNetworkData.wifiPassword.toByteArray().size < MIN_NETWORK_KEY_SIZE_BYTES || wifiNetworkData.wifiPassword.toByteArray().size > MAX_NETWORK_KEY_SIZE_BYTES
                )) {
            Logger.d("Unable to create wifi NDEF data - password size is invalid (must be at least $MIN_NETWORK_KEY_SIZE_BYTES and less than $MAX_NETWORK_KEY_SIZE_BYTES bytes")
            return false
        }
        return true
    }

    private fun generateNdefWifiNetworkPayload(wifiName: String, wifiPassword: String, wifiNetworkProtectionType: WifiNetworkNdefDataProtectionType): ByteArray {
        val ssid: String = wifiName
        val ssidSize: Short = ssid.toByteArray().size.toShort()
        val authType: Short = if (WifiNetworkNdefDataProtectionType.PASSWORD == wifiNetworkProtectionType) AUTH_TYPE_WPA_AND_WPA2_PSK else AUTH_TYPE_OPEN
        val networkKey: String = wifiPassword
        val networkKeySize: Short = networkKey.toByteArray().size.toShort()
        val macAddress = ByteArray(MAX_MAC_ADDRESS_SIZE_BYTES)
        Arrays.fill(macAddress, 0xff.toByte())

        val bufferSize = if (WifiNetworkNdefDataProtectionType.PASSWORD == wifiNetworkProtectionType) {
            18 + ssidSize + networkKeySize
        } else {
            14 + ssidSize
        }
        val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
        buffer.putShort(CREDENTIAL_FIELD_ID)
        buffer.putShort((bufferSize - 4).toShort())
        buffer.putShort(SSID_FIELD_ID)
        buffer.putShort(ssidSize)
        buffer.put(ssid.toByteArray())
        buffer.putShort(AUTH_TYPE_FIELD_ID)
        buffer.putShort(AUTH_TYPE_EXPECTED_SIZE)
        buffer.putShort(authType)
        if (WifiNetworkNdefDataProtectionType.PASSWORD == wifiNetworkProtectionType) {
            buffer.putShort(NETWORK_KEY_FIELD_ID)
            buffer.putShort(networkKeySize)
            buffer.put(networkKey.toByteArray())
        }
        return buffer.array()
    }
}