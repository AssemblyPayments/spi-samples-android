package com.assemblypayments.ramenpos.activities.main

import android.support.v7.app.AlertDialog
import com.assemblypayments.ramenpos.logic.RamenPos
import com.assemblypayments.spi.model.*
import kotlinx.android.synthetic.main.activity_main.*

class PrintStatusActions {
    fun handleTerminalConfigurationResponse(message: Message) {
        if (message.successState == Message.SuccessState.SUCCESS) {
            val terminalConfigResponse = TerminalConfigurationResponse(message)
            RamenPos.mainActivity.txtCommsSelected.text = terminalConfigResponse.commsSelected
            RamenPos.mainActivity.txtMerchantId.text = terminalConfigResponse.merchantId
            RamenPos.mainActivity.txtPAVersion.text = terminalConfigResponse.paVersion
            RamenPos.mainActivity.txtPaymentInterfaceVersion.text = terminalConfigResponse.paymentInterfaceVersion
            RamenPos.mainActivity.txtPluginVersion.text = terminalConfigResponse.pluginVersion
            RamenPos.mainActivity.txtSerialNumber.text = terminalConfigResponse.serialNumber
            RamenPos.mainActivity.txtTerminalId.text = terminalConfigResponse.terminalId
            RamenPos.mainActivity.txtTerminalModel.text = terminalConfigResponse.terminalModel

            RamenPos.mainActivity.runOnUiThread {
                AlertDialog.Builder(RamenPos.mainActivity)
                        .setCancelable(false)
                        .setMessage("Terminal Configuration retrieving successful")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Terminal Configuration")
                        .show()
            }
            return
        } else {
            RamenPos.mainActivity.runOnUiThread {
                AlertDialog.Builder(RamenPos.mainActivity)
                        .setCancelable(false)
                        .setMessage("ERROR: Terminal Configuration retrieving failed")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Terminal Configuration")
                        .show()
            }
        }
    }

    fun handleTerminalStatusResponse(message: Message) {
        if (message.successState == Message.SuccessState.SUCCESS) {
            val terminalStatusResponse = TerminalStatusResponse(message)
            RamenPos.mainActivity.txtCharging.text = terminalStatusResponse.isCharging.toString()
            RamenPos.mainActivity.txtTerminalStatus.text = terminalStatusResponse.status

            val batteryLevel = terminalStatusResponse.batteryLevel.replace("d", "")
            RamenPos.mainActivity.txtBatteryLevel.text = "$batteryLevel%";

//            if (batteryLevel as Int >= 50) {
//                RamenPos.mainActivity.txtBatteryLevel.setTextColor()
//            } else {
//                RamenPos.mainActivity.txtBatteryLevel.textColor = UIColor.red
//            }
            RamenPos.mainActivity.runOnUiThread {
                AlertDialog.Builder(RamenPos.mainActivity)
                        .setCancelable(false)
                        .setMessage("Terminal Status retrieving successful")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Terminal Status")
                        .show()
            }
            return
        } else {
            RamenPos.mainActivity.runOnUiThread {
                AlertDialog.Builder(RamenPos.mainActivity)
                        .setCancelable(false)
                        .setMessage("ERROR: Terminal Status retrieving failed")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Terminal Status")
                        .show()
            }
        }
    }
}

