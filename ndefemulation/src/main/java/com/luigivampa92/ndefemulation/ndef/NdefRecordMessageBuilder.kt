package com.luigivampa92.ndefemulation.ndef

import android.nfc.NdefMessage
import android.nfc.NdefRecord

internal class NdefRecordMessageBuilder : NdefMessageBuilder {

    override fun build(ndefData: NdefData): NdefMessage? {
        if (ndefData !is NdefRecordData) {
            return null
        }
        val ndefRecordData: NdefRecordData = ndefData
        return NdefMessage(NdefRecord(ndefRecordData.tnf, ndefRecordData.type, ndefRecordData.id, ndefRecordData.payload))
    }
}