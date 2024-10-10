package com.luigivampa92.ndefemulation.ndef

import android.annotation.SuppressLint
import android.nfc.NdefRecord
import com.luigivampa92.ndefemulation.DataUtil
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

internal object NdefDataSerializer {

    private const val delimeter = "`"

    internal fun serializeData(ndefData: NdefData): String? {
        try {
            val ndefDataType: NdefDataType = when (ndefData) {
                is NdefRecordData -> NdefDataType.NDEF_RECORD
                is TextNdefData -> NdefDataType.PLAIN_TEXT
                is UriNdefData -> NdefDataType.GENERIC_URI
                is ContactNdefData -> NdefDataType.VCARD_CONTACT
                is WifiNetworkNdefData -> NdefDataType.WIFI_CONFIG
                is LocationNdefData -> NdefDataType.GEO_LOCATION
            }
            val sb = StringBuilder(ndefDataType.name + delimeter)
            when (ndefDataType) {
                NdefDataType.NDEF_RECORD -> {
                    if (ndefData !is NdefRecordData) return null
                    sb.append(arrayOf(
                        ndefData.tnf.toString(),
                        DataUtil.toHexStringLowercase(ndefData.type, ""),
                        DataUtil.toHexStringLowercase(ndefData.id, ""),
                        DataUtil.toHexStringLowercase(ndefData.payload, ""),
                    ).joinToString(delimeter))
                }
                NdefDataType.PLAIN_TEXT -> {
                    if (ndefData !is TextNdefData) return null
                    sb.append(ndefData.text)
                }
                NdefDataType.GENERIC_URI -> {
                    if (ndefData !is UriNdefData) return null
                    sb.append(ndefData.uri)
                }
                NdefDataType.VCARD_CONTACT -> {
                    if (ndefData !is ContactNdefData) return null
                    sb.append(arrayOf(
                        ndefData.firstName,
                        valueOrEmpty(ndefData.lastName),
                        valueOrEmpty(ndefData.phoneNumber),
                        valueOrEmpty(ndefData.email),
                        valueOrEmpty(ndefData.birthday?.toVCardDate()),
                        valueOrEmpty(ndefData.jobCompany),
                        valueOrEmpty(ndefData.jobTitle),
                        valueOrEmpty(ndefData.siteUrl),
                        valueOrEmpty(ndefData.notes),
                    ).joinToString(delimeter))
                }
                NdefDataType.WIFI_CONFIG -> {
                    if (ndefData !is WifiNetworkNdefData) return null
                    sb.append(arrayOf(
                        ndefData.wifiName,
                        ndefData.wifiProtection.name,
                        valueOrEmpty(ndefData.wifiPassword)
                    ).joinToString(delimeter))
                }
                NdefDataType.GEO_LOCATION -> {
                    if (ndefData !is LocationNdefData) return null
                    sb.append(arrayOf(
                        ndefData.latitude.toString(),
                        ndefData.longitude.toString()
                    ).joinToString(delimeter))
                }
            }
            return sb.toString()
        } catch (e: Throwable) {
            return null
        }
    }

    internal fun deserializeData(serializedData: String?): NdefData? {
        try {
            val values: List<String> = serializedData?.split(delimeter) ?: return null
            val ndefDataType: NdefDataType = NdefDataType.valueOf(values[0])
            return when (ndefDataType) {
                NdefDataType.NDEF_RECORD -> NdefRecordData(
                    NdefRecord(
                        values[1].toShort(),
                        DataUtil.hexStringToByteArray(values[2]),
                        DataUtil.hexStringToByteArray(values[3]),
                        DataUtil.hexStringToByteArray(values[4]),
                    )
                )
                NdefDataType.PLAIN_TEXT -> TextNdefData(values[1])
                NdefDataType.GENERIC_URI -> UriNdefData(values[1])
                NdefDataType.WIFI_CONFIG -> WifiNetworkNdefData(values[1], WifiNetworkNdefDataProtectionType.valueOf(values[2]), values[3])
                NdefDataType.VCARD_CONTACT -> ContactNdefData(
                    values[1],
                    values[2].ifBlank { null },
                    values[3].ifBlank { null },
                    values[4].ifBlank { null },
                    parseDateValue(values[5]),
                    values[6].ifBlank { null },
                    values[7].ifBlank { null },
                    values[8].ifBlank { null },
                    values[9].ifBlank { null },
                )
                NdefDataType.GEO_LOCATION -> LocationNdefData(values[1].toDouble(), values[2].toDouble())
            }
        } catch (e: Throwable) {
            return null
        }
    }

    private fun parseDateValue(value: String?): Date? {
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            dateFromVCardDateString(value)
        } catch (e: ParseException) {
            null
        }
    }

    private fun valueOrEmpty(value: String?) = value ?: ""

    @SuppressLint("SimpleDateFormat")
    private val vcardDateFormat = SimpleDateFormat("yyyy-MM-dd")

    private fun Date.toVCardDate() = vcardDateFormat.format(this)

    private fun dateFromVCardDateString(value: String) = vcardDateFormat.parse(value)

}