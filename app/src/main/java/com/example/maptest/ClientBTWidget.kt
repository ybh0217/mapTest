package com.example.maptest

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import kotlin.concurrent.timer


class ClientBTWidget : AppWidgetProvider() {

    lateinit var bAdapter: BluetoothAdapter

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {

    }
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        bAdapter = BluetoothAdapter.getDefaultAdapter()

        //브로드캐스트 내용과 일치 확인
        if (ACTION_BUTTON1.equals(intent?.action)){

            if (bAdapter == null) {
                Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Bluetooth is available", Toast.LENGTH_SHORT).show()

                if (bAdapter.isEnabled) {
                    Toast.makeText(context, "Already on", Toast.LENGTH_SHORT).show()
                } else {
                    bAdapter.enable()
                    Toast.makeText(context, "Bluetooth is on", Toast.LENGTH_LONG).show()
                }

                if (bAdapter.isDiscovering) {
                    Toast.makeText(context, "Already Your device discoverable", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Making Your device discoverable", Toast.LENGTH_LONG).show()
                    timer(period = 30000) {
                        bAdapter.startDiscovery()
                    }
                }
            }
        }
    }

    companion object {
        internal val ACTION_BUTTON1 = "ACTTION BUTTON1"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

            //intent 정보입력
            val intent = Intent(context, ClientBTWidget::class.java)
            intent.action = ACTION_BUTTON1
            intent.putExtra("appWigetID",appWidgetId)

            //Broadcast 설정
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)

            //Widget 선택
            val views = RemoteViews(context.packageName, R.layout.client_b_t_widget)
            views.setOnClickPendingIntent(R.id.BTWidgetButton,pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
