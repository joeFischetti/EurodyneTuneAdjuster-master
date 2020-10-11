package com.brianledbetter.tuneadjuster

import android.annotation.TargetApi
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import com.brianledbetter.tuneadjuster.elm327.BluetoothThread
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.locks.LockSupport


class BluetoothService: Service() {

    //The service connection to the bluetooth thread
    private var btThread : BluetoothThread? = null

    //The messenger that handls messages to the bt thread
    private var threadMessenger : Messenger? = null

    //The messenger that handles messages back from the BluetoothThread
    private val messageToActivityHandler = Handler { message ->
        val messageIntent = message.obj as Intent

        //Send whatever came through back to the class
        threadMessenger?.send(messageWithIntent(messageIntent))
        if (messageIntent.action == ServiceActions.Responses.SOCKET_CLOSED) {
            btThread = null
        }
        true
    }

    //Set the btAdapater (default, is the adapter that the device has as the default)
    private val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    //The name of the device we want to connect to (defaults, to "none" since it hasn't
    //  been picked
    private var btDeviceName: String = "none"

    //Messages from the activity
    private val fromActivityMessenger = Messenger(Handler { message ->
        val messageIntent = message.obj as Intent

        if (message.replyTo != null) {
            threadMessenger = message.replyTo
        }
        when (messageIntent.action) {
            ServiceActions.Requests.START_CONNECTION -> {
                Log.d("BTService","START_CONNECTION")
                val selectedDevice = messageIntent.getStringExtra("BluetoothDevice")
                //btAdapter = BluetoothAdapter.getDefaultAdapter()
                btDeviceName = selectedDevice
                val btDevice = btAdapter.getRemoteDevice(btDeviceName)
                btThread = BluetoothThread(btDevice, Messenger(messageToActivityHandler))
                btThread?.start()
                threadMessenger?.send(messageWithIntent(getStatusIntent()))
            }

            ServiceActions.Requests.GET_CONNECTION_STATUS -> {
                Log.d("BTService","GET_CONNECTION_STATUS")
                threadMessenger?.send(messageWithIntent(getStatusIntent()))
            }
            else -> { // Forward on to connection thread
                Log.d("BTService","Forwarding intent to BTThread")
                val forwardMessage = btThread?.handler?.obtainMessage()
                forwardMessage?.obj = messageIntent
                btThread?.handler?.sendMessage(forwardMessage)
            }
        }
        true
    })


    private fun getStatusIntent() : Intent {
        return if (btThread != null) {
            Intent(ServiceActions.Responses.CONNECTION_ACTIVE)
        } else {
            Intent(ServiceActions.Responses.CONNECTION_NOT_ACTIVE)
        }
    }


    private fun messageWithIntent(i: Intent) : Message {
        val message = Message()
        message.obj = i
        return message
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        Log.d("BTService","Creating new bluetooth service/Showing notification")
        super.onCreate()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        // Android continues to be the worst development platform in history
        val channelId = "ed_tune_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH)
            val description = getString(R.string.channel_description)
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.app_icon)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()
        startForeground(ServiceActions.SERVICE_ID, notification)
    }

    override fun onDestroy() {
        Log.d("BTService", "BTService has been destroyed")
        super.onDestroy()
        LockSupport.unpark(btThread)
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("BTService", "Rebinding to bluetooth")
        return fromActivityMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BTService","Starting bluetooth service")
        return Service.START_STICKY
    }
}