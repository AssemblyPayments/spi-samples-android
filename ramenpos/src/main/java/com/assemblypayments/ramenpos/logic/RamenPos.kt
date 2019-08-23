package com.assemblypayments.ramenpos.logic

import android.app.Application
import com.assemblypayments.ramenpos.activities.connection.ConnectionActivity
import com.assemblypayments.ramenpos.activities.main.MainActivity
import com.assemblypayments.ramenpos.activities.transactions.TransactionsActivity
import com.assemblypayments.ramenpos.logic.settings.SettingsProvider
import com.assemblypayments.spi.Spi
import com.assemblypayments.spi.model.Secrets
import com.assemblypayments.spi.model.TransactionOptions

class RamenPos : Application() {
    companion object {
        var spi: Spi? = null

        var secrets: Secrets? = null
        var options: TransactionOptions? = null
        var autoAddressEnabled: Boolean = false
        var isAppStarted: Boolean = false

        var connectionActivity: ConnectionActivity = ConnectionActivity()
        var mainActivity: MainActivity = MainActivity()
        var transactionsActivity: TransactionsActivity = TransactionsActivity()
        var settings: SettingsProvider? = null
        var spiDelegation: SPIDelegation = SPIDelegation()

        fun initialize() {
            spi = Spi(settings?.posId!!, settings?.serialNumber!!, settings?.eftposAddress!!, secrets)

            spi!!.setPosInfo("assembly", "2.6.1")
            options = TransactionOptions()

            spi!!.setDeviceAddressChangedHandler(spiDelegation::onDeviceAddressChanged)
            spi!!.setStatusChangedHandler(spiDelegation::onSpiStatusChanged)
            spi!!.setPairingFlowStateChangedHandler(spiDelegation::onPairingFlowStateChanged)
            spi!!.setSecretsChangedHandler(spiDelegation::onSecretsChanged)
            spi!!.setTxFlowStateChangedHandler(spiDelegation::onTxFlowStateChanged)

            spi!!.setPrintingResponseDelegate(spiDelegation::handlePrintingResponse)
            spi!!.setTerminalStatusResponseDelegate(spiDelegation::handleTerminalStatusResponse)
            spi!!.setTerminalConfigurationResponseDelegate(spiDelegation::handleTerminalConfigurationResponse)
            spi!!.setBatteryLevelChangedDelegate(spiDelegation::handleBatteryLevelChanged)

            spi!!.setAcquirerCode("wbc")
            spi!!.setDeviceApiKey("RamenPosDeviceAddressApiKey")

            spi!!.start()
        }
    }
}