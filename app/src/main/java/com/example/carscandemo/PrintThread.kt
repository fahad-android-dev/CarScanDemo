package com.example.carscandemo

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.carscandemo.MainActivity.Companion.edtOffset
import com.example.carscandemo.MainActivity.Companion.mUsbDriver
import com.example.carscandemo.MainActivity.Companion.printerQueueList
import com.example.carscandemo.PrintCmd.CheckStatus5
import com.example.carscandemo.PrintCmd.GetStatus5
import com.example.carscandemo.PrintCmd.GetStatusspecialDescriptionEn
import com.example.carscandemo.UtilsTools.hexStringToBytes
import kotlinx.coroutines.*
import java.util.regex.Pattern

class PrintThread : Activity(), Runnable {

   // private var mUsbDriver: UsbDriver? = MainActivity.mUsbDriver
    private var m_blnRun: Boolean = true
    private var isCheckingStatus: Boolean = false
    private var statusCheckingJob: Job? = null // For handling the status-checking coroutine
    private val mainActivity: MainActivity = MainActivity()
   // private val handler: Handler = MainActivity.MyHandler(mainActivity)

    override fun run() {
        Looper.prepare()
        var printData: DataBean?


        try {
            while (m_blnRun) {
                if (mUsbDriver != null) {
                    while ((printerQueueList.poll().also { printData = it }) != null) {
                        when (printData?.m_iFunID) {
                            1 -> {
                                // Prioritize and process print commands

                                mUsbDriver?.write(PrintCmd.SetClean())
                                var data = "Hello"
                                mUsbDriver?.write(PrintCmd.PrintString(data,0))
                                mUsbDriver?.write(PrintCmd.PrintMarkpositioncut())
                                mUsbDriver?.write(PrintCmd.PrintCutpaper(0))

                               /* mUsbDriver?.write(PrintCmd.SetLeftmargin(50))
                                mUsbDriver?.write(PrintCmd.SetCommandmode(3))
                                mUsbDriver?.write(PrintCmd.SetReadZKmode(1))
                                val strdata = "1B 74 3F"
                                val bSendData = hexStringToBytes(strdata)
                                mUsbDriver?.write(bSendData)

                                val byteArray = byteArrayOf(
                                    251.toByte(),
                                    158.toByte(),
                                    200.toByte(),
                                    32.toByte(),
                                    32.toByte(),
                                    252.toByte(),
                                    158.toByte(),
                                    225.toByte(),
                                    195.toByte()
                                )

                                mUsbDriver?.write(byteArray)

                                mUsbDriver?.write(PrintCmd.PrintString("Dump", 0))
                                mUsbDriver?.write(PrintCmd.PrintString("أحمق", 0))

                                mUsbDriver?.write(PrintCmd.PrintFeedline(5))
                                mUsbDriver?.write(PrintCmd.PrintCutpaper(1))
*/
                            }

                            2 -> {
                                // Start status checking
                                mUsbDriver?.write(PrintCmd.SetClean())
                                mUsbDriver?.write(PrintCmd.PrintMarkposition())
                            }

                            3 -> {
                                // Stop status checking
                                mUsbDriver?.write(PrintCmd.SetClean())
                                mUsbDriver?.write(PrintCmd.PrintMarkpositioncut())
                            }
                            4 -> {
                                mUsbDriver?.write(PrintCmd.SetClean())
                                mUsbDriver?.write(PrintCmd.SetMarkoffsetprint(edtOffset.text.toString().toInt()))
                                println("here is offset ${edtOffset.text}")
                            }
                        }
                        Thread.sleep(200) // Prevent CPU overloading
                    }
                } else {
                    Thread.sleep(100) // Check periodically if mUsbDriver is null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Looper.loop()
    }


 /*   private fun startStatusChecking() {
        if (isCheckingStatus) return // Avoid multiple status-checking jobs
        isCheckingStatus = true
        statusCheckingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isCheckingStatus) {
                checkPrinterStatus()
                delay(1000) // Non-blocking delay for 1 second
            }
        }
    }*/

    private fun stopStatusChecking() {
        isCheckingStatus = false
        statusCheckingJob?.cancel() // Cancel coroutine for status checking
    }


 /*   private fun PrintStatus(): Int {
        var iResult = 1
        try {
            var iValue = -1
            val bRead1 = ByteArray(1)
            var strValue = ""
            val message = Message.obtain()
            message.what = 4
            if ((mUsbDriver?.read(bRead1, PrintCmd.GetStatus1()!!) ?: 0) > 0) {
                iValue = PrintCmd.CheckStatus1(bRead1[0])
                if (iValue != 0) {
                    strValue = PrintCmd.getStatusDescriptionEn(iValue)
                    message.obj = "$strValue CheckStatus 1 : $iValue"
                    handler.sendMessage(message)
                }
            }

            if (iValue == 0) {
                iValue = -1
                if ((mUsbDriver?.read(bRead1, PrintCmd.GetStatus2()!!) ?: 0) > 0) {
                    iValue = PrintCmd.CheckStatus2(bRead1[0])
                    if (iValue != 0) {
                        strValue = PrintCmd.getStatusDescriptionEn(iValue)
                        message.obj = "$strValue CheckStatus 2 : $iValue"
                        handler.sendMessage(message)
                    }
                }
            }

            if (iValue == 0) {
                iValue = -1
                if ((mUsbDriver?.read(bRead1, PrintCmd.GetStatus3()!!) ?: 0) > 0) {
                    iValue = PrintCmd.CheckStatus3(bRead1[0])
                    if (iValue != 0) {
                        strValue = PrintCmd.getStatusDescriptionEn(iValue)
                        message.obj = "$strValue CheckStatus 3 : $iValue"
                        handler.sendMessage(message)
                    }
                }
            }
            if (iValue == 0) {
                iValue = -1
                if ((mUsbDriver?.read(bRead1, PrintCmd.GetStatus4()!!) ?: 0) > 0) {
                    iValue = PrintCmd.CheckStatus4(bRead1[0])
                    if (iValue != 0) {
                        strValue = PrintCmd.getStatusDescriptionEn(iValue)
                        message.obj = "$strValue CheckStatus 4 : $iValue"
                        handler.sendMessage(message)
                    }
                }
            }
            if (iValue == 0) {
                iValue = -1
                if ((mUsbDriver?.read(bRead1, PrintCmd.GetStatus5()!!) ?: 0) > 0) {
                    iValue = PrintCmd.CheckStatus5(bRead1[0])  // Now passing bRead1[0] which is a Byte
                    if (iValue != 0) {
                        strValue = PrintCmd.getStatusDescriptionEn(iValue)
                        message.obj = "$strValue CheckStatus 5 : $iValue"
                        handler.sendMessage(message)
                    }
                }
            }
            if (iValue == 0) {
                strValue = PrintCmd.getStatusDescriptionEn(iValue)
                message.obj = strValue
                handler.sendMessage(message)
            }
            iResult = iValue
        } catch (e: Exception) {
            val message = Message.obtain()
            message.what = 4
            message.obj = e.message
            handler.sendMessage(message)

        }
        return iResult
    }

    private fun GetStatusspecial(): Int {
        var iResult = 1
        try {
            var iValue = -1
            val bRead1 = ByteArray(1)
            var strValue = ""
            val message = Message.obtain()
            message.what = 4

            if (mUsbDriver!!.read(bRead1, GetStatus5()!!) > 0) {
                iValue = CheckStatus5(bRead1[0])
                if (iValue != 0) {
                    strValue = GetStatusspecialDescriptionEn(iValue)
                    message.obj = strValue
                    handler.sendMessage(message)
                }
            }

            if (iValue == 0) {
                strValue = GetStatusspecialDescriptionEn(iValue)
                message.obj = strValue
                handler.sendMessage(message)
            }

            iResult = iValue
        } catch (e: java.lang.Exception) {
            val message = Message.obtain()
            message.what = 4
            message.obj = e.message
            handler.sendMessage(message)
            Log.e(TAG, "PrintSpecialStatus:" + e.message)
        }
        return iResult
    }


    private fun checkPrinterStatus(): Int {
        println("here is started printing")
        var iResult = 1
        try {
            var iValue = -1
            val bRead1 = ByteArray(1)
            val message = Message.obtain()
            message.what = 4

            // List of status check methods
            val statusCommands = listOf(
                Pair(PrintCmd::GetStatus1, PrintCmd::CheckStatus1 to PrintCmd::getStatusDescriptionEn),
                Pair(PrintCmd::GetStatus2, PrintCmd::CheckStatus2 to PrintCmd::getStatusDescriptionEn),
                Pair(PrintCmd::GetStatus3, PrintCmd::CheckStatus3 to PrintCmd::getStatusDescriptionEn),
                Pair(PrintCmd::GetStatus4, PrintCmd::CheckStatus4 to PrintCmd::getStatusDescriptionEn),
                Pair(PrintCmd::GetStatus5, PrintCmd::CheckStatus5 to ::GetStatusspecialDescriptionEn) // Include special description for GetStatus5
            )

            for ((getStatus, checkAndDescribe) in statusCommands) {
                val (checkStatus, getDescription) = checkAndDescribe
                if ((mUsbDriver?.read(bRead1, getStatus()!!) ?: 0) > 0) {
                    iValue = checkStatus(bRead1[0])
                    if (iValue != 0) {
                        val strValue = getDescription(iValue)
                        message.obj = strValue
                        handler.sendMessage(message)
                        break
                    }
                }
            }

            if (iValue == 0) {
                val strValue = PrintCmd.getStatusDescriptionEn(iValue)
                message.obj = strValue
                handler.sendMessage(message)
            }

            iResult = iValue
        } catch (e: Exception) {
            val message = Message.obtain()
            message.what = 4
            message.obj = e.message
            handler.sendMessage(message)
            Log.e(TAG, "PrinterStatusError: ${e.message}")
        }
        return iResult
    }*/

    companion object {
        fun isHexStrValid(str: String?): Boolean {
            val pattern = "^[0-9A-F]+$"
            return Pattern.compile(pattern).matcher(str).matches()
        }
    }
}
