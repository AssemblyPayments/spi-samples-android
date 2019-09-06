package com.assemblypayments.ramenpos.logic

import com.assemblypayments.ramenpos.logic.enums.AppEvent
import com.assemblypayments.ramenpos.logic.protocols.NotificationListener
import com.assemblypayments.spi.model.*

class SPIDelegation {

    fun onDeviceAddressChanged(deviceAddressStatus: DeviceAddressStatus?) {
        NotificationListener.postNotification(RamenPos.connectionActivity.applicationContext, AppEvent.DEVICE_ADDRESS_CHANGED)
    }

    fun onTxFlowStateChanged(txState: TransactionFlowState) {
        NotificationListener.postNotification(RamenPos.mainActivity.applicationContext, AppEvent.TRANSACTION_FLOW_STATE_CHANGED)
    }

    fun onPairingFlowStateChanged(pairingFlowState: PairingFlowState) {
        NotificationListener.postNotification(RamenPos.connectionActivity.applicationContext, AppEvent.PAIRING_FLOW_CHANGED)
    }

    fun onSecretsChanged(secrets: Secrets?) {
        if (secrets != null) {
            RamenPos.settings?.encriptionKey = secrets.encKey
            RamenPos.settings?.hmacKey = secrets.hmacKey
        } else {
            RamenPos.settings?.encriptionKey = null
            RamenPos.settings?.hmacKey = null

            NotificationListener.postNotification(RamenPos.connectionActivity.applicationContext, AppEvent.SECRET_DROPPED)
        }
    }

    fun onSpiStatusChanged(status: SpiStatus) {
        NotificationListener.postNotification(RamenPos.mainActivity.applicationContext, AppEvent.CONNNECTION_STATUS_CHANGED, status)
    }

    fun handlePrintingResponse(message: Message) {
        NotificationListener.postNotification(RamenPos.transactionsActivity.applicationContext, AppEvent.PRINTING_RESPONSE, message)
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
        NotificationListener.postNotification(RamenPos.mainActivity.applicationContext, AppEvent.TERMINAL_STATUS_RESPONSE, message)
    }

    fun handleTerminalConfigurationResponse(message: Message) {
        NotificationListener.postNotification(RamenPos.mainActivity.applicationContext, AppEvent.TERMINAL_CONFIGURATION_RESPONSE, message)
    }

    fun handleBatteryLevelChanged(message: Message) {
        NotificationListener.postNotification(RamenPos.mainActivity.applicationContext, AppEvent.BATTERY_LEVEL_CHANGED, message)
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