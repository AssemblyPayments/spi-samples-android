package com.assemblypayments.ramenpos.activities.main

import android.support.v7.app.AlertDialog
import com.assemblypayments.ramenpos.logic.RamenPos
import com.assemblypayments.spi.model.*
import kotlinx.android.synthetic.main.activity_main.*

class PrintStatusActions {
//    fun logMessage(message: String) {
//        //txtOutput.text = message + "\r\n" + txtOutput.text
//        print(message)
//    }
//
//    private fun printFlowInfo() {
//        formAction.txtAreaFlow.setText("")
//
//        when (RamenPos.spi?.currentFlow) {
//            SpiFlow.PAIRING -> {
//                val pairingState = RamenPos.spi?.getCurrentPairingFlowState()
//                RamenPos.transactionsActivity.lblFlowMessage.setText(pairingState.getMessage())
//                formAction.txtAreaFlow.append("### PAIRING PROCESS UPDATE ###" + "\n")
//                formAction.txtAreaFlow.append("# " + pairingState.getMessage() + "\n")
//                formAction.txtAreaFlow.append("# Finished? " + pairingState.isFinished() + "\n")
//                formAction.txtAreaFlow.append("# Successful? " + pairingState.isSuccessful() + "\n")
//                formAction.txtAreaFlow.append("# Confirmation code: " + pairingState.getConfirmationCode() + "\n")
//                formAction.txtAreaFlow.append("# Waiting confirm from EFTPOS? " + pairingState.isAwaitingCheckFromEftpos() + "\n")
//                formAction.txtAreaFlow.append("# Waiting confirm from POS? " + pairingState.isAwaitingCheckFromPos() + "\n")
//            }
//
//            TRANSACTION -> {
//                val txState = spi.getCurrentTxFlowState()
//                formAction.lblFlowMessage.setText(txState.getDisplayMessage())
//                formAction.txtAreaFlow.append("### TX PROCESS UPDATE ###" + "\n")
//                formAction.txtAreaFlow.append("# " + txState.getDisplayMessage() + "\n")
//                formAction.txtAreaFlow.append("# Id: " + txState.getPosRefId() + "\n")
//                formAction.txtAreaFlow.append("# Type: " + txState.getType() + "\n")
//                formAction.txtAreaFlow.append("# Amount: " + txState.getAmountCents() / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# Waiting for signature: " + txState.isAwaitingSignatureCheck() + "\n")
//                formAction.txtAreaFlow.append("# Attempting to cancel: " + txState.isAttemptingToCancel() + "\n")
//                formAction.txtAreaFlow.append("# Finished: " + txState.isFinished() + "\n")
//                formAction.txtAreaFlow.append("# Success: " + txState.getSuccess() + "\n")
//                formAction.txtAreaFlow.append("# GLT Response PosRefId: " + txState.getGltResponsePosRefId() + "\n")
//                formAction.txtAreaFlow.append("# Last GLT Response Request Id: " + txState.getLastGltRequestId() + "\n")
//
//                if (txState.isAwaitingSignatureCheck()) {
//                    // We need to print the receipt for the customer to sign.
//                    formAction.txtAreaFlow.append("# RECEIPT TO PRINT FOR SIGNATURE" + "\n")
//                    formTransactions.txtAreaReceipt.append(txState.getSignatureRequiredMessage().getMerchantReceipt().trim() + "\n")
//                }
//
//                if (txState.isAwaitingPhoneForAuth()) {
//                    formAction.txtAreaFlow.append("# PHONE FOR AUTH DETAILS:" + "\n")
//                    formAction.txtAreaFlow.append("# CALL: " + txState.getPhoneForAuthRequiredMessage().getPhoneNumber() + "\n")
//                    formAction.txtAreaFlow.append("# QUOTE: Merchant ID: " + txState.getPhoneForAuthRequiredMessage().getMerchantId() + "\n")
//                }
//
//                if (txState.isFinished()) {
//                    formAction.txtAreaFlow.setText("")
//                    when (txState.getType()) {
//                        PURCHASE -> handleFinishedPurchase(txState)
//                        REFUND -> handleFinishedRefund(txState)
//                        CASHOUT_ONLY -> handleFinishedCashout(txState)
//                        MOTO -> handleFinishedMoto(txState)
//                        SETTLE -> handleFinishedSettle(txState)
//                        SETTLEMENT_ENQUIRY -> handleFinishedSettlementEnquiry(txState)
//                        GET_LAST_TRANSACTION -> handleFinishedGetLastTransaction(txState)
//
//                        else -> formAction.txtAreaFlow.append("# CAN'T HANDLE TX TYPE: " + txState.getType() + "\n")
//                    }
//                }
//            }
//            IDLE -> {
//            }
//            else -> throw IllegalArgumentException()
//        }
//
//        formAction.txtAreaFlow.append("# --------------- STATUS ------------------" + "\n")
//        formAction.txtAreaFlow.append("# $posId <-> Eftpos: $eftposAddress #\n")
//        formAction.txtAreaFlow.append("# SPI STATUS: " + spi.getCurrentStatus() + "     FLOW:" + spi.getCurrentFlow() + " #" + "\n")
//        formAction.txtAreaFlow.append("# -----------------------------------------" + "\n")
//        formAction.txtAreaFlow.append("# POS: v" + getVersion() + " Spi: v" + getVersion() + "\n")
//    }
//
//    private fun handleFinishedPurchase(txState: TransactionFlowState) {
//        val purchaseResponse: PurchaseResponse
//        when (txState.success) {
//            SUCCESS -> {
//                formAction.txtAreaFlow.append("# WOOHOO - WE GOT PAID!" + "\n")
//                purchaseResponse = PurchaseResponse(txState.response)
//                formAction.txtAreaFlow.append("# Response: " + purchaseResponse.responseText + "\n")
//                formAction.txtAreaFlow.append("# RRN: " + purchaseResponse.rrn + "\n")
//                formAction.txtAreaFlow.append("# Scheme: " + purchaseResponse.schemeName + "\n")
//                formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                formTransactions.txtAreaReceipt.append(if (!purchaseResponse.wasCustomerReceiptPrinted()) purchaseResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//
//                formAction.txtAreaFlow.append("# PURCHASE: $" + purchaseResponse.purchaseAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# TIP: $" + purchaseResponse.tipAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# CASHOUT: $" + purchaseResponse.cashoutAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# BANKED NON-CASH AMOUNT: $" + purchaseResponse.bankNonCashAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# BANKED CASH AMOUNT: $" + purchaseResponse.bankCashAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# SURCHARGE AMOUNT: $" + purchaseResponse.surchargeAmount / 100.0 + "\n")
//            }
//            FAILED -> {
//                formAction.txtAreaFlow.append("# WE DID NOT GET PAID :(" + "\n")
//                if (txState.response != null) {
//                    formAction.txtAreaFlow.append("# Error: " + txState.response.error + "\n")
//                    formAction.txtAreaFlow.append("# Error Detail: " + txState.response.errorDetail + "\n")
//                    purchaseResponse = PurchaseResponse(txState.response)
//                    formAction.txtAreaFlow.append("# Response: " + purchaseResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# RRN: " + purchaseResponse.rrn + "\n")
//                    formAction.txtAreaFlow.append("# Scheme: " + purchaseResponse.schemeName + "\n")
//                    formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                    formTransactions.txtAreaReceipt.append(if (!purchaseResponse.wasCustomerReceiptPrinted())
//                        purchaseResponse.customerReceipt.trim()
//                    else
//                        "# PRINTED FROM EFTPOS" + "\n")
//                }
//            }
//            UNKNOWN -> {
//                formAction.txtAreaFlow.append("# WE'RE NOT QUITE SURE WHETHER WE GOT PAID OR NOT :/" + "\n")
//                formAction.txtAreaFlow.append("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM." + "\n")
//                formAction.txtAreaFlow.append("# IF YOU CONFIRM THAT THE CUSTOMER PAID, CLOSE THE ORDER." + "\n")
//                formAction.txtAreaFlow.append("# OTHERWISE, RETRY THE PAYMENT FROM SCRATCH." + "\n")
//            }
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    private fun handleFinishedRefund(txState: TransactionFlowState) {
//        val refundResponse: RefundResponse
//        when (txState.success) {
//            SUCCESS -> {
//                formAction.txtAreaFlow.append("# REFUND GIVEN- OH WELL!" + "\n")
//                refundResponse = RefundResponse(txState.response)
//                formAction.txtAreaFlow.append("# Response: " + refundResponse.responseText + "\n")
//                formAction.txtAreaFlow.append("# RRN: " + refundResponse.rrn + "\n")
//                formAction.txtAreaFlow.append("# Scheme: " + refundResponse.schemeName + "\n")
//                formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                formTransactions.txtAreaReceipt.append(if (!refundResponse.wasCustomerReceiptPrinted()) refundResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                formAction.txtAreaFlow.append("# REFUNDED AMOUNT: $" + refundResponse.refundAmount / 100.0 + "\n")
//            }
//            FAILED -> {
//                formAction.txtAreaFlow.append("# REFUND FAILED!" + "\n")
//                if (txState.response != null) {
//                    refundResponse = RefundResponse(txState.response)
//                    formAction.txtAreaFlow.append("# Error: " + txState.response.error + "\n")
//                    formAction.txtAreaFlow.append("# Error Detail: " + txState.response.errorDetail + "\n")
//                    formAction.txtAreaFlow.append("# Response: " + refundResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# RRN: " + refundResponse.rrn + "\n")
//                    formAction.txtAreaFlow.append("# Scheme: " + refundResponse.schemeName + "\n")
//                    formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                    formTransactions.txtAreaReceipt.append(if (!refundResponse.wasCustomerReceiptPrinted()) refundResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                }
//            }
//            UNKNOWN -> {
//                formAction.txtAreaFlow.append("# WE'RE NOT QUITE SURE WHETHER THE REFUND WENT THROUGH OR NOT :/" + "\n")
//                formAction.txtAreaFlow.append("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM." + "\n")
//                formAction.txtAreaFlow.append("# YOU CAN THE TAKE THE APPROPRIATE ACTION." + "\n")
//            }
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    private fun handleFinishedCashout(txState: TransactionFlowState) {
//        val cashoutResponse: CashoutOnlyResponse
//        when (txState.success) {
//            SUCCESS -> {
//                formAction.txtAreaFlow.append("# CASH-OUT SUCCESSFUL - HAND THEM THE CASH!" + "\n")
//                cashoutResponse = CashoutOnlyResponse(txState.response)
//                formAction.txtAreaFlow.append("# Response: " + cashoutResponse.responseText + "\n")
//                formAction.txtAreaFlow.append("# RRN: " + cashoutResponse.rrn + "\n")
//                formAction.txtAreaFlow.append("# Scheme: " + cashoutResponse.schemeName + "\n")
//                formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                formTransactions.txtAreaReceipt.append(if (!cashoutResponse.wasCustomerReceiptPrinted()) cashoutResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                formAction.txtAreaFlow.append("# CASHOUT: $" + cashoutResponse.cashoutAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# BANKED NON-CASH AMOUNT: $" + cashoutResponse.bankNonCashAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# BANKED CASH AMOUNT: $" + cashoutResponse.bankCashAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# SURCHARGE AMOUNT: $" + cashoutResponse.surchargeAmount / 100.0 + "\n")
//            }
//            FAILED -> {
//                formAction.txtAreaFlow.append("# CASHOUT FAILED!" + "\n")
//                if (txState.response != null) {
//                    formAction.txtAreaFlow.append("# Error: " + txState.response.error + "\n")
//                    formAction.txtAreaFlow.append("# Error detail: " + txState.response.errorDetail + "\n")
//                    cashoutResponse = CashoutOnlyResponse(txState.response)
//                    formAction.txtAreaFlow.append("# Response: " + cashoutResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# RRN: " + cashoutResponse.rrn + "\n")
//                    formAction.txtAreaFlow.append("# Scheme: " + cashoutResponse.schemeName + "\n")
//                    formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                    formTransactions.txtAreaReceipt.append(if (!cashoutResponse.wasCustomerReceiptPrinted()) cashoutResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                }
//            }
//            UNKNOWN -> {
//                formAction.txtAreaFlow.append("# WE'RE NOT QUITE SURE WHETHER THE CASHOUT WENT THROUGH OR NOT :/" + "\n")
//                formAction.txtAreaFlow.append("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM." + "\n")
//                formAction.txtAreaFlow.append("# YOU CAN THE TAKE THE APPROPRIATE ACTION." + "\n")
//            }
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    private fun handleFinishedMoto(txState: TransactionFlowState) {
//        val motoResponse: MotoPurchaseResponse
//        val purchaseResponse: PurchaseResponse
//        when (txState.success) {
//            SUCCESS -> {
//                formAction.txtAreaFlow.append("# WOOHOO - WE GOT MOTO-PAID!" + "\n")
//                motoResponse = MotoPurchaseResponse(txState.response)
//                purchaseResponse = motoResponse.purchaseResponse
//                formAction.txtAreaFlow.append("# Response: " + purchaseResponse.responseText + "\n")
//                formAction.txtAreaFlow.append("# RRN: " + purchaseResponse.rrn + "\n")
//                formAction.txtAreaFlow.append("# Scheme: " + purchaseResponse.schemeName + "\n")
//                formAction.txtAreaFlow.append("# Card entry: " + purchaseResponse.cardEntry + "\n")
//                formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                formTransactions.txtAreaReceipt.append(if (!purchaseResponse.wasCustomerReceiptPrinted()) purchaseResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                formAction.txtAreaFlow.append("# PURCHASE: $" + purchaseResponse.purchaseAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# BANKED NON-CASH AMOUNT: $" + purchaseResponse.bankNonCashAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# BANKED CASH AMOUNT: $" + purchaseResponse.bankCashAmount / 100.0 + "\n")
//                formAction.txtAreaFlow.append("# BANKED SURCHARGE AMOUNT: $" + purchaseResponse.surchargeAmount / 100.0 + "\n")
//            }
//            FAILED -> {
//                formAction.txtAreaFlow.append("# WE DID NOT GET MOTO-PAID :(" + "\n")
//                if (txState.response != null) {
//                    formAction.txtAreaFlow.append("# Error: " + txState.response.error + "\n")
//                    formAction.txtAreaFlow.append("# Error detail: " + txState.response.errorDetail + "\n")
//                    motoResponse = MotoPurchaseResponse(txState.response)
//                    purchaseResponse = motoResponse.purchaseResponse
//                    formAction.txtAreaFlow.append("# Response: " + purchaseResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# RRN: " + purchaseResponse.rrn + "\n")
//                    formAction.txtAreaFlow.append("# Scheme: " + purchaseResponse.schemeName + "\n")
//                    formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//                    formTransactions.txtAreaReceipt.append(if (!purchaseResponse.wasCustomerReceiptPrinted()) purchaseResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                }
//            }
//            UNKNOWN -> {
//                formAction.txtAreaFlow.append("# WE'RE NOT QUITE SURE WHETHER THE MOTO WENT THROUGH OR NOT :/" + "\n")
//                formAction.txtAreaFlow.append("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM." + "\n")
//                formAction.txtAreaFlow.append("# YOU CAN THE TAKE THE APPROPRIATE ACTION." + "\n")
//            }
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    private fun handleFinishedGetLastTransaction(txState: TransactionFlowState) {
//        if (txState.response != null) {
//            val gltResponse = GetLastTransactionResponse(txState.response)
//
//            // User specified that he intended to retrieve a specific tx by pos_ref_id
//            // This is how you can use a handy function to match it.
//            val success = spi.gltMatch(gltResponse, formAction.txtAction1.getText().trim())
//            if (success === Message.SuccessState.UNKNOWN) {
//                formAction.txtAreaFlow.append("# Did not retrieve expected transaction. Here is what we got:" + "\n")
//            } else {
//                formAction.txtAreaFlow.append("# Tx matched expected purchase request." + "\n")
//            }
//
//            val purchaseResponse = PurchaseResponse(txState.response)
//            formAction.txtAreaFlow.append("# Scheme: " + purchaseResponse.schemeName + "\n")
//            formAction.txtAreaFlow.append("# Response: " + purchaseResponse.responseText + "\n")
//            formAction.txtAreaFlow.append("# RRN: " + purchaseResponse.rrn + "\n")
//            formAction.txtAreaFlow.append("# Error: " + txState.response.error + "\n")
//            formAction.txtAreaFlow.append("# Customer receipt:" + "\n")
//            formTransactions.txtAreaReceipt.append(purchaseResponse.customerReceipt.trim() + "\n")
//        } else {
//            // We did not even get a response, like in the case of a time-out.
//            formAction.txtAreaFlow.append("# Could not retrieve last transaction." + "\n")
//        }
//    }
//
//    private fun handleFinishedSettle(txState: TransactionFlowState) {
//        when (txState.success) {
//            SUCCESS -> {
//                formAction.txtAreaFlow.append("# SETTLEMENT SUCCESSFUL!")
//                if (txState.response != null) {
//                    val settleResponse = Settlement(txState.response)
//                    formAction.txtAreaFlow.append("# Response: " + settleResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# Merchant receipt:" + "\n")
//                    formAction.txtAreaFlow.append(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                    formAction.txtAreaFlow.append("# Period start: " + settleResponse.periodStartTime + "\n")
//                    formAction.txtAreaFlow.append("# Period end: " + settleResponse.periodEndTime + "\n")
//                    formAction.txtAreaFlow.append("# Settlement time: " + settleResponse.triggeredTime + "\n")
//                    formAction.txtAreaFlow.append("# Transaction range: " + settleResponse.transactionRange + "\n")
//                    formAction.txtAreaFlow.append("# Terminal ID: " + settleResponse.terminalId + "\n")
//                    formAction.txtAreaFlow.append("# Total TX count: " + settleResponse.totalCount + "\n")
//                    formAction.txtAreaFlow.append("# Total TX value: $" + settleResponse.totalValue / 100.0 + "\n")
//                    formAction.txtAreaFlow.append("# By acquirer TX count: " + settleResponse.settleByAcquirerCount + "\n")
//                    formAction.txtAreaFlow.append("# By acquirer TX value: " + settleResponse.settleByAcquirerValue / 100.0 + "\n")
//                    formAction.txtAreaFlow.append("# SCHEME SETTLEMENTS:" + "\n")
//                    val schemes = settleResponse.schemeSettlementEntries
//                    for (s in schemes) {
//                        formTransactions.txtAreaReceipt.append("# $s\n")
//                    }
//                }
//            }
//            FAILED -> {
//                formAction.txtAreaFlow.append("# SETTLEMENT FAILED!" + "\n")
//                if (txState.response != null) {
//                    val settleResponse = Settlement(txState.response)
//                    formAction.txtAreaFlow.append("# Response: " + settleResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# Error: " + txState.response.error + "\n")
//                    formAction.txtAreaFlow.append("# Merchant receipt:" + "\n")
//                    formTransactions.txtAreaReceipt.append(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                }
//            }
//            UNKNOWN -> formAction.txtAreaFlow.append("# SETTLEMENT ENQUIRY RESULT UNKNOWN!" + "\n")
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    private fun handleFinishedSettlementEnquiry(txState: TransactionFlowState) {
//        when (txState.success) {
//            SUCCESS -> {
//                formAction.txtAreaFlow.append("# SETTLEMENT ENQUIRY SUCCESSFUL!" + "\n")
//                if (txState.response != null) {
//                    val settleResponse = Settlement(txState.response)
//                    formAction.txtAreaFlow.append("# Response: " + settleResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# Merchant receipt:" + "\n")
//                    formAction.txtAreaFlow.append(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                    formAction.txtAreaFlow.append("# Period start: " + settleResponse.periodStartTime + "\n")
//                    formAction.txtAreaFlow.append("# Period end: " + settleResponse.periodEndTime + "\n")
//                    formAction.txtAreaFlow.append("# Settlement time: " + settleResponse.triggeredTime + "\n")
//                    formAction.txtAreaFlow.append("# Transaction range: " + settleResponse.transactionRange + "\n")
//                    formAction.txtAreaFlow.append("# Terminal ID: " + settleResponse.terminalId + "\n")
//                    formAction.txtAreaFlow.append("# Total TX count: " + settleResponse.totalCount + "\n")
//                    formAction.txtAreaFlow.append("# Total TX value: " + settleResponse.totalValue / 100.0 + "\n")
//                    formAction.txtAreaFlow.append("# By acquirer TX count: " + settleResponse.settleByAcquirerCount + "\n")
//                    formAction.txtAreaFlow.append("# By acquirer TX value: " + settleResponse.settleByAcquirerValue / 100.0 + "\n")
//                    formAction.txtAreaFlow.append("# SCHEME SETTLEMENTS:" + "\n")
//                    val schemes = settleResponse.schemeSettlementEntries
//                    for (s in schemes) {
//                        formTransactions.txtAreaReceipt.append("# $s\n")
//                    }
//                }
//            }
//            FAILED -> {
//                formAction.txtAreaFlow.append("# SETTLEMENT ENQUIRY FAILED!" + "\n")
//                if (txState.response != null) {
//                    val settleResponse = Settlement(txState.response)
//                    formAction.txtAreaFlow.append("# Response: " + settleResponse.responseText + "\n")
//                    formAction.txtAreaFlow.append("# Error: " + txState.response.error + "\n")
//                    formAction.txtAreaFlow.append("# Merchant receipt:" + "\n")
//                    formTransactions.txtAreaReceipt.append(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS" + "\n")
//                }
//            }
//            UNKNOWN -> formAction.txtAreaFlow.append("# SETTLEMENT ENQUIRY RESULT UNKNOWN!" + "\n")
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    private fun printPairingStatus() {
//        lblPairingStatus.setText(spi.getCurrentStatus().toString())
//    }

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

