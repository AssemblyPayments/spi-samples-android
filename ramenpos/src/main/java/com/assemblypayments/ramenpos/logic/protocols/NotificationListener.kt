package com.assemblypayments.ramenpos.logic.protocols

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.assemblypayments.ramenpos.logic.enums.AppEvent
import com.assemblypayments.spi.model.Message
import com.assemblypayments.spi.model.SpiStatus


class NotificationListener {
    companion object {
        fun registerForEvents(context: Context, appEvents: Array<AppEvent>, responseHandler: BroadcastReceiver) {
            for (event in appEvents) {
                addObserver(context, event, responseHandler)
            }
        }

        private fun addObserver(context: Context, appEvent: AppEvent, responseHandler: BroadcastReceiver) {

            LocalBroadcastManager.getInstance(context).registerReceiver(responseHandler, IntentFilter(appEvent.name))
        }

        private fun removeObserver(context: Context, responseHandler: BroadcastReceiver) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(responseHandler)
        }

        fun postNotification(context: Context, appEvent: AppEvent) {
            val intent = Intent(appEvent.name)

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun postNotification(context: Context, appEvent: AppEvent, status: SpiStatus) {
            val intent = Intent(appEvent.name)
            intent.putExtra("STATUS", status)

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun postNotification(context: Context, appEvent: AppEvent, msg: Message) {
            val intent = Intent(appEvent.name)
            val messageParcel = MessageSerializable()
            messageParcel.message = msg
            intent.putExtra("MSG", messageParcel)

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

}

