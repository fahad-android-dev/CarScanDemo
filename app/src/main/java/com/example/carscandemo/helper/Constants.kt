package com.example.carscandemo.helper

import android.os.Build
import android.os.Environment
import java.io.File


object Constants {

    // File Config Variables
    val configFile = File(
        Environment.getExternalStorageDirectory()
            .toString() + "/Car_Parking_Entry_Scan/Config.xls"
    )

    val filePath = Environment.getExternalStorageDirectory()
        .toString() + "/Car_Parking_Entry_Scan/Config.xls"

    const val DEBUG_ON = "DebugON"
    const val GATE_NO = "GateNo"
    const val PORT = "Port"
    const val LOOP_PRESENT = "LoopPresent"
    const val MAX_DIST = "maxDist"
    const val TIME_OUT = "timeOut"
    const val GATE_RELAY_TIME_BUFFER = "GateRelayTimeBuffer"
    const val ENTRY_OR_EXIT = "EntryORExit"
    const val LED_PRESENT = "LEDPresent"
    const val DISPLAY_ON = "DisplayON"
    const val HEART_BEAT_INTERVAL = "HeartBeatInterval"
    const val AUDIO_ON = "AudioON"
    const val HEADER_DISPLAY_A = "HeaderDisplay_A"
    const val HEADER_DISPLAY_FONT_SIZE_A = "HeaderDisplayFntSize_A"
    const val HEADER_DISPLAY_E = "HeaderDisplay_E"
    const val HEADER_DISPLAY_FONT_SIZE_E = "HeaderDisplayFntSize_E"
    const val PRINTER_SIZE = "PrinterSize"
    const val PORT_NO = "PortNo"
    const val BEAT_WRITE_INTERVAL = "BeatWriteInterval"
    const val OFC_START = "OfcStart"
    const val OFC_END = "OfcSEnd"
    const val WEEK_OFF = "WeekOff"
    const val NETWORK_PRESENT = "NetworkPresent"

}

