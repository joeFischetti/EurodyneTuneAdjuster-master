package com.brianledbetter.tuneadjuster

import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.brianledbetter.tuneadjuster.elm327.EcuIO
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity  : FragmentActivity(), BluetoothPickerDialogFragment.BluetoothDialogListener {

    //The messenger that sends messages to other classes
    private var serviceMessenger : Messenger? = null

    //The serviceConnection to the BluetoothService
    private var serviceConnection = BluetoothConnection()

    //Gives us feedback for the switch at the bottom, and can be used
    //  for other actions that rely on the bluetooth connection
    private var isActive = false


    //The messenger that handles inbound messages
    private var serviceReceiveMessenger = Messenger(Handler { message ->
        handleMessage(message)
        true
    })


    //The BluetoothConnection ServiceConnection.  This is the connection to the
    //  bluetooth service that runs in the background (so that other classes can connect)
    inner class BluetoothConnection : ServiceConnection {

        //Called when we connect to the service.  Get the connection status
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            val statusIntent = Intent(ServiceActions.Requests.GET_CONNECTION_STATUS)
            serviceMessenger?.send(messageWithIntent(statusIntent))
        }

        //Called when we disconnect from the service (should be never...?  on close?.  Or if
        // it gets unplugged from the car or the car shuts off?)
        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isActive = false
        }
    }


    //Called when we create the view.  This is what sets up the connection to the service
    //  and sets up the actions for all the buttons.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set the content view for this class
        setContentView(R.layout.activity_main)

        //Create a serviceIntent pointing to the BluetoothService class
        val serviceIntent = Intent(this, BluetoothService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection,
                Context.BIND_AUTO_CREATE)

        //There's a button to go to the adjustSliders activity, set it up
        val buttonAdjustSliders = findViewById<Button>(R.id.buttonAdjustSliders)
        buttonAdjustSliders.setOnClickListener{
            val intent = Intent(this, AdjustSliders::class.java)
            startActivity(intent)
        }

        //There's a button to go to the Logger activity, set it up
        buttonLogger.setOnClickListener{
            val intent = Intent(this, Logger::class.java)
            Log.d("actions", "Starting Logger")
            startActivity(intent)
        }

        //Set the isActive boolean (from either a saved instance, or false)
        isActive = savedInstanceState?.getBoolean("Active") ?: false
        connectionSwitch.isChecked = isActive

        //Set the action for the connection switch
        connectionSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked && !isActive) {
                startConnection()
            } else if (!isChecked) {
                stopConnection()
            }
        }

        //Set the action for the exit button...
        // Stop the bluetooth thread and finish the activity
        exitSwitch.setOnClickListener {
            stopBluetoothThread()
            finish()
        }
    }

    //Called when we would save the instance (putting the app in the bg?)
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putBoolean("Active", isActive)
    }

    //Called when we're killing the app
    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    //Called when you slide the "connect" slider
    private fun startConnection() {
        //Get the defaultAdapter (this, we technically don't need since the
        // BluetoothService does this anyway
        val b = BluetoothAdapter.getDefaultAdapter()

        //If there's no bluetooth adapter available, spit out an error and end
        if (b == null) {
            Toast.makeText(applicationContext, "ERROR! " + "No Bluetooth Device available!", Toast.LENGTH_LONG).show()
            return
        }

        //if bluetooth isn't enabled, prompt to enable it.
        if (!b.isEnabled) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnOn, 0)
        }

        //Get a list of all the bluetooth devices that the phone knows about
        val devices = b.bondedDevices.toTypedArray()

        //If there's at least one device avaiable to the interface
        if (devices.isNotEmpty()) {

            //Present the user with a device picker
            val bpdf = BluetoothPickerDialogFragment()
            bpdf.mPossibleDevices = devices
            bpdf.show(supportFragmentManager, "BluetoothPickerDialogFragment")
        } else {
            Toast.makeText(applicationContext, "ERROR! " + "No Bluetooth Device available!", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopConnection() {

        //Send a "STOP_CONNECTION" intent to the serviceMessenger
        val stopIntent = Intent(ServiceActions.Requests.STOP_CONNECTION)
        serviceMessenger?.send(messageWithIntent(stopIntent))
    }

    private fun handleMessage(message: Message) {
        val intent = message.obj as? Intent
        when (intent?.action) {
            ServiceActions.Responses.SOCKET_CLOSED, ServiceActions.Responses.CONNECTION_NOT_ACTIVE -> {
                statusLabel.text = resources.getString(R.string.not_connected)
                isActive = false
                connectionSwitch.isChecked = false
            }
            ServiceActions.Responses.CONNECTION_ACTIVE -> {
                isActive = true
                connectionSwitch.isChecked = true
                statusLabel.text = resources.getString(R.string.connecting)
            }
            ServiceActions.Responses.CONNECTED -> {
                statusLabel.text = resources.getString(R.string.connected)
            //    serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.FETCH_ECU_DATA)))
            }
//            ServiceActions.Responses.ECU_DATA -> {
//                serviceMessenger?.send(messageWithIntent(Intent(ServiceActions.Requests.FETCH_FEATURE_FLAGS)))
//                val ecuIdData = intent.getParcelableExtra<EcuIO.EcuInfo>("ecuInfo")
//                val ecuIdFragment = EcuIdFragment.newInstance(ecuIdData.swNumber, ecuIdData.swVersion, ecuIdData.vinNumber)
//                supportFragmentManager.beginTransaction()
//                        .replace(R.id.ecuIdFragmentContainer, ecuIdFragment, "ecuId")
//                        .commit()
//            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        stopConnection()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, selectedDevice: String) {
        val connectIntent = Intent(ServiceActions.Requests.START_CONNECTION)
        connectIntent.putExtra("BluetoothDevice", selectedDevice)
        serviceMessenger?.send(messageWithIntent(connectIntent))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopBluetoothThread()
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

}
