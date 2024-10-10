package com.luigivampa92.ndefemulation.hce

internal data class ApduCommand(
    val cla: Byte,
    val ins: Byte,
    val p1: Byte,
    val p2: Byte,
    val lc: ByteArray?,
    val data: ByteArray?,
    val le: ByteArray?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ApduCommand
        if (cla != other.cla) return false
        if (ins != other.ins) return false
        if (p1 != other.p1) return false
        if (p2 != other.p2) return false
        if (lc != null) {
            if (other.lc == null) return false
            if (!lc.contentEquals(other.lc)) return false
        } else if (other.lc != null) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (le != null) {
            if (other.le == null) return false
            if (!le.contentEquals(other.le)) return false
        } else if (other.le != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cla.toInt()
        result = 31 * result + ins
        result = 31 * result + p1
        result = 31 * result + p2
        result = 31 * result + (lc?.contentHashCode() ?: 0)
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + (le?.contentHashCode() ?: 0)
        return result
    }
}
