package com.assemblypayments.ramenpos.activities.main

import android.support.v7.app.AlertDialog
import com.assemblypayments.ramenpos.logic.RamenPos
import com.assemblypayments.spi.model.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.txtOutput
import kotlinx.android.synthetic.main.activity_transactions.*


class PrintStatusActions {
    private fun logMessage(message: String) {
        RamenPos.mainActivity.txtOutput.text = message + "\r\n" + RamenPos.mainActivity.txtOutput.text
        RamenPos.transactionsActivity.txtOutput.text = message + "\r\n" + RamenPos.mainActivity.txtOutput.text
        print(message)
    }

    fun printFlowInfo() {
        when (RamenPos.spi?.currentFlow) {
            SpiFlow.PAIRING -> {
                val pairingState = RamenPos.spi?.currentPairingFlowState
                logMessage(pairingState?.message!!)
                logMessage("### PAIRING PROCESS UPDATE ###")
                logMessage("# " + pairingState?.message)
                logMessage("# Finished? " + pairingState?.isFinished)
                logMessage("# Successful? " + pairingState?.isSuccessful)
                logMessage("# Confirmation code: " + pairingState?.confirmationCode)
                logMessage("# Waiting confirm from EFTPOS? " + pairingState?.isAwaitingCheckFromEftpos)
                logMessage("# Waiting confirm from POS? " + pairingState?.isAwaitingCheckFromPos)
            }

            SpiFlow.TRANSACTION -> {
                val txState = RamenPos.spi?.currentTxFlowState
                logMessage(txState!!.displayMessage)
                logMessage("### TX PROCESS UPDATE ###")
                logMessage("# " + txState.displayMessage)
                logMessage("# Id: " + txState.posRefId)
                logMessage("# Type: " + txState.type)
                logMessage("# Amount: " + txState.amountCents / 100.0)
                logMessage("# Waiting for signature: " + txState.isAwaitingSignatureCheck)
                logMessage("# Attempting to cancel: " + txState.isAttemptingToCancel)
                logMessage("# Finished: " + txState.isFinished)
                logMessage("# Success: " + txState.success)
                logMessage("# GLT Response PosRefId: " + txState.gltResponsePosRefId)
                logMessage("# Last GLT Response Request Id: " + txState.lastGltRequestId)

                if (txState.isAwaitingSignatureCheck) {
                    // We need to print the receipt for the customer to sign.
                    logMessage("# RECEIPT TO PRINT FOR SIGNATURE")
                    logMessage(txState.signatureRequiredMessage.merchantReceipt.trim())
                }

                if (txState.isAwaitingPhoneForAuth) {
                    logMessage("# PHONE FOR AUTH DETAILS:")
                    logMessage("# CALL: " + txState.phoneForAuthRequiredMessage.phoneNumber)
                    logMessage("# QUOTE: Merchant ID: " + txState.phoneForAuthRequiredMessage.merchantId)
                }

                if (txState.isFinished) {
                    when (txState.type) {
                        TransactionType.PURCHASE -> handleFinishedPurchase(txState)
                        TransactionType.REFUND -> handleFinishedRefund(txState)
                        TransactionType.CASHOUT_ONLY -> handleFinishedCashout(txState)
                        TransactionType.MOTO -> handleFinishedMoto(txState)
                        TransactionType.SETTLE -> handleFinishedSettle(txState)
                        TransactionType.SETTLEMENT_ENQUIRY -> handleFinishedSettlementEnquiry(txState)
                        TransactionType.GET_LAST_TRANSACTION -> handleFinishedGetLastTransaction(txState)

                        else -> logMessage("# CAN'T HANDLE TX TYPE: " + txState.type)
                    }
                }
            }
            SpiFlow.IDLE -> {
            }
        }
    }

