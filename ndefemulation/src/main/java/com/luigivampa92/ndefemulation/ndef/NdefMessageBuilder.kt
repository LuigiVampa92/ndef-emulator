package com.luigivampa92.ndefemulation.ndef

import android.nfc.NdefMessage

internal interface NdefMessageBuilder {
    fun build(ndefData: NdefData): NdefMessage?
}
