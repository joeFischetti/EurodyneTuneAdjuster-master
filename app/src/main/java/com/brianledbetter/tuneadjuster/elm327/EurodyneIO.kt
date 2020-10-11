package com.brianledbetter.tuneadjuster.elm327

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.content.Context
import live.pinger.android.tuneadjuster.util.LoggedParameter
import live.pinger.android.tuneadjuster.util.ParameterDefinition
import unsigned.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext

/**
 * Created by brian.ledbetter on 1/20/18.
 * Modified by joe.fischetti on 10/3/2020.
 */
class EurodyneIO(private val io : UDSIO, private val directIO : ElmIO) {

    var writeToFile: Boolean = false
    var fileName: String = "log.txt"

    data class FeatureFlagInfo(val boostEnabled: Boolean, val octaneEnabled: Boolean, val e85Enabled: Boolean) : Parcelable {
        private fun Boolean.toInt() = if (this) 1 else 0

        constructor(parcel: Parcel) : this(
                parcel.readInt() > 0,
                parcel.readInt() > 0,
                parcel.readInt() > 0
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(boostEnabled.toInt())
            parcel.writeInt(octaneEnabled.toInt())
            parcel.writeInt(e85Enabled.toInt())
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<FeatureFlagInfo> {
            override fun createFromParcel(parcel: Parcel): FeatureFlagInfo {
                return FeatureFlagInfo(parcel)
            }

            override fun newArray(size: Int): Array<FeatureFlagInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class OctaneInfo(val minimum: Int, val maximum: Int, val current: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(minimum)
            parcel.writeInt(maximum)
            parcel.writeInt(current)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<OctaneInfo> {
            override fun createFromParcel(parcel: Parcel): OctaneInfo {
                return OctaneInfo(parcel)
            }

            override fun newArray(size: Int): Array<OctaneInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class E85Info(val current: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(current)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<E85Info> {
            override fun createFromParcel(parcel: Parcel): E85Info {
                return E85Info(parcel)
            }

            override fun newArray(size: Int): Array<E85Info?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class BoostInfo(val minimum: Int, val maximum: Int, val current: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(minimum)
            parcel.writeInt(maximum)
            parcel.writeInt(current)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<BoostInfo> {
            override fun createFromParcel(parcel: Parcel): BoostInfo {
                return BoostInfo(parcel)
            }

            override fun newArray(size: Int): Array<BoostInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    private fun getFirstByteOrZero(inBytes : ByteArray?) : Int {
        return inBytes?.get(0)?.toUInt() ?: 0
    }

    private fun getFirstUByteOrZero(inBytes : ByteArray?) : Ubyte {
        return inBytes?.get(0)?.toUbyte() ?: 0.toUbyte()
    }

    fun getFeatureFlags() : FeatureFlagInfo {
        val featureFlags = io.readLocalIdentifier(0xF1, 0xFB).thenApply(::getFirstUByteOrZero).join()
        val boostEnabled = featureFlags and 2 > 0
        val octaneEnabled = featureFlags and 4 > 0
        val e85Enabled = featureFlags and 32 > 0
        return FeatureFlagInfo(boostEnabled, octaneEnabled, e85Enabled)
    }

    fun getOctaneInfo() : OctaneInfo {
        val minOctane = io.readLocalIdentifier(0xFD, 0x32).thenApply(::getFirstByteOrZero).join()
        val maxOctane = io.readLocalIdentifier(0xFD, 0x33).thenApply(::getFirstByteOrZero).join()
        val currentOctane = io.readLocalIdentifier(0xF1, 0xF9).thenApply(::getFirstByteOrZero).join()
        return OctaneInfo(minOctane, maxOctane, currentOctane)
    }

    fun getBoostInfo() : BoostInfo {
        val minBoost = io.readLocalIdentifier(0xFD, 0x30).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val maxBoost = io.readLocalIdentifier(0xFD, 0x31).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()
        val currentBoost = io.readLocalIdentifier(0xF1, 0xF8).thenApply(::getFirstUByteOrZero).thenApply(::calculateBoost).join()

        return BoostInfo(minBoost, maxBoost, currentBoost)
    }

    fun getE85Info() : E85Info {
        val currentE85 = io.readLocalIdentifier(0xF1, 0xFD).thenApply(::getFirstUByteOrZero).thenApply(::calculateE85).join();

        return E85Info(currentE85)
    }

    fun setBoostInfo(boost : Int) {
        val writeBoostByte = calculateWriteBoost(boost)
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xF8), byteArrayOf(writeBoostByte.toByte())).join()
    }

    fun setOctaneInfo(octane : Int) {
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xF9), byteArrayOf(octane.toUByte())).join()

    }

    fun setE85Info(e85 : Int) {
        val e85Byte = calculateWriteE85(e85)
        io.writeLocalIdentifier(intArrayOf(0xF1, 0xFD), byteArrayOf(e85Byte.toByte())).join()
    }

    private fun calculateWriteBoost(psi: Int) : Ubyte {
        val offsetPsi = psi + 16
        val num = (offsetPsi.toDouble() / 0.014503773773).toInt()
        val num2 = (num.toDouble() * 0.047110065099374217).toInt()
        return num2.toUbyte()
    }

    private fun calculateBoost(boost : Ubyte) : Int {
        val num = (boost.toDouble() / 0.047110065099374217).toInt()
        val num2 = (num.toDouble() * 0.014503773773).toInt()
        return num2 - 15
    }

    private fun calculateWriteE85(e85 : Int) : Ubyte {
        return (e85.toDouble() * 1.28).toUbyte()
    }

    private fun calculateE85(e85 : Ubyte) : Int {
        return (e85.toDouble() / 1.28).toInt() + 1
    }

    fun passLevel3Security() : Boolean {

        //Need to pass level3 security with the ECU to do high speed datalogging
        //We do this by sending 0x27 and the level requested (0x03). We'll get a seed
        //and we'll need to compute a key and send it back.

        val bytes = ByteArrayOutputStream()
        bytes.write(0x27)
        bytes.write(0x03)

        directIO.writeBytesBlocking(bytes.toByteArray()).thenApply {
            readBytes ->
            Log.d("SecurityCheck", "Received response from security check")
            readBytes?.drop(2)?.toByteArray()
        }.thenApply {
            response  ->
            if(response != null && response.size == 4) {

                Log.d("SecurityCheck", "Security challenge: " + byteArrayToHex(response!!))

                //The localArray is the key on the local side
                val localArray = ubyteArrayOf(0x00u, 0x00u, 0x6Du, 0x43u)

                //The responseBytes1 is the beginning of our response array
                var responseBytes1 = ubyteArrayOf(0x27u, 0x04u)
                val charset = Charsets.UTF_8

                //localSum will be the uint of the local array
                var localSum = 0u
                var shift = 24
                localArray.forEach {

                    val result = it.toUInt() shl shift
                    localSum += result
                    shift -= 8
                }

                //seedSum will be the uint of the seed array
                var seedSum = 0u
                shift = 24
                response.forEach {
                    val result = it.toUInt() shl shift
                    seedSum += result.toUInt()
                    shift -= 8
                }

                //We add them both toghether
                val challengeSum = seedSum + localSum

                //From the challengesum, we'll create an array of bytes
                var responseBytes = ByteArray(4)
                ByteBuffer.wrap(responseBytes).order(ByteOrder.BIG_ENDIAN).putInt(challengeSum.toInt())

                //And then concat the responseBytes and the challengeSum
                var responseArray: ByteArray = responseBytes1.toByteArray() + responseBytes

                Log.d("SecurityCheck", "Prepared security response:  " + byteArrayToHex(responseArray))
                return@thenApply responseArray
            }
            else{
                Log.d("SecurityCheck", "Invalid response from security controller")
                return@thenApply null

            }
        }.thenApply {
            challengeResponse ->
            if(challengeResponse != null) {
                Log.d("SecurityCheck", "Sending challenge response back")

                directIO.writeBytesBlocking(challengeResponse).thenApply { readBytes ->
                    readBytes?.drop(0)?.toByteArray()
                }.thenApply {
                    response ->
                    Log.d("setUpLogger", "Security Response: " + byteArrayToHex(response!!))

                }
            }
            else {
                Log.d("SecurityCheck", "Failed to pass security mechanism")
                byteArrayOf(0x00, 0x00, 0x00, 0x00)

            }
        }

        //Default, since I didn't actually accomplish anything yet.
        return true

    }

    fun setUpLogger() : Boolean{

        //First we need to send 0x10 0x4F
        Log.d("setUpLogger", "Connecting to the logger via Extended Diagnostics")
        val stage1 = ByteArrayOutputStream()
        stage1.write(0x10)
        stage1.write(0x4F)

        directIO.writeBytesBlocking(stage1.toByteArray()).thenApply { readBytes ->
            readBytes?.drop(0)?.toByteArray()
        }.thenApply {
            response ->
            Log.d("setUpLogger", "Initial Response: " + byteArrayToHex(response!!))
        }


        Log.d("setUpLogger", "Setting Up Level3 Security")
        //Then we need to passLevel3Security
        if(!passLevel3Security()) {
            Log.d("setUpLogger", "Security pass failed")
            return false
        }

        Log.d("setUpLogger", "Clearing DID 0xF200 with 0x2C 03 F2 00")
        //then we need to send 0x2C 0x03 0xF2 0x00
        val stage2 = ByteArrayOutputStream()
        stage2.write(0x2c)
        stage2.write(0x03)
        stage2.write(0xF2)
        stage2.write(0x00)

        directIO.writeBytesBlocking(stage2.toByteArray()).thenApply { readBytes ->
            readBytes?.drop(0)?.toByteArray()
        }.thenApply {
            response ->
            Log.d("setUpLogger", "Got Bytes back: " + byteArrayToHex(response!!))
        }


        //Then we need to send 0x2C 0x02 0xF2 0x00 0x14 (create new DID by memory address
        // at 0xF200, 14 is the max(?), followed by all of the memory addresses
        // that we want to start logging.  Each one will need to be reversed
        val stage3 = ByteArrayOutputStream()
        stage3.write(0x2C)
        stage3.write(0x02)
        stage3.write(0xF2)
        stage3.write(0x00)
        stage3.write(0x14)


        //Get array of loggable parameters
        val parameters = getParameters()

        parameters.forEach{
            stage3.write(it.address.toByteArray())
            stage3.write(byteArrayOf(it.width))
        }


        Log.d("setUpLogger", "Sending list of memory addresses: "
                    + byteArrayToHex(stage3.toByteArray()))


        directIO.writeBytesBlocking(stage3.toByteArray()).thenApply { readBytes ->
            readBytes?.drop(0)?.toByteArray()
        }.thenApply { response ->
            Log.d("setUpLogger", "Got Bytes back: " + byteArrayToHex(response!!))
        }


        Log.d("setUpLogger","preparing to query for initial data")
        val queryBytes = ubyteArrayOf(0x22u, 0xF2u, 0x00u)
        val query = ByteArrayOutputStream()
        query.write(queryBytes.toByteArray())

        while(true) {

            directIO.writeBytesBlocking(query.toByteArray()).thenApply { readBytes ->
                readBytes?.drop(3)?.toByteArray()
            }.thenApply { response ->
                Log.d("setUpLogger", "Got Bytes back: " + byteArrayToHex(response!!))

                if(writeToFile){
                    processLogBytes(response, parameters)
                }
            }

            Thread.sleep(500)
        }

        return true
    }



    private fun getParameters(): List<LoggedParameter> {

        var parameterDefinition = ParameterDefinition()

        return parameterDefinition.getParameters("sw8V0906264M")


    }

    private fun processLogBytes(raw: ByteArray, params: List<LoggedParameter>){

        //j is going to be the position in the raw data
        var j = 0
        var rawValue: ByteArray

        try {
            val fileOutPutStream = FileOutputStream(fileName)


            for(param in params){

                if(param.width.equals(0x01)) {
                    rawValue = byteArrayOf(raw[j])
              }
              else {
                  rawValue = byteArrayOf(raw[j], raw[j++])
             }

                var value = ByteBuffer.wrap(rawValue).getDouble() * param.factor
                Log.d("processLogBytes", param.name + ": " + value)


                fileOutPutStream.write(byteArrayOf(",".toByte(), value.toByte()))

              j++
            }


            fileOutPutStream.write(byteArrayOf("\n".toByte()))
            fileOutPutStream.close()


        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    public fun startWriteLogs(filename: String){

        Log.d("EurodyneIO", "Start writing to file: " + filename)
        writeToFile = true
        fileName = filename

    }

    //Helper function so we can write debug output to logcat
    private fun byteArrayToHex(input: ByteArray): String{
        var hexString = ""
        for (b in input) {
            val st = String.format("%02X", b)
            hexString += st
        }

        return hexString
    }
}