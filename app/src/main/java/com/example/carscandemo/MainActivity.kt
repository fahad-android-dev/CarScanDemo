package com.example.carscandemo

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.carscandemo.databinding.ActivityMainBinding
import com.example.carscandemo.helper.BaseActivity

import com.physicaloid.lib.Physicaloid
import com.physicaloid.lib.usb.driver.uart.UartConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Queue


class MainActivity : BaseActivity() {
    private val handler = Handler()
    private var serial: Physicaloid? = null
    private var stop = false
    private var runningMainLoop = false
    var dataBean: DataBean = DataBean(0, "")
    private val timeHandler = Handler(Looper.getMainLooper())
    private val isExternalStorageAvailable: Boolean = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    private var cardNumber = 0

    var mUsbDevice: UsbDevice? = null

    private val loop = Runnable {
        val rbuf = ByteArray(4096) // Buffer for incoming data
        while (true) {
            val len = serial?.read(rbuf) ?: 0

            println("len :::: $len")
            if (len > 0) {
                val receivedData = rbuf.copyOfRange(0, len)
                println("receivedData :::: $receivedData")
                processReceivedData(receivedData)
            }

            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (stop) {
                runningMainLoop = false
                return@Runnable
            }
        }
    }



    var usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    mUsbDriver?.closeUsbDevice(device)
                    stop = true
                    serial?.close()
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    println("USB device attached")
                    if (serial?.isOpened == false) {
                        println("Connecting to USB serial")
                        openUsbSerial()
                    }
                }

                ACTION_USB_PERMISSION -> synchronized(this) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        println("Permission granted for USB device")
                        if (serial?.isOpened == false) {
                            openUsbSerial()
                        }
                    } else {
                        showMessage("Permission denied for device")
                    }

                    if (!runningMainLoop) {
                        mainloop()
                    }
                }
            }
        }
    }

    /*var usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if ((device?.productId == 8211 && device.vendorId == 1305) ||
                        (device?.productId == 8213 && device.vendorId == 1305)
                    ) {
                        mUsbDriver?.closeUsbDevice(device)
                        stop = true
                        serial?.close()
                    }else {
                        stop = true
                        serial?.close()
                    }
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    println("here is attached")
                    if (serial?.isOpened == false) {
                        println("here is serial connecting")
                        openUsbSerial()
                    }
                }

                ACTION_USB_PERMISSION -> synchronized(this) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if ((device?.productId == 8211 && device.vendorId == 1305) ||
                            (device?.productId == 8213 && device.vendorId == 1305)
                        ) {
                            println("here is serial connecting 1111")
                            if (serial?.isOpened == false) {
                                openUsbSerial()
                            }
                        }else {
                            println("here is serial connecting 222")
                            if (serial?.isOpened == false) {
                                openUsbSerial()
                            }
                        }
                    } else {
                        showMessage("Permission denied for device")
                    }
                    if (!runningMainLoop) {
                        mainloop()
                    }
                }
            }
        }
    }*/


    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        edtOffset = binding.edtOffset

        /*if (!Settings.canDrawOverlays(applicationContext)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }*/

        // for hiding systems buttons and status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For API 30+
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        else {
            // Fallback for older versions
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }

        mUsbDriver = UsbDriver(getSystemService(USB_SERVICE) as UsbManager?, this)

        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE)
        mUsbDriver?.setPermissionIntent(permissionIntent)
        serial = Physicaloid(this)


        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        this.registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        Thread(PrintThread()).start()


        initializeUsbConnection()
        onClickListeners()
    }


    private fun onClickListeners(){
        binding.txtCard.setOnClickListener {
            var iDriverCheck: Int = 0
            iDriverCheck = usbDriverCheck()
            when (iDriverCheck) {
                -1 -> {
                    Toast.makeText(this, "Printer not connected!", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    Toast.makeText(this, "Printer unauthorized!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    dataBean.m_iFunID = 1
                    printerQueueList.add(dataBean)
                }
            }

        }

        binding.btnSetOffset.setOnClickListener {
            var iDriverCheck: Int = 0
            iDriverCheck = usbDriverCheck()
            when (iDriverCheck) {
                -1 -> {
                    Toast.makeText(this, "Printer not connected!", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    Toast.makeText(this, "Printer unauthorized!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    dataBean.m_iFunID = 4
                    printerQueueList.add(dataBean)
                }
            }

        }

        binding.btnPosition.setOnClickListener {
            var iDriverCheck: Int = 0
            iDriverCheck = usbDriverCheck()
            when (iDriverCheck) {
                -1 -> {
                    Toast.makeText(this, "Printer not connected!", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    Toast.makeText(this, "Printer unauthorized!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    dataBean.m_iFunID = 2
                    printerQueueList.add(dataBean)
                }
            }

        }

        binding.btnPositionCut.setOnClickListener {
            var iDriverCheck: Int = 0
            iDriverCheck = usbDriverCheck()
            when (iDriverCheck) {
                -1 -> {
                    Toast.makeText(this, "Printer not connected!", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    Toast.makeText(this, "Printer unauthorized!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    dataBean.m_iFunID = 3
                    printerQueueList.add(dataBean)
                }
            }

        }
    }




    private fun processReceivedData(data: ByteArray) {
        if (data.size < 6) return // Ensure the data has the expected length
        var iDriverCheck: Int = 0

        val startByte = data[0].toInt() and 0xFF // Convert to unsigned
        val deviceType = data[1].toInt() and 0xFF // Convert to unsigned
        val command = data[3].toInt() and 0xFF // Convert to unsigned

// Convert byte array to a human-readable string (e.g., hex or decimal values)
        val dataString = data.joinToString(separator = " ") { byte -> byte.toInt().and(0xFF).toString() } // Decimal values
// val dataString = data.joinToString(separator = " ") { byte -> String.format("%02X", byte) } // Hexadecimal values

        println("startByte :::: $startByte")
        println("deviceType :::: $deviceType")
        println("command :::: $command")
        println("Received Data: $dataString")

        runOnUiThread {
            binding.txtCardValue.text = dataString
        }

        /*iDriverCheck = usbDriverCheck()
        if (iDriverCheck == -1) {
            showMessage("Printer not connected!")
            return
        }

        if (iDriverCheck == 1) {
            showMessage("Printer unauthorized!")
            return
        }

        // Validate start byte
        if (startByte == 250) {
            when (deviceType) {
                9 -> { // Loop Detect
                    when (command) {
                        100 -> {

                        }
                        200 -> {

                        }
                    }
                }

                10 -> { // Push Button

                }

                11 -> { // Card Scan
                    val command1 = data[4].toInt() and 0xFF // Convert to unsigned
                    val command2 = data[5].toInt() and 0xFF // Convert to unsigned

                    val one = command * 65536
                    val two = command1 * 256
                    cardNumber = one + two + command2
                }
            }
        }*/
    }

    private fun initializeUsbConnection() {
        if (serial?.isOpened == false) {
            println("here is serial connecting")
            openUsbSerial()
        }
    }

    private fun sendRelayCommand(port: Int, action: Int) {
        if (serial != null && serial!!.isOpened) {
            val relayCommand = byteArrayOf(
                250.toByte(), // Start Byte
                port.toByte(), // Port number
                0, // Reserved
                action.toByte(), // Action (0=close, 3=trigger)
                251.toByte(), // End Byte
                13.toByte() // Terminating Byte
            )
            serial?.write(relayCommand, relayCommand.size)
        } else {
            handler.post {
                Toast.makeText(
                    this,
                    "Failed to send relay command. Serial not open.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openUsbSerial() {
        println("here is serial is searching")
        if (serial == null) {
            Toast.makeText(this, "Cannot open USB serial", Toast.LENGTH_SHORT).show()
            return
        }

        if (!serial!!.isOpened) {
            if (!serial!!.open()) {
                Toast.makeText(this, "Cannot open USB serial", Toast.LENGTH_SHORT).show()
                return
            } else {
                serial!!.setConfig(UartConfig(9600, 8, 1, 0, false, false))
                Toast.makeText(this, "Connected to USB serial", Toast.LENGTH_SHORT).show()
            }
        }

        if (!runningMainLoop) {
            mainloop()
        }
    }

    private fun mainloop() {
        stop = false
        runningMainLoop = true
        Toast.makeText(this, "USB Serial connected", Toast.LENGTH_SHORT).show()
        Thread(loop).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        serial?.close()
        stop = true
        unregisterReceiver(usbReceiver)
        timeHandler.removeCallbacksAndMessages(null)
    }

    private fun usbDriverCheck(): Int {
        var iResult = -1
        try {
            if (mUsbDriver?.isUsbPermission != true) {
                val manager = getSystemService(USB_SERVICE) as UsbManager
                val deviceList = manager.deviceList
                mUsbDevice = null
                val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
                while (deviceIterator.hasNext()) {
                    val device = deviceIterator.next()
                    if ((device.productId == 8211 && device.vendorId == 1305)
                        || (device.productId == 8213 && device.vendorId == 1305)
                    ) {
                        mUsbDevice = device
                    }
                }
                if (mUsbDevice != null) {
                    iResult = 1
                    if (mUsbDriver?.usbAttached(mUsbDevice) == true) {
                        if (mUsbDriver?.openUsbDevice(mUsbDevice) == true) iResult = 0
                    }
                }
            } else {
                if (mUsbDriver?.isConnected != true) {
                    if (mUsbDriver?.openUsbDevice(mUsbDevice) == true) iResult = 0
                } else {
                    iResult = 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "usbDriverCheck:" + e.message)
        }

        return iResult
    }



    class MyHandler(private val activity: Activity) : Handler() {

        override fun handleMessage(msg: Message) {
            // Use the activity reference directly
            val arg1 = msg.arg1 // Get message attributes
            val arg2 = msg.arg2
            val what = msg.what
            val result = msg.obj

            when (what) {
                0 -> {}
                3, 4 -> {
                    showMessage(result.toString())

                }
                else -> {}
            }
        }
    }

    companion object {
        private lateinit var binding: ActivityMainBinding
        var mUsbDriver: UsbDriver? = null
        var printerQueueList: Queue<DataBean> = LinkedList()
        private const val ACTION_USB_PERMISSION = "com.example.carscanentry.ACTION_USB_PERMISSION"
        var edtLogs: EditText? = null
        lateinit var edtOffset : EditText


        fun showMessage(sMsg: String?) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    edtLogs?.setText(sMsg)
                }
            }
        }


    }

}
