package com.luigivampa92.ndefemulation.hce

import com.luigivampa92.ndefemulation.defineByteArrayOf

internal object ApduConstants {
    internal val SW_OK = defineByteArrayOf(0x90, 0x00)
    internal val SW_ERROR_GENERAL = defineByteArrayOf(0x69, 0x81)
    internal val SW_ERROR_NO_SUCH_DF = defineByteArrayOf(0x69, 0x82)
    internal val SW_ERROR_NO_DATA_PERSISTED = defineByteArrayOf(0x69, 0x83)
    internal val SW_ERROR_INPUT_DATA_ABSENT = defineByteArrayOf(0x69, 0xF1)
    internal val SW_ERROR_OUTPUT_DATA_ABSENT = defineByteArrayOf(0x69, 0xF2)
}