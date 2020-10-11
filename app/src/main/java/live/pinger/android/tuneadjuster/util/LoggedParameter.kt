package live.pinger.android.tuneadjuster.util

class LoggedParameter(name: String, address: UByteArray,
                      width: Byte, signed: Boolean, factor: Double,
                        units: String) {

    var name: String = ""
    var address: UByteArray = ubyteArrayOf(0x00u, 0x00u, 0x00u, 0x00u)
    var width: Byte = 0x00
    var factor: Double = 1.0


}