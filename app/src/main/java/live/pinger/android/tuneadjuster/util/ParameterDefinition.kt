package live.pinger.android.tuneadjuster.util

class ParameterDefinition {

    //Parameters for 8v0906264M
    var sw8V0906264M = listOf(
            LoggedParameter("Adjustable boost: Adjustable top limit", ubyteArrayOf(0xD0u, 0x01u, 0xDEu, 0x90u), 0x01, false, 17.0, "hPa"),
            LoggedParameter("Adjustable octane: Octane value", ubyteArrayOf(0xD0u, 0x01u, 0xDEu, 0x8Eu), 0x01, false, 1.0, "ron"),
            LoggedParameter("Adjustable octane: high octane basic ignition lookup", ubyteArrayOf(0xD0u, 0x01u, 0xDEu, 0x8Au), 0x01, false, 8.0, "°CRK"),
            LoggedParameter("Adjustable octane: low octane basic ignition lookup", ubyteArrayOf(0xD0u, 0x01u, 0xDEu, 0x89u), 0x01, false, 8.0, "°CRK"),
            LoggedParameter("Adjustable octane: octane interpolation factor", ubyteArrayOf(0xD0u, 0x01u, 0xDEu, 0x8Du), 0x01, false, 18.0, "[-]"),
            LoggedParameter("Ambient pressure", ubyteArrayOf(0xD0u, 0x01u, 0x3Cu, 0x76u), 0x02, false, 2.0, "hPa"),
            LoggedParameter("lambda controller output", ubyteArrayOf(0xD0u, 0x01u, 0x43u, 0x1Au), 0x02, true, 21.0, "%"),
            LoggedParameter("Fuel rail pressure", ubyteArrayOf(0xD0u, 0x01u, 0x36u, 0x12u), 0x02, false, 7.0, "hPa"),
            LoggedParameter("Actual gear", ubyteArrayOf(0xD0u, 0x00u, 0xF3u, 0x9Au), 0x01, false, 1.0, "Gear"),
            LoggedParameter("Knock retard cylinder 1", ubyteArrayOf(0xD0u, 0x00u, 0xEFu, 0xB1u), 0x01, false, 10.0, "°CRK"),
            LoggedParameter("Knock retard cylinder 2", ubyteArrayOf(0xD0u, 0x00u, 0xEFu, 0xB2u), 0x01, false, 10.0, "°CRK"),
            LoggedParameter("Knock retard cylinder 3", ubyteArrayOf(0xD0u, 0x00u, 0xEFu, 0xB3u), 0x01, false, 10.0, "°CRK"),
            LoggedParameter("Knock retard cylinder 4", ubyteArrayOf(0xD0u, 0x00u, 0xEFu, 0xB4u), 0x01, false, 10.0, "°CRK"),
            LoggedParameter("Ignition timing cylinder 1", ubyteArrayOf(0xD0u, 0x00u, 0xE5u, 0x65u), 0x01, false, 8.0, "°CRK"),
            LoggedParameter("Ignition timing cylinder 2", ubyteArrayOf(0xD0u, 0x00u, 0xE5u, 0x66u), 0x01, false, 8.0, "°CRK"),
            LoggedParameter("Ignition timing cylinder 3", ubyteArrayOf(0xD0u, 0x00u, 0xE5u, 0x67u), 0x01, false, 8.0, "°CRK"),
            LoggedParameter("Ignition timing cylinder 4", ubyteArrayOf(0xD0u, 0x00u, 0xE5u, 0x68u), 0x01, false, 8.0, "°CRK"),
            LoggedParameter("Lambda value", ubyteArrayOf(0xD0u, 0x01u, 0x20u, 0xC6u), 0x02, false, 9.0, "-"),
            LoggedParameter("Lambda setpoint", ubyteArrayOf(0xD0u, 0x01u, 0x43u, 0xF6u), 0x02, false, 9.0, "-"),
            LoggedParameter("Air mass", ubyteArrayOf(0xD0u, 0x01u, 0x3Eu, 0x42u), 0x02, false, 3.0, "mg/stk"),
            LoggedParameter("Engine speed", ubyteArrayOf(0xD0u, 0x01u, 0x24u, 0x00u), 0x02, false, 1.0, "RPM"),
            LoggedParameter("Position of boost pressure actuator", ubyteArrayOf(0xD0u, 0x01u, 0x1Du, 0x8Au), 0x02, true, 22.0, "%"),
            LoggedParameter("Position setpoint for boost pressure actuator", ubyteArrayOf(0xD0u, 0x01u, 0x1Du, 0x96u), 0x02, true, 22.0, "%"),
            LoggedParameter("Pressure upstream throttle", ubyteArrayOf(0xD0u, 0x01u, 0x3Fu, 0xAEu), 0x02, false, 2.0, "hPa"),
            LoggedParameter("Pressure before throttle set point", ubyteArrayOf(0xD0u, 0x01u, 0x1Eu, 0xEEu), 0x02, false, 2.0, "hPa"),
            LoggedParameter("Top limit of pressure upstream throttle setpoint", ubyteArrayOf(0xD0u, 0x01u, 0x1Eu, 0xFCu), 0x02, false, 2.0, "hPa"),
            LoggedParameter("Accelerator pedal", ubyteArrayOf(0xD0u, 0x01u, 0x20u, 0x22u), 0x02, false, 4.0, "%"),
            LoggedParameter("Ambient air temperature", ubyteArrayOf(0xD0u, 0x00u, 0xC1u, 0x77u), 0x01, false, 4.0, "°C"),
            LoggedParameter("Injector pulsewidth", ubyteArrayOf(0xD0u, 0x00u, 0xE6u, 0x83u), 0x01, false, 6.0, "ms"),
            LoggedParameter("Intake air temperature", ubyteArrayOf(0xD0u, 0x00u, 0xC1u, 0x79u), 0x01, false, 23.0, "°C"),
            LoggedParameter("Throttle angle", ubyteArrayOf(0xD0u, 0x01u, 0x52u, 0x2Eu), 0x02, false, 24.0, "°TPS"),
            LoggedParameter("Actual engine torque", ubyteArrayOf(0xD0u, 0x01u, 0x51u, 0x5Cu), 0x02, true, 5.0, "Nm"),
            LoggedParameter("Torque limit", ubyteArrayOf(0xD0u, 0x01u, 0x54u, 0x44u), 0x02, false, 5.0, "Nm"),
            LoggedParameter("Injector pulsewidth MPI", ubyteArrayOf(0xD0u, 0x01u, 0x38u, 0x24u), 0x02, false, 25.0, "ms"),
            LoggedParameter("Low pressure rail pressure", ubyteArrayOf(0xD0u, 0x01u, 0x1Bu, 0x26u), 0x02, false, 26.0, "hPa"),
            LoggedParameter("Low pressure fuel pump speed", ubyteArrayOf(0xD0u, 0x01u, 0x36u, 0x00u), 0x02, false, 27.0, "%"),
            LoggedParameter("Recirculation actuator position", ubyteArrayOf(0xD0u, 0x01u, 0x1Du, 0xB2u), 0x02, false, 27.0, "%"),
            LoggedParameter("Recirculation actuator setpoint", ubyteArrayOf(0xD0u, 0x01u, 0x1Eu, 0xC0u), 0x02, false, 27.0, "%")
    )

    public fun getParameters(swCode: String?): List<LoggedParameter>{
        return sw8V0906264M
    }

}