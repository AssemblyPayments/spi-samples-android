package com.assemblypayments.ramenpos.activities.main

import com.assemblypayments.ramenpos.logic.RamenPos
import com.assemblypayments.spi.model.SpiStatus
import kotlinx.android.synthetic.main.activity_main.*

class TransactionFlowChange {
    fun stateChanged() {
        updateUIFlowInfo()
    }

    private fun updateUIFlowInfo() {
        RamenPos.mainActivity.btnConnection.text = "Pair"
        when (RamenPos.spi?.currentStatus) {
            SpiStatus.PAIRED_CONNECTED -> {
                RamenPos.mainActivity.txtStatus.text = "Connected"
                RamenPos.mainActivity.lblTopStatus.text = "Connected"
                RamenPos.mainActivity.btnConnection.text = "Connection"
            }
            SpiStatus.PAIRED_CONNECTING -> RamenPos.mainActivity.txtStatus.text = "Connecting"
            SpiStatus.UNPAIRED -> {
                RamenPos.mainActivity.txtStatus.text = "Not Connected"
                RamenPos.mainActivity.lblTopStatus.text = "Not Connected"
            }
            null -> TODO()
        }

        RamenPos.mainActivity.txtPosId.text = RamenPos.settings?.posId
        RamenPos.mainActivity.txtAddress.text = RamenPos.settings?.eftposAddress
        RamenPos.mainActivity.txtFlow.text = RamenPos.spi?.currentFlow?.name
    }
}