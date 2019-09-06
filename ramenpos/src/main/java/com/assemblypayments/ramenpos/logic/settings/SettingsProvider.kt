package com.assemblypayments.ramenpos.logic.settings

import android.content.SharedPreferences

class SettingsProvider(private var preferences: SharedPreferences) {
    private var editor: SharedPreferences.Editor = preferences.edit()

    enum class SettingsKeys {
        PosId,
        EftposAddress,
        EncriptionKey,
        HmacKey,
        CustomerReceipt,
        CustomerSignature,
        PrintMerchantCopy,
        ReceiptHeader,
        ReceiptFooter,
        PrintText,
        PosVendorKey,
        TestMode,
        AutoResolution,
        SuppressMerchantPassword,
        SerialNumber
    }

    var posId: String?
        get() = preferences.getString(SettingsKeys.PosId.name, "")
        set(value) {
            editor.putString(SettingsKeys.PosId.name, value)
            editor.apply()
        }

    var eftposAddress: String?
        get() = preferences.getString(SettingsKeys.EftposAddress.name, "")
        set(value) {
            editor.putString(SettingsKeys.EftposAddress.name, value)
            editor.apply()
        }

    var encriptionKey: String?
        get() = preferences.getString(SettingsKeys.EncriptionKey.name, "")
        set(value) {
            editor.putString(SettingsKeys.EncriptionKey.toString(), value)
            editor.apply()
        }

    var hmacKey: String?
        get() = preferences.getString(SettingsKeys.HmacKey.name, "")
        set(value) {
            editor.putString(SettingsKeys.HmacKey.toString(), value)
            editor.apply()
        }

    var customerReceiptFromEftpos: Boolean
        get() = preferences.getBoolean(SettingsKeys.CustomerReceipt.name, false)
        set(value) {
            editor.putBoolean(SettingsKeys.CustomerReceipt.toString(), value)
            editor.apply()
        }

    var customerSignatureFromEftpos: Boolean
        get() = preferences.getBoolean(SettingsKeys.CustomerSignature.name, false)
        set(value) {
            editor.putBoolean(SettingsKeys.CustomerSignature.toString(), value)
            editor.apply()
        }

    var printMerchantCopy: Boolean
        get() = preferences.getBoolean(SettingsKeys.PrintMerchantCopy.name, false)
        set(value) {
            editor.putBoolean(SettingsKeys.PrintMerchantCopy.toString(), value)
            editor.apply()
        }

    var receiptHeader: String?
        get() = preferences.getString(SettingsKeys.ReceiptHeader.name, "")
        set(value) {
            editor.putString(SettingsKeys.ReceiptHeader.toString(), value)
            editor.apply()
        }

    var receiptFooter: String?
        get() = preferences.getString(SettingsKeys.ReceiptFooter.name, "")
        set(value) {
            editor.putString(SettingsKeys.ReceiptFooter.toString(), value)
            editor.apply()
        }

    var printText: String?
        get() = preferences.getString(SettingsKeys.PrintText.name, "")
        set(value) {
            editor.putString(SettingsKeys.PrintText.toString(), value)
            editor.apply()
        }

    var posVendorKey: String?
        get() = preferences.getString(SettingsKeys.PosVendorKey.name, "")
        set(value) {
            editor.putString(SettingsKeys.PosVendorKey.toString(), value)
            editor.apply()
        }

    var testMode: Boolean
        get() = preferences.getBoolean(SettingsKeys.TestMode.name, false)
        set(value) {
            editor.putBoolean(SettingsKeys.TestMode.toString(), value)
            editor.apply()
        }

    var autoResolution: Boolean
        get() = preferences.getBoolean(SettingsKeys.AutoResolution.name, false)
        set(value) {
            editor.putBoolean(SettingsKeys.AutoResolution.toString(), value)
            editor.apply()
        }

    var suppressMerchantPassword: Boolean
        get() = preferences.getBoolean(SettingsKeys.SuppressMerchantPassword.name, false)
        set(value) {
            editor.putBoolean(SettingsKeys.SuppressMerchantPassword.toString(), value)
            editor.apply()
        }

    var serialNumber: String?
        get() = preferences.getString(SettingsKeys.SerialNumber.name, "")
        set(value) {
            editor.putString(SettingsKeys.SerialNumber.toString(), value)
            editor.apply()
        }

}