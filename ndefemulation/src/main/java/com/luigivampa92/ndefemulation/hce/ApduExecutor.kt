package com.luigivampa92.ndefemulation.hce

internal interface ApduExecutor {
    fun transmitApdu(apdu: ByteArray): ByteArray
}