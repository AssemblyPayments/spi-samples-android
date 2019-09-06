package com.assemblypayments.ramenpos.activities.main

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.assemblypayments.ramenpos.logic.RamenPos
import com.assemblypayments.spi.model.SpiFlow
import com.assemblypayments.spi.model.SpiStatus
import kotlinx.android.synthetic.main.activity_main.*

class TransactionFlowChange(activity: AppCompatActivity) {
    private var dialogBuilder: AlertDialog.Builder? = null
    private var alertDialog: AlertDialog? = null

    init {
        dialogBuilder = AlertDialog.Builder(activity)
    }

    fun stateChanged() {
        updateUIFlowInfo()
        selectActions()
    }

    private fun updateUIFlowInfo() {
        RamenPos.mainActivity.runOnUiThread {
            RamenPos.mainActivity.btnConnection.text = "Pair"
            when (RamenPos.spi?.currentStatus) {
                SpiStatus.PAIRED_CONNECTED -> {
                    RamenPos.mainActivity.txtStatus.text = "Connected"
                    RamenPos.mainActivity.btnConnection.text = "Connection"
                }
                SpiStatus.PAIRED_CONNECTING -> RamenPos.mainActivity.txtStatus.text = "Connecting"
                SpiStatus.UNPAIRED -> {
                    RamenPos.mainActivity.txtStatus.text = "Not Connected"
                }
                null -> TODO()
            }

            RamenPos.mainActivity.txtPosId.text = RamenPos.settings?.posId
            RamenPos.mainActivity.txtAddress.text = RamenPos.settings?.eftposAddress
            RamenPos.mainActivity.txtFlow.text = RamenPos.spi?.currentFlow?.name
        }
    }

    fun selectActions() {
        when (RamenPos.spi?.currentFlow) {
            SpiFlow.IDLE -> clear()
            SpiFlow.PAIRING -> {
                if (RamenPos.spi?.currentPairingFlowState!!.isAwaitingCheckFromPos) {
                    RamenPos.spi?.pairingConfirmCode()
                } else if (!RamenPos.spi?.currentPairingFlowState!!.isFinished) {
                    pairCancel()
                } else if (RamenPos.spi?.currentPairingFlowState!!.isFinished) {
                    ok()
                }
            }
            SpiFlow.TRANSACTION -> {
                if (RamenPos.spi?.currentTxFlowState!!.isAwaitingSignatureCheck) {
                    txSignature()
                } else if (RamenPos.spi?.currentTxFlowState!!.isAwaitingPhoneForAuth) {
                    txAuthCode()
                } else if (!RamenPos.spi?.currentTxFlowState!!.isFinished && !RamenPos.spi?.currentTxFlowState!!.isAttemptingToCancel) {
                    txCancel()
                } else if (RamenPos.spi?.currentTxFlowState!!.isFinished) {
                    ok()
                }
            }
            else -> return
        }
    }

    private fun clear() {
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }

    private fun ok() {
        RamenPos.spi?.ackFlowEndedAndBackToIdle()
    }

    private fun pairCancel() {
        if (RamenPos.spi?.currentTxFlowState != null) {
            RamenPos.transactionsActivity.runOnUiThread {
                dialogBuilder?.setMessage(RamenPos.spi?.currentPairingFlowState!!.message)
                dialogBuilder?.setNegativeButton("Cancel") { _, _ -> RamenPos.spi?.pairingCancel() }
                dialogBuilder?.setTitle("Pairing")

                alertDialog = dialogBuilder!!.create()
                alertDialog?.setCancelable(false)
                alertDialog?.show()
            }
        }
    }

    private fun txCancel() {
        RamenPos.transactionsActivity.runOnUiThread {
            dialogBuilder?.setMessage(RamenPos.spi?.currentTxFlowState!!.displayMessage)
            dialogBuilder?.setNegativeButton("Cancel") { _, _ -> RamenPos.spi?.cancelTransaction() }
            dialogBuilder?.setTitle("Transaction")

            alertDialog = dialogBuilder!!.create()
            alertDialog?.setCancelable(false)
            alertDialog?.show()

        }
    }

    private fun txAuthCode() {
//        var txtAuthCode: UITextField?
//        let alertVC = UIAlertController (title: "Auth Code", message: "Submit Phone for Auth Code", preferredStyle: .alert)
//        _ = alertVC.addTextField {
//            (txt) in
//                    txtAuthCode = txt
//            txt.text = "Enter code"
//        }
//        let submitBtn = UIAlertAction (title: "Submit", style: .default) {
//            (_) in
//                    self.client.submitAuthCode(txtAuthCode?.text, completion: {
//                        (result) in
//                                self.logMessage(String(format: "Valid format: %@)", result?.isValidFormat ?? false))
//                        self.logMessage(String(format: "Message: %@", result?.message ?? "-"))
//                    })
//        }
//        alertVC.addAction(submitBtn)
//        showAlert(alertController: alertVC)
    }

    private fun txSignature() {
//        let alertVC = UIAlertController (title: "Signature", message: "Select Action", preferredStyle: .alert)
//        alertVC.addAction(UIAlertAction(title: "Accept Signature", style:.default, handler: {
//            _ in
//                    RamenApp.current.client.acceptSignature(true)
//        }))
//        alertVC.addAction(UIAlertAction(title: "Decline Signature", style:.default, handler: {
//            _ in
//                    RamenApp.current.client.acceptSignature(false)
//        }))
//        showAlert(alertController: alertVC)
    }
}