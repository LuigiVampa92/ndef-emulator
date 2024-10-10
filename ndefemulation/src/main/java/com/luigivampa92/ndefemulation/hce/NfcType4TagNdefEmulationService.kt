package com.luigivampa92.ndefemulation.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.luigivampa92.ndefemulation.DataUtil
import com.luigivampa92.ndefemulation.Logger
import com.luigivampa92.ndefemulation.NdefEmulation

internal class NfcType4TagNdefEmulationService : HostApduService() {

    private var ndefEmulation: NdefEmulation? = null
    private var ndefEmulator: ApduExecutor? = null

    override fun onDeactivated(reason: Int) {
        ndefEmulation = null
        ndefEmulator = null
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray? {
        try {
            if (commandApdu == null) {
                return ApduConstants.SW_ERROR_INPUT_DATA_ABSENT
            }
            if (ndefEmulation == null || ndefEmulator == null) {
                ndefEmulation = NdefEmulation(this)
                ndefEmulation?.currentEmulatedNdefData?.let {
                    ndefEmulator = NfcType4TagNdefEmulator(it)
                } ?: return ApduConstants.SW_ERROR_NO_DATA_PERSISTED
            }
            Logger.i("RX: " + DataUtil.toHexString(commandApdu))
            val response = ndefEmulator?.transmitApdu(commandApdu) ?: return ApduConstants.SW_ERROR_NO_DATA_PERSISTED
            Logger.i("TX: " + DataUtil.toHexString(response))
            return if (response.isNotEmpty()) response else ApduConstants.SW_ERROR_OUTPUT_DATA_ABSENT
        } catch (e: Exception) {
            return ApduConstants.SW_ERROR_GENERAL
        }
    }
}
