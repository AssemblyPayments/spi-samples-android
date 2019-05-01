package com.assemblypayments.kebabpos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import com.assemblypayments.spi.Spi;
import com.assemblypayments.spi.model.*;
import com.assemblypayments.spi.util.RequestIdHelper;
import com.assemblypayments.utils.SystemHelper;
import org.apache.commons.lang.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * NOTE: THIS PROJECT USES THE 2.1.x of the SPI Client Library
 * <p>
 * This is your POS. To integrate with SPI, you need to instantiate a Spi Object
 * and interact with it.
 * Primarily you need to implement 3 things.
 * 1. Settings Screen
 * 2. Pairing Flow Screen
 * 3. Transaction Flow screen
 */
@SuppressWarnings({"Duplicates", "unused"})
public class PosActivity extends ConsoleActivity {

    private static final String TAG = PosActivity.class.getSimpleName();

    private Spi spi;
    private String posId = "KEBABPOS1";
    private String eftposAddress = "emulator-prod.herokuapp.com";
    private Secrets spiSecrets = null;
    private TransactionOptions options;

    private String[] lastCmd = new String[0];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemHelper.consolePrintln("Starting KebabPos...");
        loadPersistedState(loadStateArgs(posId, eftposAddress));

        try {
            // This is how you instantiate SPI while checking for JDK compatibility.
            spi = new Spi(posId, eftposAddress, "123-456-789", spiSecrets); // It is ok to not have the secrets yet to start with. Please replace serial number
            spi.setPosInfo("assembly", "2.3.0");
            options = new TransactionOptions();
        } catch (Spi.CompatibilityException e) {
            Log.e(TAG, "Compatibility check failed", e);

            new AlertDialog.Builder(this)
                    .setTitle("Compatibility check failed")
                    .setMessage("Please ensure you followed all the configuration steps on your machine")
                    .setNegativeButton(R.string.btn_ok, null)
                    .show();

            finish();
        }
        spi.setStatusChangedHandler(new Spi.EventHandler<SpiStatus>() {
            @Override
            public void onEvent(SpiStatus value) {
                onSpiStatusChanged(value);
            }
        });
        spi.setPairingFlowStateChangedHandler(new Spi.EventHandler<PairingFlowState>() {
            @Override
            public void onEvent(PairingFlowState value) {
                onPairingFlowStateChanged(value);
            }
        });
        spi.setSecretsChangedHandler(new Spi.EventHandler<Secrets>() {
            @Override
            public void onEvent(Secrets value) {
                onSecretsChanged(value);
            }
        });
        spi.setTxFlowStateChangedHandler(new Spi.EventHandler<TransactionFlowState>() {
            @Override
            public void onEvent(TransactionFlowState value) {
                onTxFlowStateChanged(value);
            }
        });
        spi.start();

