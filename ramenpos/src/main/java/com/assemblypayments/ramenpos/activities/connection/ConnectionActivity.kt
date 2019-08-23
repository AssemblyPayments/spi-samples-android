package com.assemblypayments.ramenpos.activities.connection

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.assemblypayments.ramenpos.R
import com.assemblypayments.ramenpos.activities.main.MainActivity
import com.assemblypayments.ramenpos.logic.RamenPos
import com.assemblypayments.spi.model.DeviceAddressResponseCode
import com.assemblypayments.spi.model.SpiFlow
import com.assemblypayments.spi.model.SpiStatus
import kotlinx.android.synthetic.main.activity_connection.*
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.lang.StringUtils


open class ConnectionActivity : AppCompatActivity() {
    private var dialogBuilder: AlertDialog.Builder? = null
    private var alertDialog: AlertDialog? = null

    val TAG = "CONNECTION ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        RamenPos.connectionActivity = this

        RamenPos.isAppStarted = true
        Log.d(TAG, "In onCreate")

        val mBtn: Button = findViewById(R.id.btnMain)
        mBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivityIfNeeded(intent, 0)
        }

        edtPosId.setText(RamenPos.settings?.posId)
        edtAddress.setText(RamenPos.settings?.eftposAddress)
        edtSerialNum.setText(RamenPos.settings?.serialNumber)
        swtTestMode.isChecked = RamenPos.settings?.testMode!!
        swtAuto.isChecked = RamenPos.settings?.autoResolution!!

        btnMain.text = RamenPos.mainActivity.txtStatus.text

