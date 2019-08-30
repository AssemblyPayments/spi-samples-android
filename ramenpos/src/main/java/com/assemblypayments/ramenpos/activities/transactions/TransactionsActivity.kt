package com.assemblypayments.ramenpos.activities.transactions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.assemblypayments.ramenpos.R
import com.assemblypayments.ramenpos.activities.connection.ConnectionActivity
import com.assemblypayments.ramenpos.activities.main.MainActivity
import com.assemblypayments.ramenpos.activities.main.TransactionFlowChange
import com.assemblypayments.ramenpos.logic.RamenPos
import com.assemblypayments.ramenpos.logic.enums.AppEvent
import com.assemblypayments.ramenpos.logic.protocols.NotificationListener
import com.assemblypayments.spi.model.SpiStatus
import com.assemblypayments.spi.model.TransactionOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_transactions.*
import kotlinx.android.synthetic.main.activity_transactions.txtAddress
import kotlinx.android.synthetic.main.activity_transactions.txtFlow
import kotlinx.android.synthetic.main.activity_transactions.txtPosId
import kotlinx.android.synthetic.main.activity_transactions.txtStatus
import java.text.SimpleDateFormat
import java.util.*

open class TransactionsActivity : AppCompatActivity() {
    private val TAG = "TRANSACTIONS ACTIVITY"
    private var transactionFlowChange = TransactionFlowChange()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        RamenPos.transactionsActivity = this

        val appEvents: Array<AppEvent> = arrayOf(AppEvent.CONNNECTION_STATUS_CHANGED, AppEvent.PAIRING_FLOW_CHANGED, AppEvent.TRANSACTION_FLOW_STATE_CHANGED, AppEvent.SECRET_DROPPED, AppEvent.DEVICE_ADDRESS_CHANGED)
        NotificationListener.registerForEvents(applicationContext, appEvents, addEvent)

        val mBtnConnection: Button = this.findViewById(R.id.btnTransConnection)
        mBtnConnection.setOnClickListener {
            val intent = Intent(this, ConnectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivityIfNeeded(intent, 0)
        }

        val mBtnMain: Button = this.findViewById(R.id.btnMain)
        mBtnMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivityIfNeeded(intent, 0)
        }

        btnPurchase.setOnClickListener {
            if (RamenPos.spi?.currentStatus == SpiStatus.PAIRED_CONNECTED) {
                val posRefId = "ramen-" + SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(Date())

                val amount = txtAmount.text.toString().toIntOrNull()
                if (amount == 0 || amount == null) {
                    return@setOnClickListener
                }

                var tipAmount = 0
                var cashout = 0
                var surchargeAmount = 0

                if (rbTip.isSelected) {
                    tipAmount = txtNone.text.toString().toInt()
                } else if (rbCashout.isSelected) {
                    cashout = txtNone.text.toString().toInt()
                } else if (rbSurcharge.isSelected) {
                    surchargeAmount = txtNone.text.toString().toInt()
                }

                val promptCashout = false

                RamenPos.spi?.enablePayAtTable()

                // Receipt header/footer
                val options = TransactionOptions()
//                val options = RamenPos.spi?.setReceiptHeaderFooter ()

                RamenPos.spi?.initiatePurchaseTx(posRefId, amount, tipAmount, cashout, promptCashout, options, surchargeAmount)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_transactions)
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
        txtStatus.text = RamenPos.mainActivity.txtStatus.text
        txtPosId.text = RamenPos.settings?.posId
        txtAddress.text = RamenPos.settings?.eftposAddress
        txtFlow.text = RamenPos.spi?.currentFlow?.name
        btnTransConnection.text = RamenPos.mainActivity.btnConnection.text
        Log.d(TAG, "In onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "In onDestroy")
    }

    private val addEvent = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // intent  get arg
            //val position = intent.getStringExtra("***arg_name****")

//            AppEvent.CONNNECTION_STATUS_CHANGED.name,
//            AppEvent.TRANSACTION_FLOW_STATE_CHANGED.name ->

            when (intent.action) {
                AppEvent.PAIRING_FLOW_CHANGED.name ->
                    runOnUiThread {
                        transactionFlowChange.stateChanged()
                    }
            }
        }
    }
}
