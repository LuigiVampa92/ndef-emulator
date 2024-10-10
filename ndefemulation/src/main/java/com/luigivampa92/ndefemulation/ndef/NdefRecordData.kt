package com.luigivampa92.ndefemulation.ndef

import android.nfc.NdefRecord

data class NdefRecordData (
    val tnf: Short,
    val type: ByteArray,
    val id: ByteArray,
    val payload: ByteArray,
) : NdefData() {

    constructor(ndefRecord: NdefRecord) : this(ndefRecord.tnf, ndefRecord.type, ndefRecord.id, ndefRecord.payload)

    override fun hashCode(): Int {
        var result = tnf.toInt()
        result = 31 * result + type.contentHashCode()
        result = 31 * result + id.contentHashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NdefRecordData
        if (tnf != other.tnf) return false
        if (!type.contentEquals(other.type)) return false
        if (!id.contentEquals(other.id)) return false
        if (!payload.contentEquals(other.payload)) return false
        return true
    }
}