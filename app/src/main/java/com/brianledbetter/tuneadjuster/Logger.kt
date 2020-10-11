package com.brianledbetter.tuneadjuster

import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.brianledbetter.tuneadjuster.elm327.EcuIO
import com.brianledbetter.tuneadjuster.elm327.EurodyneIO
import kotlinx.android.synthetic.main.activity_adjustsliders.*
import kotlinx.android.synthetic.main.activity_logger.*
import kotlinx.android.synthetic.main.activity_logger.exitSwitch
import kotlinx.android.synthetic.main.activity_logger.statusLabel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class Logger(private var WOTLoggingEnabled: Boolean = false, private var filePath: File? = null) : FragmentActivity() {
    //private var WOTLoggingEnabled = false

    //private var filePath? = null

    private var serviceReceiveMessenger = Messenger(Handler { message ->
        handleMessage(message)
        true
    })

    private var serviceMessenger : Messenger? = null

    inner class BluetoothConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("Logger","Setting up Bluetooth connection")
            Log.d("Logger","Setting up service messenger")
            serviceMessenger = Messenger(service)

            Log.d("Logger","Getting bluetooth connection status")
            val statusIntent = Intent(ServiceActions.Requests.GET_CONNECTION_STATUS)
            serviceMessenger?.send(messageWithIntent(statusIntent))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isActive = false
        }
    }

    private var serviceConnection = BluetoothConnection()

    private var isActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        filePath = this.getExternalFilesDir(null)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logger)

        val serviceIntent = Intent(this, BluetoothService::class.java)
        Log.d("Logger","Starting/connecting to bluetooth")
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection,
                Context.BIND_AUTO_CREATE)

        isActive = savedInstanceState?.getBoolean("Active") ?: false
        buttonStartLogging.isEnabled = !WOTLoggingEnabled
        buttonStopLogging.isEnabled = !WOTLoggingEnabled

        Log.d("Logger", "File path for logger: " + filePath)

        //startLogger()

        buttonStartLogging.setOnClickListener{
            startWriteLogs()
        }

        buttonStopLogging.setOnClickListener{
            stopWriteLogs()
        }

        buttonWOTLogging.setOnClickListener{

        }

        exitSwitch.setOnClickListener {

            finish()
        }

        restartLogger.setOnClickListener{
            restartLogger()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putBoolean("Active", isActive)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }


    private fun stopConnection() {
        val stopIntent = Intent(ServiceActions.Requests.STOP_CONNECTION)
        serviceMessenger?.send(messageWithIntent(stopIntent))
    }

    private fun handleMessage(message: Message) {
        val intent = message.obj as? Intent
        when (intent?.action) {
            ServiceActions.Responses.SOCKET_CLOSED, ServiceActions.Responses.CONNECTION_NOT_ACTIVE -> {
                statusLabel.text = resources.getString(R.string.not_connected)
                isActive = false
                //buttonSave.isEnabled = false
                //connectionSwitch.isChecked = false
            }
            ServiceActions.Responses.CONNECTION_ACTIVE -> {
                isActive = true
                //connectionSwitch.isChecked = true
                statusLabel.text = resources.getString(R.string.connecting)
            }
            ServiceActions.Responses.CONNECTED -> {
                statusLabel.text = resources.getString(R.string.connected)
                serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.START_LOGGER)))

            }
            ServiceActions.Responses.LOGGER_STARTED -> {
                statusLabel.text = "Logger Ready"
            }
            ServiceActions.Responses.LOGGER_FAILED -> {
                statusLabel.text = "Logger failed"
            }
            ServiceActions.Responses.LOGGER_ACTIVE -> {
                statusLabel.text = "Logger Active"
            }
        }
    }

    //override fun onDialogNegativeClick(dialog: DialogFragment) {
    //    stopConnection()
    //}

    //override fun onDialogPositiveClick(dialog: DialogFragment, selectedDevice: String) {
    //    val connectIntent = Intent(ServiceActions.Requests.START_CONNECTION)
    //    connectIntent.putExtra("BluetoothDevice", selectedDevice)
    //    serviceMessenger?.send(messageWithIntent(connectIntent))
    //}

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun stopBluetoothThread() {
        val serviceIntent = Intent(this, BluetoothService::class.java)
        stopService(serviceIntent)
    }

    private fun messageWithIntent(i : Intent) : Message {
        val message = Message()
        message.obj = i
        message.replyTo = serviceReceiveMessenger
        return message
    }

    private fun startLogger() {
        val startLoggerIntent = Intent(ServiceActions.Requests.START_LOGGER)
        serviceMessenger?.send(messageWithIntent(startLoggerIntent))
    }

    private fun startWriteLogs(){
        val startWriteIntent = Intent(ServiceActions.Requests.WRITE_LOGS)
        var filename = filePath.toString().plus("testFile.log")

        startWriteIntent.putExtra("fileName", filename)
        serviceMessenger?.send(messageWithIntent(startWriteIntent))
    }

    private fun stopWriteLogs(){
        val stopWriteIntent = Intent(ServiceActions.Requests.NO_WRITE_LOGS)
        serviceMessenger?.send(messageWithIntent(stopWriteIntent))
    }

    private fun restartLogger(){
        serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.START_LOGGER)))
    }
}