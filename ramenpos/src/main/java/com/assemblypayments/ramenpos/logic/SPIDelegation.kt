package com.assemblypayments.ramenpos.logic

import android.support.v7.app.AlertDialog
import com.assemblypayments.ramenpos.activities.main.PrintStatusActions
import com.assemblypayments.ramenpos.activities.main.TransactionFlowChange
import com.assemblypayments.spi.model.*

class SPIDelegation {
    private var transactionFlowChange = TransactionFlowChange()
    private var printStatusActions = PrintStatusActions()

    fun onDeviceAddressChanged(deviceAddressStatus: DeviceAddressStatus?) {
        RamenPos.connectionActivity.runOnUiThread {
            RamenPos.connectionActivity.deviceAddressStatusAndAction()
        }
    }

    fun onTxFlowStateChanged(txState: TransactionFlowState) {
        RamenPos.connectionActivity.runOnUiThread {
            RamenPos.connectionActivity.printStatusAndAction()
        }
    }

    fun onPairingFlowStateChanged(pairingFlowState: PairingFlowState) {
        RamenPos.connectionActivity.runOnUiThread {
            transactionFlowChange.stateChanged()
            RamenPos.connectionActivity.printStatusAndAction()
        }
    }

    fun onSecretsChanged(secrets: Secrets?) {
        if (secrets != null) {
            RamenPos.settings?.encriptionKey = secrets.encKey
            RamenPos.settings?.hmacKey = secrets.hmacKey
        } else {
            RamenPos.settings?.encriptionKey = null
            RamenPos.settings?.hmacKey = null

            RamenPos.connectionActivity.runOnUiThread {
                AlertDialog.Builder(RamenPos.connectionActivity)
                        .setCancelable(false)
                        .setTitle("Pairing")
                        .setMessage("Secrets have been dropped")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
            }
        }
    }

    fun onSpiStatusChanged(status: SpiStatus) {
        RamenPos.connectionActivity.runOnUiThread {
            transactionFlowChange.stateChanged()
            RamenPos.connectionActivity.printStatusAndAction()
        }
    }

    fun handlePrintingResponse(message: Message) {
//        formAction.txtAreaFlow.setText("")
//        val printingResponse = PrintingResponse(message)
//
//        if (printingResponse.isSuccess) {
//            formAction.lblFlowMessage.setText("# --> Printing Response: Printing Receipt successful")
//        } else {
//            formAction.lblFlowMessage.setText("# --> Printing Response:  Printing Receipt failed: reason = " + printingResponse.errorReason + ", detail = " + printingResponse.errorDetail)
//        }
//
//        spi.ackFlowEndedAndBackToIdle()
//        getOKActionComponents()
//        transactionsFrame.setEnabled(false)
//        actionDialog.setVisible(true)
//        actionDialog.pack()
//        transactionsFrame.pack()
    }

    fun handleTerminalStatusResponse(message: Message) {
        RamenPos.connectionActivity.runOnUiThread {
            printStatusActions.handleTerminalStatusResponse(message)
        }
    }

    fun handleTerminalConfigurationResponse(message: Message) {
        RamenPos.connectionActivity.runOnUiThread {
            printStatusActions.handleTerminalConfigurationResponse(message)
        }
    }

    fun handleBatteryLevelChanged(message: Message) {
//        if (!actionDialog.isVisible()) {
//            formAction.lblFlowMessage.setText("# --> Battery Level Changed Successful")
//            val terminalBattery = TerminalBattery(message)
//            formAction.txtAreaFlow.setText("")
//            formAction.txtAreaFlow.append("# Battery Level Changed #" + "\n")
//            formAction.txtAreaFlow.append("# Battery Level: " + terminalBattery.batteryLevel.replace("d", "") + "%" + "\n")
//
//            spi.ackFlowEndedAndBackToIdle()
//            transactionsFrame.setEnabled(false)
//            actionDialog.setVisible(true)
//            actionDialog.pack()
//            transactionsFrame.pack()
//        }
    }

}