    private fun handleFinishedPurchase(txState: TransactionFlowState) {
        val purchaseResponse: PurchaseResponse
        when (txState.success) {
            Message.SuccessState.SUCCESS -> {
                logMessage("# WOOHOO - WE GOT PAID!")
                purchaseResponse = PurchaseResponse(txState.response)
                logMessage("# Response: " + purchaseResponse.responseText)
                logMessage("# RRN: " + purchaseResponse.rrn)
                logMessage("# Scheme: " + purchaseResponse.schemeName)
                logMessage("# Customer receipt:")
                logMessage(if (!purchaseResponse.wasCustomerReceiptPrinted()) purchaseResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS")
                logMessage("# PURCHASE: $" + purchaseResponse.purchaseAmount / 100.0)
                logMessage("# TIP: $" + purchaseResponse.tipAmount / 100.0)
                logMessage("# CASHOUT: $" + purchaseResponse.cashoutAmount / 100.0)
                logMessage("# BANKED NON-CASH AMOUNT: $" + purchaseResponse.bankNonCashAmount / 100.0)
                logMessage("# BANKED CASH AMOUNT: $" + purchaseResponse.bankCashAmount / 100.0)
                logMessage("# SURCHARGE AMOUNT: $" + purchaseResponse.surchargeAmount / 100.0)
            }
            Message.SuccessState.FAILED -> {
                logMessage("# WE DID NOT GET PAID :(")
                if (txState.response != null) {
                    logMessage("# Error: " + txState.response.error)
                    logMessage("# Error Detail: " + txState.response.errorDetail)
                    purchaseResponse = PurchaseResponse(txState.response)
                    logMessage("# Response: " + purchaseResponse.responseText)
                    logMessage("# RRN: " + purchaseResponse.rrn)
                    logMessage("# Scheme: " + purchaseResponse.schemeName)
                    logMessage("# Customer receipt:")
                    logMessage(if (!purchaseResponse.wasCustomerReceiptPrinted())
                        purchaseResponse.customerReceipt.trim()
                    else
                        "# PRINTED FROM EFTPOS")
                }
            }
            Message.SuccessState.UNKNOWN -> {
                logMessage("# WE'RE NOT QUITE SURE WHETHER WE GOT PAID OR NOT :/")
                logMessage("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.")
                logMessage("# IF YOU CONFIRM THAT THE CUSTOMER PAID, CLOSE THE ORDER.")
                logMessage("# OTHERWISE, RETRY THE PAYMENT FROM SCRATCH.")
            }
        }
    }

    private fun handleFinishedRefund(txState: TransactionFlowState) {
        val refundResponse: RefundResponse
        when (txState.success) {
            Message.SuccessState.SUCCESS -> {
                logMessage("# REFUND GIVEN- OH WELL!")
                refundResponse = RefundResponse(txState.response)
                logMessage("# Response: " + refundResponse.responseText)
                logMessage("# RRN: " + refundResponse.rrn)
                logMessage("# Scheme: " + refundResponse.schemeName)
                logMessage("# Customer receipt:")
                logMessage(if (!refundResponse.wasCustomerReceiptPrinted()) refundResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS")
                logMessage("# REFUNDED AMOUNT: $" + refundResponse.refundAmount / 100.0)
            }
            Message.SuccessState.FAILED -> {
                logMessage("# REFUND FAILED!")
                if (txState.response != null) {
                    refundResponse = RefundResponse(txState.response)
                    logMessage("# Error: " + txState.response.error)
                    logMessage("# Error Detail: " + txState.response.errorDetail)
                    logMessage("# Response: " + refundResponse.responseText)
                    logMessage("# RRN: " + refundResponse.rrn)
                    logMessage("# Scheme: " + refundResponse.schemeName)
                    logMessage("# Customer receipt:")
                    logMessage(if (!refundResponse.wasCustomerReceiptPrinted()) refundResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS")
                }
            }
            Message.SuccessState.UNKNOWN -> {
                logMessage("# WE'RE NOT QUITE SURE WHETHER THE REFUND WENT THROUGH OR NOT :/")
                logMessage("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.")
                logMessage("# YOU CAN THE TAKE THE APPROPRIATE ACTION.")
            }
            else -> throw IllegalArgumentException()
        }
    }

    private fun handleFinishedCashout(txState: TransactionFlowState) {
        val cashoutResponse: CashoutOnlyResponse
        when (txState.success) {
            Message.SuccessState.SUCCESS -> {
                logMessage("# CASH-OUT SUCCESSFUL - HAND THEM THE CASH!")
                cashoutResponse = CashoutOnlyResponse(txState.response)
                logMessage("# Response: " + cashoutResponse.responseText)
                logMessage("# RRN: " + cashoutResponse.rrn)
                logMessage("# Scheme: " + cashoutResponse.schemeName)
                logMessage("# Customer receipt:")
                logMessage(if (!cashoutResponse.wasCustomerReceiptPrinted()) cashoutResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS")
                logMessage("# CASHOUT: $" + cashoutResponse.cashoutAmount / 100.0)
                logMessage("# BANKED NON-CASH AMOUNT: $" + cashoutResponse.bankNonCashAmount / 100.0)
                logMessage("# BANKED CASH AMOUNT: $" + cashoutResponse.bankCashAmount / 100.0)
                logMessage("# SURCHARGE AMOUNT: $" + cashoutResponse.surchargeAmount / 100.0)
            }
            Message.SuccessState.FAILED -> {
                logMessage("# CASHOUT FAILED!")
                if (txState.response != null) {
                    logMessage("# Error: " + txState.response.error)
                    logMessage("# Error detail: " + txState.response.errorDetail)
                    cashoutResponse = CashoutOnlyResponse(txState.response)
                    logMessage("# Response: " + cashoutResponse.responseText)
                    logMessage("# RRN: " + cashoutResponse.rrn)
                    logMessage("# Scheme: " + cashoutResponse.schemeName)
                    logMessage("# Customer receipt:")
                    logMessage(if (!cashoutResponse.wasCustomerReceiptPrinted()) cashoutResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS")
                }
            }
            Message.SuccessState.UNKNOWN -> {
                logMessage("# WE'RE NOT QUITE SURE WHETHER THE CASHOUT WENT THROUGH OR NOT :/")
                logMessage("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.")
                logMessage("# YOU CAN THE TAKE THE APPROPRIATE ACTION.")
            }
            else -> throw IllegalArgumentException()
        }
    }

    private fun handleFinishedMoto(txState: TransactionFlowState) {
        val motoResponse: MotoPurchaseResponse
        val purchaseResponse: PurchaseResponse
        when (txState.success) {
            Message.SuccessState.SUCCESS -> {
                logMessage("# WOOHOO - WE GOT MOTO-PAID!")
                motoResponse = MotoPurchaseResponse(txState.response)
                purchaseResponse = motoResponse.purchaseResponse
                logMessage("# Response: " + purchaseResponse.responseText)
                logMessage("# RRN: " + purchaseResponse.rrn)
                logMessage("# Scheme: " + purchaseResponse.schemeName)
                logMessage("# Card entry: " + purchaseResponse.cardEntry)
                logMessage("# Customer receipt:")
                logMessage(if (!purchaseResponse.wasCustomerReceiptPrinted()) purchaseResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS")
                logMessage("# PURCHASE: $" + purchaseResponse.purchaseAmount / 100.0)
                logMessage("# BANKED NON-CASH AMOUNT: $" + purchaseResponse.bankNonCashAmount / 100.0)
                logMessage("# BANKED CASH AMOUNT: $" + purchaseResponse.bankCashAmount / 100.0)
                logMessage("# BANKED SURCHARGE AMOUNT: $" + purchaseResponse.surchargeAmount / 100.0)
            }
            Message.SuccessState.FAILED -> {
                logMessage("# WE DID NOT GET MOTO-PAID :(")
                if (txState.response != null) {
                    logMessage("# Error: " + txState.response.error)
                    logMessage("# Error detail: " + txState.response.errorDetail)
                    motoResponse = MotoPurchaseResponse(txState.response)
                    purchaseResponse = motoResponse.purchaseResponse
                    logMessage("# Response: " + purchaseResponse.responseText)
                    logMessage("# RRN: " + purchaseResponse.rrn)
                    logMessage("# Scheme: " + purchaseResponse.schemeName)
                    logMessage("# Customer receipt:")
                    logMessage(if (!purchaseResponse.wasCustomerReceiptPrinted()) purchaseResponse.customerReceipt.trim() else "# PRINTED FROM EFTPOS")
                }
            }
            Message.SuccessState.UNKNOWN -> {
                logMessage("# WE'RE NOT QUITE SURE WHETHER THE MOTO WENT THROUGH OR NOT :/")
                logMessage("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.")
                logMessage("# YOU CAN THE TAKE THE APPROPRIATE ACTION.")
            }
            else -> throw IllegalArgumentException()
        }
    }

    private fun handleFinishedGetLastTransaction(txState: TransactionFlowState) {
        if (txState.response != null) {
            val gltResponse = GetLastTransactionResponse(txState.response)

            // User specified that he intended to retrieve a specific tx by pos_ref_id
            // This is how you can use a handy function to match it.
            val success = RamenPos.spi?.gltMatch(gltResponse, RamenPos.transactionsActivity.txtReference.text.toString().trim())
            if (success === Message.SuccessState.UNKNOWN) {
                logMessage("# Did not retrieve expected transaction. Here is what we got:")
            } else {
                logMessage("# Tx matched expected purchase request.")
            }

            val purchaseResponse = PurchaseResponse(txState.response)
            logMessage("# Scheme: " + purchaseResponse.schemeName)
            logMessage("# Response: " + purchaseResponse.responseText)
            logMessage("# RRN: " + purchaseResponse.rrn)
            logMessage("# Error: " + txState.response.error)
            logMessage("# Customer receipt:")
            logMessage(purchaseResponse.customerReceipt.trim())
        } else {
            // We did not even get a response, like in the case of a time-out.
            logMessage("# Could not retrieve last transaction.")
        }
    }

    private fun handleFinishedSettle(txState: TransactionFlowState) {
        when (txState.success) {
            Message.SuccessState.SUCCESS -> {
                logMessage("# SETTLEMENT SUCCESSFUL!")
                if (txState.response != null) {
                    val settleResponse = Settlement(txState.response)
                    logMessage("# Response: " + settleResponse.responseText)
                    logMessage("# Merchant receipt:")
                    logMessage(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS")
                    logMessage("# Period start: " + settleResponse.periodStartTime)
                    logMessage("# Period end: " + settleResponse.periodEndTime)
                    logMessage("# Settlement time: " + settleResponse.triggeredTime)
                    logMessage("# Transaction range: " + settleResponse.transactionRange)
                    logMessage("# Terminal ID: " + settleResponse.terminalId)
                    logMessage("# Total TX count: " + settleResponse.totalCount)
                    logMessage("# Total TX value: $" + settleResponse.totalValue / 100.0)
                    logMessage("# By acquirer TX count: " + settleResponse.settleByAcquirerCount)
                    logMessage("# By acquirer TX value: " + settleResponse.settleByAcquirerValue / 100.0)
                    logMessage("# SCHEME SETTLEMENTS:")
                    val schemes = settleResponse.schemeSettlementEntries
                    for (s in schemes) {
                        logMessage("# $s\n")
                    }
                }
            }
            Message.SuccessState.FAILED -> {
                logMessage("# SETTLEMENT FAILED!")
                if (txState.response != null) {
                    val settleResponse = Settlement(txState.response)
                    logMessage("# Response: " + settleResponse.responseText)
                    logMessage("# Error: " + txState.response.error)
                    logMessage("# Merchant receipt:")
                    logMessage(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS")
                }
            }
            Message.SuccessState.UNKNOWN -> logMessage("# SETTLEMENT ENQUIRY RESULT UNKNOWN!")
        }
    }

    private fun handleFinishedSettlementEnquiry(txState: TransactionFlowState) {
        when (txState.success) {
            Message.SuccessState.SUCCESS -> {
                logMessage("# SETTLEMENT ENQUIRY SUCCESSFUL!")
                if (txState.response != null) {
                    val settleResponse = Settlement(txState.response)
                    logMessage("# Response: " + settleResponse.responseText)
                    logMessage("# Merchant receipt:")
                    logMessage(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS")
                    logMessage("# Period start: " + settleResponse.periodStartTime)
                    logMessage("# Period end: " + settleResponse.periodEndTime)
                    logMessage("# Settlement time: " + settleResponse.triggeredTime)
                    logMessage("# Transaction range: " + settleResponse.transactionRange)
                    logMessage("# Terminal ID: " + settleResponse.terminalId)
                    logMessage("# Total TX count: " + settleResponse.totalCount)
                    logMessage("# Total TX value: " + settleResponse.totalValue / 100.0)
                    logMessage("# By acquirer TX count: " + settleResponse.settleByAcquirerCount)
                    logMessage("# By acquirer TX value: " + settleResponse.settleByAcquirerValue / 100.0)
                    logMessage("# SCHEME SETTLEMENTS:")
                    val schemes = settleResponse.schemeSettlementEntries
                    for (s in schemes) {
                        logMessage("# $s\n")
                    }
                }
            }
            Message.SuccessState.FAILED -> {
                logMessage("# SETTLEMENT ENQUIRY FAILED!")
                if (txState.response != null) {
                    val settleResponse = Settlement(txState.response)
                    logMessage("# Response: " + settleResponse.responseText)
                    logMessage("# Error: " + txState.response.error)
                    logMessage("# Merchant receipt:")
                    logMessage(if (!settleResponse.wasMerchantReceiptPrinted()) settleResponse.merchantReceipt.trim() else "# PRINTED FROM EFTPOS")
                }
            }
            Message.SuccessState.UNKNOWN -> logMessage("# SETTLEMENT ENQUIRY RESULT UNKNOWN!")
        }
    }

    fun handlePrintingResponse(message: Message) {
        val printingResponse = PrintingResponse(message)

        if (printingResponse.isSuccess) {
            RamenPos.mainActivity.runOnUiThread {
                AlertDialog.Builder(RamenPos.mainActivity)
                        .setCancelable(false)
                        .setMessage("Printing Receipt successful")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Print Receipt")
                        .show()
            }
        } else {
            RamenPos.mainActivity.runOnUiThread {
                AlertDialog.Builder(RamenPos.mainActivity)
                        .setCancelable(false)
                        .setMessage("Printing Response:  Printing Receipt failed: reason = \" + printingResponse.errorReason + \", detail = \" + printingResponse.errorDetail")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Print Receipt")
                        .show()
            }
        }
    }

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
            RamenPos.mainActivity.txtBatteryLevel.text = "$batteryLevel%"

//            if (batteryLevel as Int >= 50) {
//                RamenPos.mainActivity.txtBatteryLevel.textColors =
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

    fun handleBatteryLevelChanged(message: Message) {
        val terminalBatteryResponse = TerminalBattery(message)
        RamenPos.mainActivity.txtBatteryLevel.text = terminalBatteryResponse.batteryLevel + "%"
    }
}


