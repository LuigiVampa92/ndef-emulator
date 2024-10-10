package com.luigivampa92.ndefemulation.ndef

data class WifiNetworkNdefData(
    val wifiName: String,
    val wifiProtection: WifiNetworkNdefDataProtectionType,
    val wifiPassword: String? = null,
) : NdefData()