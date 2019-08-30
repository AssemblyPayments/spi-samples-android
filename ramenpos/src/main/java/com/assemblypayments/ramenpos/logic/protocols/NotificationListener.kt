package com.assemblypayments.ramenpos.logic.protocols

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.assemblypayments.ramenpos.logic.enums.AppEvent
import java.util.*


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
            var intent = Intent(appEvent.name)

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}