        SystemHelper.clearConsole();
        SystemHelper.consolePrintln("# Welcome to KebabPos !");
        printStatusAndActions();
    }

    @Override
    protected void onDestroy() {
        spi.dispose();

        super.onDestroy();
    }

    @Override
    public void setPosId(String posId) {
        this.posId = posId;
    }

    @Override
    public void setEftposAddress(String eftposAddress) {
        if (spi != null && spi.setEftposAddress(eftposAddress)) {
            this.eftposAddress = eftposAddress;
        }
    }

    private void onTxFlowStateChanged(TransactionFlowState txState) {
        SystemHelper.clearConsole();
        printStatusAndActions();
        SystemHelper.consolePrint("> ");
    }

    private void onPairingFlowStateChanged(PairingFlowState pairingFlowState) {
        SystemHelper.clearConsole();
        printStatusAndActions();
        SystemHelper.consolePrint("> ");
    }

    private void onSecretsChanged(Secrets secrets) {
        spiSecrets = secrets;
        if (secrets != null) {
            SystemHelper.consolePrintln("# I have secrets: " + secrets.getEncKey() + secrets.getHmacKey() + ". Persist them securely.");
        } else {
            SystemHelper.consolePrintln("# I have lost the secrets, i.e. unpaired. Destroy the persisted secrets.");
        }
    }

    private void onSpiStatusChanged(SpiStatus status) {
        SystemHelper.clearConsole();
        SystemHelper.consolePrintln("# --> SPI Status Changed: " + status);
        printStatusAndActions();
        SystemHelper.consolePrint("> ");
    }

    private void printStatusAndActions() {
        printFlowInfo();

        printActions();

        printPairingStatus();
    }

    private void printFlowInfo() {
        switch (spi.getCurrentFlow()) {
            case PAIRING:
                PairingFlowState pairingState = spi.getCurrentPairingFlowState();
                SystemHelper.consolePrintln("### PAIRING PROCESS UPDATE ###");
                SystemHelper.consolePrintln("# " + pairingState.getMessage());
                SystemHelper.consolePrintln("# Finished? " + pairingState.isFinished());
                SystemHelper.consolePrintln("# Successful? " + pairingState.isSuccessful());
                SystemHelper.consolePrintln("# Confirmation code: " + pairingState.getConfirmationCode());
                SystemHelper.consolePrintln("# Waiting confirm from EFTPOS? " + pairingState.isAwaitingCheckFromEftpos());
                SystemHelper.consolePrintln("# Waiting confirm from POS? " + pairingState.isAwaitingCheckFromPos());
                break;

            case TRANSACTION:
                TransactionFlowState txState = spi.getCurrentTxFlowState();
                SystemHelper.consolePrintln("### TX PROCESS UPDATE ###");
                SystemHelper.consolePrintln("# " + txState.getDisplayMessage());
                SystemHelper.consolePrintln("# Id: " + txState.getPosRefId());
                SystemHelper.consolePrintln("# Type: " + txState.getType());
                SystemHelper.consolePrintln("# Amount: " + (txState.getAmountCents() / 100.0));
                SystemHelper.consolePrintln("# Waiting for signature: " + txState.isAwaitingSignatureCheck());
                SystemHelper.consolePrintln("# Attempting to cancel: " + txState.isAttemptingToCancel());
                SystemHelper.consolePrintln("# Finished: " + txState.isFinished());
                SystemHelper.consolePrintln("# Success: " + txState.getSuccess());

                if (txState.isAwaitingSignatureCheck()) {
                    // We need to print the receipt for the customer to sign.
                    SystemHelper.consolePrintln("# RECEIPT TO PRINT FOR SIGNATURE");
                    SystemHelper.consolePrintln(txState.getSignatureRequiredMessage().getMerchantReceipt().trim());
                }

                if (txState.isAwaitingPhoneForAuth()) {
                    SystemHelper.consolePrintln("# PHONE FOR AUTH DETAILS:");
                    SystemHelper.consolePrintln("# CALL: " + txState.getPhoneForAuthRequiredMessage().getPhoneNumber());
                    SystemHelper.consolePrintln("# QUOTE: Merchant ID: " + txState.getPhoneForAuthRequiredMessage().getMerchantId());
                }

                if (txState.isFinished()) {
                    SystemHelper.consolePrintln();
                    switch (txState.getType()) {
                        case PURCHASE:
                            handleFinishedPurchase(txState);
                            break;
                        case REFUND:
                            handleFinishedRefund(txState);
                            break;
                        case CASHOUT_ONLY:
                            handleFinishedCashout(txState);
                            break;
                        case MOTO:
                            handleFinishedMoto(txState);
                            break;
                        case SETTLE:
                            handleFinishedSettle(txState);
                            break;
                        case SETTLEMENT_ENQUIRY:
                            handleFinishedSettlementEnquiry(txState);
                            break;

                        case GET_LAST_TRANSACTION:
                            handleFinishedGetLastTransaction(txState);
                            break;
                        default:
                            SystemHelper.consolePrintln("# CAN'T HANDLE TX TYPE: " + txState.getType());
                            break;
                    }
                }
                break;
            case IDLE:
                break;
        }

        SystemHelper.consolePrintln();
    }

    private void handleFinishedPurchase(TransactionFlowState txState) {
        PurchaseResponse purchaseResponse;
        switch (txState.getSuccess()) {
            case SUCCESS:
                SystemHelper.consolePrintln("# WOOHOO - WE GOT PAID!");
                purchaseResponse = new PurchaseResponse(txState.getResponse());
                SystemHelper.consolePrintln("# Response: " + purchaseResponse.getResponseText());
                SystemHelper.consolePrintln("# RRN: " + purchaseResponse.getRRN());
                SystemHelper.consolePrintln("# Scheme: " + purchaseResponse.getSchemeName());
                SystemHelper.consolePrintln("# Customer receipt:");
                SystemHelper.consolePrintln(!purchaseResponse.wasCustomerReceiptPrinted() ? purchaseResponse.getCustomerReceipt().trim() : "# PRINTED FROM EFTPOS");
                SystemHelper.consolePrintln("# PURCHASE: " + purchaseResponse.getPurchaseAmount());
                SystemHelper.consolePrintln("# TIP: " + purchaseResponse.getTipAmount());
                SystemHelper.consolePrintln("# CASHOUT: " + purchaseResponse.getCashoutAmount());
                SystemHelper.consolePrintln("# BANKED NON-CASH AMOUNT: " + purchaseResponse.getBankNonCashAmount());
                SystemHelper.consolePrintln("# BANKED CASH AMOUNT: " + purchaseResponse.getBankCashAmount());
                SystemHelper.consolePrintln("# BANKED SURCHARGE AMOUNT: " + purchaseResponse.getSurchargeAmount());
                break;
            case FAILED:
                SystemHelper.consolePrintln("# WE DID NOT GET PAID :(");
                SystemHelper.consolePrintln("# Error: " + txState.getResponse().getError());
                SystemHelper.consolePrintln("# Error Detail: " + txState.getResponse().getErrorDetail());
                if (txState.getResponse() != null) {
                    purchaseResponse = new PurchaseResponse(txState.getResponse());
                    SystemHelper.consolePrintln("# Response: " + purchaseResponse.getResponseText());
                    SystemHelper.consolePrintln("# RRN: " + purchaseResponse.getRRN());
                    SystemHelper.consolePrintln("# Scheme: " + purchaseResponse.getSchemeName());
                    SystemHelper.consolePrintln("# Customer receipt:");
                    SystemHelper.consolePrintln(!purchaseResponse.wasCustomerReceiptPrinted()
                            ? purchaseResponse.getCustomerReceipt().trim()
                            : "# PRINTED FROM EFTPOS");
                }
                break;
            case UNKNOWN:
                SystemHelper.consolePrintln("# WE'RE NOT QUITE SURE WHETHER WE GOT PAID OR NOT :/");
                SystemHelper.consolePrintln("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.");
                SystemHelper.consolePrintln("# IF YOU CONFIRM THAT THE CUSTOMER PAID, CLOSE THE ORDER.");
                SystemHelper.consolePrintln("# OTHERWISE, RETRY THE PAYMENT FROM SCRATCH.");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleFinishedRefund(TransactionFlowState txState) {
        RefundResponse refundResponse;
        switch (txState.getSuccess()) {
            case SUCCESS:
                SystemHelper.consolePrintln("# REFUND GIVEN- OH WELL!");
                refundResponse = new RefundResponse(txState.getResponse());
                SystemHelper.consolePrintln("# Response: " + refundResponse.getResponseText());
                SystemHelper.consolePrintln("# RRN: " + refundResponse.getRRN());
                SystemHelper.consolePrintln("# Scheme: " + refundResponse.getSchemeName());
                SystemHelper.consolePrintln("# Customer receipt:");
                SystemHelper.consolePrintln(!refundResponse.wasCustomerReceiptPrinted() ? refundResponse.getCustomerReceipt().trim() : "# PRINTED FROM EFTPOS");
                SystemHelper.consolePrintln("# REFUNDED AMOUNT: " + refundResponse.getRefundAmount());
                break;
            case FAILED:
                SystemHelper.consolePrintln("# REFUND FAILED!");
                SystemHelper.consolePrintln("# Error: " + txState.getResponse().getError());
                SystemHelper.consolePrintln("# Error Detail: " + txState.getResponse().getErrorDetail());
                if (txState.getResponse() != null) {
                    refundResponse = new RefundResponse(txState.getResponse());
                    SystemHelper.consolePrintln("# Response: " + refundResponse.getResponseText());
                    SystemHelper.consolePrintln("# RRN: " + refundResponse.getRRN());
                    SystemHelper.consolePrintln("# Scheme: " + refundResponse.getSchemeName());
                    SystemHelper.consolePrintln("# Customer receipt:");
                    SystemHelper.consolePrintln(!refundResponse.wasCustomerReceiptPrinted() ? refundResponse.getCustomerReceipt().trim() : "# PRINTED FROM EFTPOS");
                }
                break;
            case UNKNOWN:
                SystemHelper.consolePrintln("# WE'RE NOT QUITE SURE WHETHER THE REFUND WENT THROUGH OR NOT :/");
                SystemHelper.consolePrintln("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.");
                SystemHelper.consolePrintln("# YOU CAN THE TAKE THE APPROPRIATE ACTION.");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleFinishedCashout(TransactionFlowState txState) {
        CashoutOnlyResponse cashoutResponse;
        switch (txState.getSuccess()) {
            case SUCCESS:
                SystemHelper.consolePrintln("# CASH-OUT SUCCESSFUL - HAND THEM THE CASH!");
                cashoutResponse = new CashoutOnlyResponse(txState.getResponse());
                SystemHelper.consolePrintln("# Response: " + cashoutResponse.getResponseText());
                SystemHelper.consolePrintln("# RRN: " + cashoutResponse.getRRN());
                SystemHelper.consolePrintln("# Scheme: " + cashoutResponse.getSchemeName());
                SystemHelper.consolePrintln("# Customer receipt:");
                SystemHelper.consolePrintln(!cashoutResponse.wasCustomerReceiptPrinted() ? cashoutResponse.getCustomerReceipt().trim() : "# PRINTED FROM EFTPOS");
                SystemHelper.consolePrintln("# CASHOUT: " + cashoutResponse.getCashoutAmount());
                SystemHelper.consolePrintln("# BANKED NON-CASH AMOUNT: " + cashoutResponse.getBankNonCashAmount());
                SystemHelper.consolePrintln("# BANKED CASH AMOUNT: " + cashoutResponse.getBankCashAmount());
                break;
            case FAILED:
                SystemHelper.consolePrintln("# CASHOUT FAILED!");
                SystemHelper.consolePrintln("# Error: " + txState.getResponse().getError());
                SystemHelper.consolePrintln("# Error detail: " + txState.getResponse().getErrorDetail());
                if (txState.getResponse() != null) {
                    cashoutResponse = new CashoutOnlyResponse(txState.getResponse());
                    SystemHelper.consolePrintln("# Response: " + cashoutResponse.getResponseText());
                    SystemHelper.consolePrintln("# RRN: " + cashoutResponse.getRRN());
                    SystemHelper.consolePrintln("# Scheme: " + cashoutResponse.getSchemeName());
                    SystemHelper.consolePrintln("# Customer receipt:");
                    SystemHelper.consolePrintln(cashoutResponse.getCustomerReceipt().trim());
                }
                break;
            case UNKNOWN:
                SystemHelper.consolePrintln("# WE'RE NOT QUITE SURE WHETHER THE CASHOUT WENT THROUGH OR NOT :/");
                SystemHelper.consolePrintln("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.");
                SystemHelper.consolePrintln("# YOU CAN THE TAKE THE APPROPRIATE ACTION.");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleFinishedMoto(TransactionFlowState txState) {
        MotoPurchaseResponse motoResponse;
        PurchaseResponse purchaseResponse;
        switch (txState.getSuccess()) {
            case SUCCESS:
                SystemHelper.consolePrintln("# WOOHOO - WE GOT MOTO-PAID!");
                motoResponse = new MotoPurchaseResponse(txState.getResponse());
                purchaseResponse = motoResponse.getPurchaseResponse();
                SystemHelper.consolePrintln("# Response: " + purchaseResponse.getResponseText());
                SystemHelper.consolePrintln("# RRN: " + purchaseResponse.getRRN());
                SystemHelper.consolePrintln("# Scheme: " + purchaseResponse.getSchemeName());
                SystemHelper.consolePrintln("# Card entry: " + purchaseResponse.getCardEntry());
                SystemHelper.consolePrintln("# Customer receipt:");
                SystemHelper.consolePrintln(!purchaseResponse.wasCustomerReceiptPrinted() ? purchaseResponse.getCustomerReceipt().trim() : "# PRINTED FROM EFTPOS");
                SystemHelper.consolePrintln("# PURCHASE: " + purchaseResponse.getPurchaseAmount());
                SystemHelper.consolePrintln("# BANKED NON-CASH AMOUNT: " + purchaseResponse.getBankNonCashAmount());
                SystemHelper.consolePrintln("# BANKED CASH AMOUNT: " + purchaseResponse.getBankCashAmount());
                SystemHelper.consolePrintln("# BANKED SURCHARGE AMOUNT: " + purchaseResponse.getSurchargeAmount());
                break;
            case FAILED:
                SystemHelper.consolePrintln("# WE DID NOT GET MOTO-PAID :(");
                SystemHelper.consolePrintln("# Error: " + txState.getResponse().getError());
                SystemHelper.consolePrintln("# Error detail: " + txState.getResponse().getErrorDetail());
                if (txState.getResponse() != null) {
                    motoResponse = new MotoPurchaseResponse(txState.getResponse());
                    purchaseResponse = motoResponse.getPurchaseResponse();
                    SystemHelper.consolePrintln("# Response: " + purchaseResponse.getResponseText());
                    SystemHelper.consolePrintln("# RRN: " + purchaseResponse.getRRN());
                    SystemHelper.consolePrintln("# Scheme: " + purchaseResponse.getSchemeName());
                    SystemHelper.consolePrintln("# Customer receipt:");
                    SystemHelper.consolePrintln(purchaseResponse.getCustomerReceipt().trim());
                }
                break;
            case UNKNOWN:
                SystemHelper.consolePrintln("# WE'RE NOT QUITE SURE WHETHER THE MOTO WENT THROUGH OR NOT :/");
                SystemHelper.consolePrintln("# CHECK THE LAST TRANSACTION ON THE EFTPOS ITSELF FROM THE APPROPRIATE MENU ITEM.");
                SystemHelper.consolePrintln("# YOU CAN THE TAKE THE APPROPRIATE ACTION.");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleFinishedGetLastTransaction(TransactionFlowState txState) {
        if (txState.getResponse() != null) {
            GetLastTransactionResponse gltResponse = new GetLastTransactionResponse(txState.getResponse());

            if (lastCmd.length > 1) {
                // User specified that he intended to retrieve a specific tx by pos_ref_id
                // This is how you can use a handy function to match it.
                Message.SuccessState success = spi.gltMatch(gltResponse, lastCmd[1]);
                if (success == Message.SuccessState.UNKNOWN) {
                    SystemHelper.consolePrintln("# Did not retrieve expected transaction. Here is what we got:");
                } else {
                    SystemHelper.consolePrintln("# Tx matched expected purchase request.");
                }
            }

            PurchaseResponse purchaseResponse = new PurchaseResponse(txState.getResponse());
            SystemHelper.consolePrintln("# Scheme: " + purchaseResponse.getSchemeName());
            SystemHelper.consolePrintln("# Response: " + purchaseResponse.getResponseText());
            SystemHelper.consolePrintln("# RRN: " + purchaseResponse.getRRN());
            SystemHelper.consolePrintln("# Error: " + txState.getResponse().getError());
            SystemHelper.consolePrintln("# Customer receipt:");
            SystemHelper.consolePrintln(purchaseResponse.getCustomerReceipt().trim());
        } else {
            // We did not even get a response, like in the case of a time-out.
            SystemHelper.consolePrintln("# Could not retrieve last transaction.");
        }
    }

    private void handleFinishedSettle(TransactionFlowState txState) {
        switch (txState.getSuccess()) {
            case SUCCESS:
                SystemHelper.consolePrintln("# SETTLEMENT SUCCESSFUL!");
                if (txState.getResponse() != null) {
                    Settlement settleResponse = new Settlement(txState.getResponse());
                    SystemHelper.consolePrintln("# Response: " + settleResponse.getResponseText());
                    SystemHelper.consolePrintln("# Merchant receipt:");
                    SystemHelper.consolePrintln(settleResponse.getMerchantReceipt().trim());
                    SystemHelper.consolePrintln("# Period start: " + settleResponse.getPeriodStartTime());
                    SystemHelper.consolePrintln("# Period end: " + settleResponse.getPeriodEndTime());
                    SystemHelper.consolePrintln("# Settlement time: " + settleResponse.getTriggeredTime());
                    SystemHelper.consolePrintln("# Transaction range: " + settleResponse.getTransactionRange());
                    SystemHelper.consolePrintln("# Terminal ID: " + settleResponse.getTerminalId());
                    SystemHelper.consolePrintln("# Total TX count: " + settleResponse.getTotalCount());
                    SystemHelper.consolePrintln("# Total TX value: " + (settleResponse.getTotalValue() / 100.0));
                    SystemHelper.consolePrintln("# By acquirer TX count: " + settleResponse.getSettleByAcquirerCount());
                    SystemHelper.consolePrintln("# By acquirer TX value: " + (settleResponse.getSettleByAcquirerValue() / 100.0));
                    SystemHelper.consolePrintln("# SCHEME SETTLEMENTS:");
                    Iterable<SchemeSettlementEntry> schemes = settleResponse.getSchemeSettlementEntries();
                    for (SchemeSettlementEntry s : schemes) {
                        SystemHelper.consolePrintln("# " + s);
                    }
                }
                break;
            case FAILED:
                SystemHelper.consolePrintln("# SETTLEMENT FAILED!");
                if (txState.getResponse() != null) {
                    Settlement settleResponse = new Settlement(txState.getResponse());
                    SystemHelper.consolePrintln("# Response: " + settleResponse.getResponseText());
                    SystemHelper.consolePrintln("# Error: " + txState.getResponse().getError());
                    SystemHelper.consolePrintln("# Merchant receipt:");
                    SystemHelper.consolePrintln(settleResponse.getMerchantReceipt().trim());
                }
                break;
            case UNKNOWN:
                SystemHelper.consolePrintln("# SETTLEMENT ENQUIRY RESULT UNKNOWN!");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleFinishedSettlementEnquiry(TransactionFlowState txState) {
        switch (txState.getSuccess()) {
            case SUCCESS:
                SystemHelper.consolePrintln("# SETTLEMENT ENQUIRY SUCCESSFUL!");
                if (txState.getResponse() != null) {
                    Settlement settleResponse = new Settlement(txState.getResponse());
                    SystemHelper.consolePrintln("# Response: " + settleResponse.getResponseText());
                    SystemHelper.consolePrintln("# Merchant receipt:");
                    SystemHelper.consolePrintln(settleResponse.getMerchantReceipt().trim());
                    SystemHelper.consolePrintln("# Period start: " + settleResponse.getPeriodStartTime());
                    SystemHelper.consolePrintln("# Period end: " + settleResponse.getPeriodEndTime());
                    SystemHelper.consolePrintln("# Settlement time: " + settleResponse.getTriggeredTime());
                    SystemHelper.consolePrintln("# Transaction range: " + settleResponse.getTransactionRange());
                    SystemHelper.consolePrintln("# Terminal ID: " + settleResponse.getTerminalId());
                    SystemHelper.consolePrintln("# Total TX count: " + settleResponse.getTotalCount());
                    SystemHelper.consolePrintln("# Total TX value: " + (settleResponse.getTotalValue() / 100.0));
                    SystemHelper.consolePrintln("# By acquirer TX count: " + (settleResponse.getSettleByAcquirerCount()));
                    SystemHelper.consolePrintln("# By acquirer TX value: " + (settleResponse.getSettleByAcquirerValue() / 100.0));
                    SystemHelper.consolePrintln("# SCHEME SETTLEMENTS:");
                    Iterable<SchemeSettlementEntry> schemes = settleResponse.getSchemeSettlementEntries();
                    for (SchemeSettlementEntry s : schemes) {
                        SystemHelper.consolePrintln("# " + s);
                    }
                }
                break;
            case FAILED:
                SystemHelper.consolePrintln("# SETTLEMENT ENQUIRY FAILED!");
                if (txState.getResponse() != null) {
                    Settlement settleResponse = new Settlement(txState.getResponse());
                    SystemHelper.consolePrintln("# Response: " + settleResponse.getResponseText());
                    SystemHelper.consolePrintln("# Error: " + txState.getResponse().getError());
                    SystemHelper.consolePrintln("# Merchant receipt:");
                    SystemHelper.consolePrintln(settleResponse.getMerchantReceipt().trim());
                }
                break;
            case UNKNOWN:
                SystemHelper.consolePrintln("# SETTLEMENT ENQUIRY RESULT UNKNOWN!");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void printActions() {
//        SystemHelper.consolePrintln("# ----------- AVAILABLE ACTIONS ------------");
        clearActions();

        if (spi.getCurrentFlow() == SpiFlow.IDLE) {
            SystemHelper.consolePrintln("# [kebab:1200:100:500:false] - [kebab:price:tip:cashout:promptForCash] charge for kebab with extras!");
            SystemHelper.consolePrintln("# [13kebab:1300] - MOTO - accept payment over the phone");
            SystemHelper.consolePrintln("# [yuck:500] - hand out a refund of $5.00!");
            SystemHelper.consolePrintln("# [cashout:5000] - do a cashout-only transaction");
            SystemHelper.consolePrintln("# [settle] - initiate settlement");
            SystemHelper.consolePrintln("# [settle_enq] - settlement enquiry");
//            SystemHelper.consolePrintln("#");
            SystemHelper.consolePrintln("# [recover:prchs1] - attempt state recovery for pos_ref_id 'prchs1'");
            SystemHelper.consolePrintln("# [glt:prchs1] - get last transaction - expect it to be pos_ref_id 'prchs1'");
//            SystemHelper.consolePrintln("#");
            SystemHelper.consolePrintln("# [rcpt_from_eftpos:true] - offer customer receipt from EFTPOS");
            SystemHelper.consolePrintln("# [sig_flow_from_eftpos:true] - signature flow to be handled by EFTPOS");
            SystemHelper.consolePrintln("# [print_merchant_copy:true] - add printing of footers and headers onto the existing EFTPOS receipt provided by payment application");
            SystemHelper.consolePrintln("# [receipt_header:myheader] - set header for the receipt");
            SystemHelper.consolePrintln("# [receipt_footer:myfooter] - set footer for the receipt");
//            SystemHelper.consolePrintln("#");
        }

        if (spi.getCurrentStatus() == SpiStatus.UNPAIRED && spi.getCurrentFlow() == SpiFlow.IDLE) {
            SystemHelper.consolePrintln("# [pos_id:CITYKEBAB1] - set the POS ID");
        }

        if (spi.getCurrentStatus() == SpiStatus.UNPAIRED || spi.getCurrentStatus() == SpiStatus.PAIRED_CONNECTING) {
            SystemHelper.consolePrintln("# [eftpos_address:10.161.104.104] - set the EFTPOS address");
        }

        if (spi.getCurrentStatus() == SpiStatus.UNPAIRED && spi.getCurrentFlow() == SpiFlow.IDLE)
            SystemHelper.consolePrintln("# [pair] - pair with EFTPOS");

        if (spi.getCurrentStatus() != SpiStatus.UNPAIRED && spi.getCurrentFlow() == SpiFlow.IDLE)
            SystemHelper.consolePrintln("# [unpair] - unpair and disconnect");

        if (spi.getCurrentFlow() == SpiFlow.PAIRING) {
            if (spi.getCurrentPairingFlowState().isAwaitingCheckFromPos())
                SystemHelper.consolePrintln("# [pair_confirm] - confirm pairing code");

            if (!spi.getCurrentPairingFlowState().isFinished())
                SystemHelper.consolePrintln("# [pair_cancel] - cancel pairing");

            if (spi.getCurrentPairingFlowState().isFinished())
                SystemHelper.consolePrintln("# [ok] - acknowledge final");
        }

        if (spi.getCurrentFlow() == SpiFlow.TRANSACTION) {
            TransactionFlowState txState = spi.getCurrentTxFlowState();

            if (txState.isAwaitingSignatureCheck()) {
                SystemHelper.consolePrintln("# [tx_sign_accept] - accept signature");
                SystemHelper.consolePrintln("# [tx_sign_decline] - decline signature");
            }

            if (txState.isAwaitingPhoneForAuth()) {
                SystemHelper.consolePrintln("# [tx_auth_code:123456] - submit phone for auth code");
            }

            if (!txState.isFinished() && !txState.isAttemptingToCancel())
                SystemHelper.consolePrintln("# [tx_cancel] - attempt to cancel transaction");

            if (txState.isFinished())
                SystemHelper.consolePrintln("# [ok] - acknowledge final");
        }

        SystemHelper.consolePrintln("# [status] - reprint buttons/status");
        SystemHelper.consolePrintln("# [bye] - exit");
//        SystemHelper.consolePrintln();
    }

    private void printPairingStatus() {
        SystemHelper.consolePrintln("# --------------- STATUS ------------------");
        SystemHelper.consolePrintln("# " + posId + " <-> EFTPOS: " + eftposAddress + " #");
        SystemHelper.consolePrintln("# SPI STATUS: " + spi.getCurrentStatus() + "     FLOW: " + spi.getCurrentFlow() + " #");
        SystemHelper.consolePrintln("# SPI CONFIG: " + spi.getConfig());
        SystemHelper.consolePrintln("# -----------------------------------------");
        SystemHelper.consolePrintln("# SPI: v" + Spi.getVersion());
    }

    @Override
    protected boolean processInput(String[] spInput) {
        lastCmd = spInput;

        switch (spInput[0].toLowerCase()) {
            case "purchase":
            case "kebab":
                int tipAmount = 0;
                if (spInput.length > 2) tipAmount = NumberUtils.toInt(spInput[2], tipAmount);
                int cashoutAmount = 0;
                if (spInput.length > 3) cashoutAmount = NumberUtils.toInt(spInput[3], cashoutAmount);
                boolean promptForCashout = false;
                if (spInput.length > 4) promptForCashout = Boolean.parseBoolean(spInput[4]);
                // posRefId is what you would usually use to identify the order in your own system.
                String posRefId = "kebab-" + new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.US).format(new Date());
                InitiateTxResult pres = spi.initiatePurchaseTx(posRefId, Integer.parseInt(spInput[1]), tipAmount, cashoutAmount, promptForCashout, options);
                if (!pres.isInitiated()) {
                    SystemHelper.consolePrintln("# Could not initiate purchase: " + pres.getMessage() + ". Please retry.");
                }
                break;

            case "refund":
            case "yuck":
                InitiateTxResult yuckRes = spi.initiateRefundTx("yuck-" + new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.US).format(new Date()), Integer.parseInt(spInput[1]));
                if (!yuckRes.isInitiated()) {
                    SystemHelper.consolePrintln("# Could not initiate refund: " + yuckRes.getMessage() + ". Please retry.");
                }
                break;

            case "cashout":
                InitiateTxResult coRes = spi.initiateCashoutOnlyTx("launder-" + new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.US).format(new Date()), Integer.parseInt(spInput[1]));
                if (!coRes.isInitiated()) {
                    SystemHelper.consolePrintln("# Could not initiate cashout: " + coRes.getMessage() + ". Please retry.");
                }
                break;

            case "13kebab":
                InitiateTxResult motoRes = spi.initiateMotoPurchaseTx("kebab-" + new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.US).format(new Date()), Integer.parseInt(spInput[1]));
                if (!motoRes.isInitiated()) {
                    SystemHelper.consolePrintln("# Could not initiate MOTO purchase: " + motoRes.getMessage() + ". Please retry.");
                }
                break;

            case "pos_id":
                SystemHelper.clearConsole();
                if (spi.setPosId(spInput[1])) {
                    posId = spInput[1];
                    SystemHelper.consolePrintln("## -> POS ID now set to " + posId);
                } else {
                    SystemHelper.consolePrintln("## -> Could not set POS ID");
                }
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "eftpos_address":
                SystemHelper.clearConsole();
                if (spi.setEftposAddress(spInput[1])) {
                    eftposAddress = spInput[1];
                    SystemHelper.consolePrintln("## -> EFTPOS address now set to " + eftposAddress);
                } else {
                    SystemHelper.consolePrintln("## -> Could not set EFTPOS address");
                }
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "pair":
                boolean pairingInited = spi.pair();
                if (!pairingInited) SystemHelper.consolePrintln("## -> Could not start pairing. Check settings.");
                break;

            case "pair_cancel":
                spi.pairingCancel();
                break;

            case "pair_confirm":
                spi.pairingConfirmCode();
                break;

            case "unpair":
                spi.unpair();
                break;

            case "tx_sign_accept":
                spi.acceptSignature(true);
                break;

            case "tx_sign_decline":
                spi.acceptSignature(false);
                break;

            case "tx_cancel":
                spi.cancelTransaction();
                break;

            case "tx_auth_code":
                SubmitAuthCodeResult sacRes = spi.submitAuthCode(spInput[1]);
                if (!sacRes.isValidFormat()) {
                    SystemHelper.consolePrintln("Invalid code format. " + sacRes.getMessage() + ". Try again.");
                }
                break;

            case "settle":
                InitiateTxResult settleRes = spi.initiateSettleTx(RequestIdHelper.id("settle"));
                if (!settleRes.isInitiated()) {
                    SystemHelper.consolePrintln("# Could not initiate settlement: " + settleRes.getMessage() + ". Please retry.");
                }
                break;

            case "settle_enq":
                InitiateTxResult senqRes = spi.initiateSettlementEnquiry(RequestIdHelper.id("stlenq"));
                if (!senqRes.isInitiated()) {
                    SystemHelper.consolePrintln("# Could not initiate settlement enquiry: " + senqRes.getMessage() + ". Please retry.");
                }
                break;

            case "rcpt_from_eftpos":
                spi.getConfig().setPromptForCustomerCopyOnEftpos("true".equalsIgnoreCase(spInput[1]));
                SystemHelper.clearConsole();
                spi.ackFlowEndedAndBackToIdle();
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "sig_flow_from_eftpos":
                spi.getConfig().setSignatureFlowOnEftpos("true".equalsIgnoreCase(spInput[1]));
                SystemHelper.clearConsole();
                spi.ackFlowEndedAndBackToIdle();
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "print_merchant_copy":
                spi.getConfig().setPrintMerchantCopy("true".equalsIgnoreCase(spInput[1]));
                SystemHelper.clearConsole();
                spi.ackFlowEndedAndBackToIdle();
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "receipt_header":
                String inputHeader = spInput[1].replace("\\r\\n", "\r\n");
                inputHeader = inputHeader.replace("\\\\", "\\");
                options.setCustomerReceiptHeader(inputHeader);
                options.setMerchantReceiptHeader(inputHeader);
                SystemHelper.clearConsole();
                spi.ackFlowEndedAndBackToIdle();
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "receipt_footer":
                String inputFooter = spInput[1].replace("\\r\\n", "\r\n");
                inputFooter = inputFooter.replace("\\\\", "\\");
                options.setCustomerReceiptFooter(inputFooter);
                options.setMerchantReceiptFooter(inputFooter);
                SystemHelper.clearConsole();
                spi.ackFlowEndedAndBackToIdle();
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "ok":
                SystemHelper.clearConsole();
                spi.ackFlowEndedAndBackToIdle();
                printStatusAndActions();
                SystemHelper.consolePrint("> ");
                break;

            case "recover":
                SystemHelper.clearConsole();
                InitiateTxResult rRes = spi.initiateRecovery(spInput[1], TransactionType.PURCHASE);
                if (!rRes.isInitiated()) {
                    SystemHelper.consolePrintln("# Could not initiate recovery. " + rRes.getMessage() + ". Please retry.");
                }
                break;

            case "glt":
                InitiateTxResult gltRes = spi.initiateGetLastTx();
                SystemHelper.consolePrintln(gltRes.isInitiated() ?
                        "# GLT Initiated. Will be updated with Progress." :
                        "# Could not initiate GLT: " + gltRes.getMessage() + ". Please Retry.");
                break;

            case "status":
                SystemHelper.clearConsole();
                printStatusAndActions();
                break;

            case "bye":
                return true;

            case "":
                SystemHelper.consolePrint("> ");
                break;

            default:
                SystemHelper.consolePrintln("# I don't understand. Sorry.");
                SystemHelper.consolePrint("> ");
                break;
        }
        return false;
    }

    private void loadPersistedState(String[] args) {
        if (args.length < 1) return;

        // We were given something, at least POS ID and PIN pad address...
        final String[] argSplit = args[0].split(":");
        posId = argSplit[0];

        if (argSplit.length < 2) return;
        eftposAddress = argSplit[1];

        // Let's see if we were given existing secrets as well.
        if (argSplit.length < 4) return;
        spiSecrets = new Secrets(argSplit[2], argSplit[3]);
    }

}
