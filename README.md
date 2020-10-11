# EurodyneAdjuster

##Currently, as of 10/10/20, logging functionality only works to logcat##
Don't bother trying to use this... it crashes and it doesn't work right.

This project was forked from Brian Ledbetter's TuneAdjuster (available in the play store and on github).
Functionality has been improved to not only support the slider implementation, but to also include the ability to
perform high speed logging (HSL) on the Simos 18 family of ECU's.

Brian's original details are below, but the additional functionality comes from the ability to define and
read dynamic identifiers via the standard UDS 0x2C routine.  This is supported in Eurodyne tunes and due to the
security implementation in order to get through 0x2C, it's unlikely to work on others.

One *major* requirement for the functionality of the HSL is that your bluetooth dongle support the extended
command set available in the OBDLink products.  That is, normal cheap $10 elm327 devices won't work - you'll need something
built on the STN11xx chipset.  The technical reason for the limitation?  The STN11xx supports large message tx (iso-tp) out
of the box, while the elm327's don't.  I could work around it, but it's not worth it.


https://play.google.com/store/apps/details?id=com.brianledbetter.tuneadjuster&hl=en_GB

What does this do?

This adjusts the Adjustable features on Eurodyne Simos18 (MQB Golf / GTI / R / Audi S3) tunes.

Eurodyne uses the standard UDS ReadLocalIdentifier (0x22) and WriteLocalIdentifier (0x2E) services to provide and store information about boost and octane settings.
No diagnostic session or security login are required for either call on a Eurodyne tune, so the code is VERY straightforward.

No guarantees are provided for any purpose and this is not supported or endorsed by Eurodyne.

I suspect if you try this without a Eurodyne tune the app will just crash as the identifier read fails, but please don't try anyway.

# Use

Pair an ELM327 Bluetooth adapter in the Android settings. The passcode for these adapters is usually 1234.

Start the app and flip the Connect toggle in the lower left. Turn your ignition on NOW before selecting the device. It's recommended but not required to do this without the engine running, although it will work even with the car in motion. Now select your ELM327 device, usually called "OBDII." If you see the sliders appear, you're set. Otherwise, try flipping the toggle again. If the app crashes, we probably couldn't connect to your ECU.

Press Back to kill the Foreground Service and exit.

# Todo

* Error handling - on any unexpected response we should just pop a dialog and kill the connection thread.
* Properly implement UDS protocol - retries and continuation messages, as well as checking for proper responses
* Add support for logging and gauge display.