//        RamenPos.settings?.autoResolution = swtAuto.isChecked
//        RamenPos.settings?.testMode = swtTestMode.isChecked

        btnSave.setOnClickListener {
            try {
                if (!areControlsValid(false)) {
                    return@setOnClickListener
                }

                if (!RamenPos.isAppStarted && swtAuto.isChecked) {
                    RamenPos.settings?.serialNumber = ""
                    RamenPos.settings?.eftposAddress = ""
                    RamenPos.settings?.posId = ""
                    RamenPos.isAppStarted = true
                    RamenPos.initialize()
                }

                RamenPos.spi?.setTestMode(swtTestMode.isChecked)
                RamenPos.spi?.setAutoAddressResolution(swtAuto.isChecked)
                RamenPos.spi?.setSerialNumber(edtSerialNum.text.toString())
            } catch (ex: Exception) {
                runOnUiThread {
                    AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage("SPI Client status:".plus(RamenPos.spi?.currentStatus!!.name))
                            .setPositiveButton("OK") { _, _ -> }
                            .setTitle("Error")
                            .show()
                }
            }
        }
        swtAuto.setOnClickListener {
            //btnAction.setEnabled(true);
            btnSave.isEnabled = swtAuto.isChecked
            swtTestMode.isChecked = swtAuto.isChecked
            swtTestMode.isEnabled = swtAuto.isChecked
            edtAddress.isEnabled = !swtAuto.isChecked
            edtSerialNum.isEnabled = true

        }
        btnPair.setOnClickListener {
            if (RamenPos.spi?.currentStatus != SpiStatus.UNPAIRED) {
                runOnUiThread {
                    AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage("SPI Client status:".plus(RamenPos.spi?.currentStatus!!.name))
                            .setPositiveButton("OK") { _, _ -> }
                            .setTitle("Error")
                            .show()
                }
                return@setOnClickListener
            }

            if (!areControlsValid(true)) {
                return@setOnClickListener
            }

            RamenPos.settings?.posId = edtPosId.text.toString()
            RamenPos.settings?.eftposAddress = edtAddress.text.toString()
            RamenPos.settings?.serialNumber = edtSerialNum.text.toString()
            RamenPos.settings?.encriptionKey = null
            RamenPos.settings?.hmacKey = null

            try {
                RamenPos.spi?.setPosId(edtPosId.text.toString())
                RamenPos.spi?.setEftposAddress(edtAddress.text.toString())
                RamenPos.spi?.pair()
            } catch (ex: Exception) {
                //LOG.error(ex.getMessage());
                runOnUiThread {
                    AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage(ex.message)
                            .setPositiveButton("OK") { _, _ -> }
                            .setTitle("Error")
                            .show()
                }
            }
        }
        btnUnpair.setOnClickListener {
            RamenPos.spi?.unpair()
        }
        btnCancel.setOnClickListener {
            RamenPos.spi?.pairingCancel()
        }
        swtTestMode.setOnCheckedChangeListener { _, isChecked ->
            RamenPos.settings?.testMode = isChecked
        }
        swtAuto.setOnCheckedChangeListener { _, isChecked ->
            swtTestMode.isChecked = isChecked
            swtTestMode.isEnabled = isChecked
            btnSave.isEnabled = isChecked
            //edtAddress.isEnabled = !isChecked
            RamenPos.settings?.autoResolution = isChecked
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_connection)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "In onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "In onStop")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "In onResume")
        btnMain.text = RamenPos.mainActivity.txtStatus.text
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "In onDestroy")
    }

    fun printStatusAndAction() {
        //SPILogMsg("printStatusAndAction \(String(describing: state))")
        when (RamenPos.spi?.currentStatus) {
            SpiStatus.UNPAIRED ->
                when (RamenPos.spi?.currentFlow) {
                    SpiFlow.TRANSACTION,
                    SpiFlow.IDLE -> return
                    SpiFlow.PAIRING -> {
                        showPairing()

                    }
                    null -> {
                        runOnUiThread {
                            AlertDialog.Builder(this)
                                    .setCancelable(false)
                                    .setMessage("Unexpected flow: ".plus(RamenPos.spi?.currentFlow.toString()))
                                    .setPositiveButton("OK") { _, _ -> }
                                    .setTitle("Error")
                                    .show()
                        }
                    }
                }
            SpiStatus.PAIRED_CONNECTED,
            SpiStatus.PAIRED_CONNECTING -> {
                when (RamenPos.spi?.currentFlow) {
                    SpiFlow.TRANSACTION,
                    SpiFlow.IDLE -> return
                    SpiFlow.PAIRING -> {
                        showPairing()
                    }
                    null -> TODO()
                }
            }
            null -> return
        }

    }

    fun deviceAddressStatusAndAction() {
        runOnUiThread {
            //        btnAction.setEnabled(false)
            if (RamenPos.spi?.currentStatus == SpiStatus.UNPAIRED && RamenPos.spi?.currentDeviceStatus != null) {
                when (RamenPos.spi?.currentDeviceStatus!!.deviceAddressResponseCode) {
                    DeviceAddressResponseCode.SUCCESS -> {
                        edtAddress.setText(RamenPos.spi?.currentDeviceStatus!!.address)

                        AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setMessage("Device Address has been updated to ".plus(RamenPos.spi?.currentDeviceStatus!!.address))
                                .setPositiveButton("OK") { _, _ -> }
                                .setTitle("Device Address Status")
                                .show()

                    }
                    DeviceAddressResponseCode.INVALID_SERIAL_NUMBER -> {
                        edtAddress.setText("")
                        AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setMessage("The serial number is invalid!")
                                .setPositiveButton("OK") { _, _ -> }
                                .setTitle("Device Address Status")
                                .show()
                    }
                    DeviceAddressResponseCode.DEVICE_SERVICE_ERROR -> {
                        edtAddress.setText("")
                        AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setMessage("Device service is down!")
                                .setPositiveButton("OK") { _, _ -> }
                                .setTitle("Device Address Status")
                                .show()
                    }
                    DeviceAddressResponseCode.ADDRESS_NOT_CHANGED -> {
                        //btnAction.setEnabled(true)
                        AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setMessage("The IP address have not changed!")
                                .setPositiveButton("OK") { _, _ -> }
                                .setTitle("Device Address Status")
                                .show()
                    }
                    DeviceAddressResponseCode.SERIAL_NUMBER_NOT_CHANGED -> {
                        //btnAction.setEnabled(true)
                        AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setMessage("The Serial Number have not changed!")
                                .setPositiveButton("OK") { _, _ -> }
                                .setTitle("Device Address Status")
                                .show()
                    }
                    else -> {
                        AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setMessage("The IP address have not changed or The serial number is invalid!")
                                .setPositiveButton("OK") { _, _ -> }
                                .setTitle("Device Address Status")
                                .show()
                    }
                }
            }
        }
    }

    private fun showPairing() {
//        SPILogMsg("showPairing")
        runOnUiThread {
            if (alertDialog != null && alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }

            dialogBuilder = AlertDialog.Builder(this)

            if (RamenPos.spi?.currentPairingFlowState == null) {
                dialogBuilder?.setTitle("Error")
                dialogBuilder?.setMessage("Missing pairingFlowState".plus(RamenPos.spi?.currentPairingFlowState.toString()))
                dialogBuilder?.setPositiveButton("OK") { _, _ -> }
            }

            if (RamenPos.spi?.currentPairingFlowState?.isAwaitingCheckFromPos!!) {
                dialogBuilder?.setTitle("EFTPOS Pairing Process")
                dialogBuilder?.setMessage(RamenPos.spi?.currentPairingFlowState!!.message.toString())
                dialogBuilder?.setNegativeButton("No") { _, _ -> RamenPos.spi?.pairingCancel() }
                dialogBuilder?.setPositiveButton("Yes") { _, _ -> RamenPos.spi?.pairingConfirmCode() }
            } else if (!RamenPos.spi?.currentPairingFlowState?.isFinished!!) {
                dialogBuilder?.setTitle("EFTPOS Pairing Process")
                dialogBuilder?.setMessage(RamenPos.spi?.currentPairingFlowState!!.message.toString())
                dialogBuilder?.setNegativeButton("Cancel") { _, _ -> RamenPos.spi?.pairingCancel() }
            } else if (RamenPos.spi?.currentPairingFlowState?.isSuccessful!!) {
                dialogBuilder?.setTitle("EFTPOS Pairing Process")
                dialogBuilder?.setMessage(RamenPos.spi?.currentPairingFlowState!!.message.toString())
                dialogBuilder?.setPositiveButton("OK") { _, _ -> RamenPos.spi?.ackFlowEndedAndBackToIdle() }
            } else {
                dialogBuilder?.setTitle("Error")
                dialogBuilder?.setMessage(RamenPos.spi?.currentPairingFlowState!!.message.toString())
                dialogBuilder?.setPositiveButton("OK") { _, _ -> RamenPos.spi?.ackFlowEndedAndBackToIdle() }
            }

            alertDialog = dialogBuilder!!.create()
            alertDialog?.setCancelable(false)
            alertDialog?.show()
        }
    }

    private fun areControlsValid(isPairing: Boolean): Boolean {
        RamenPos.autoAddressEnabled = swtAuto.isChecked
        RamenPos.settings?.posId = edtPosId.text.toString()
        RamenPos.settings?.eftposAddress = edtAddress.text.toString()
        RamenPos.settings?.serialNumber = edtSerialNum.text.toString()


        if (isPairing && (StringUtils.isWhitespace(RamenPos.settings?.eftposAddress))) {
            runOnUiThread {
                AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Please enable auto address resolution or enter a device address")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Error")
                        .show()
            }
            return false
        }

        if (StringUtils.isWhitespace(RamenPos.settings?.posId)) {
            runOnUiThread {
                AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Please provide a Pos Id")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Error")
                        .show()
            }
            return false
        }

        if (!isPairing && RamenPos.settings?.autoResolution!! && (StringUtils.isWhitespace(RamenPos.settings?.serialNumber))) {
            runOnUiThread {
                AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Please provide a Serial Number")
                        .setPositiveButton("OK") { _, _ -> }
                        .setTitle("Error")
                        .show()
            }
            return false
        }

        return true
    }
}
