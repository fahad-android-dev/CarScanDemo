package com.example.carscandemo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.carscandemo.databinding.ActivityMainBinding
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var usbPort: UsbSerialPort? = null
    private lateinit var usbManager: UsbManager
    private lateinit var handler: Handler
    private var isReading = false

    companion object {
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION" // Define the constant here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        usbManager = getSystemService(USB_SERVICE) as UsbManager
        handler = Handler(Looper.getMainLooper())

        // Register the receiver to listen for USB device detachment
        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)

        // Detect the connected USB serial device
        val driver = findUsbSerialDriver(usbManager)
        driver?.let {
            val connection = usbManager.openDevice(driver.device)
            usbPort = driver.ports[0] // Assuming first port is used
            try {
                usbPort?.open(connection)
                usbPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                Toast.makeText(this, "USB Serial Opened", Toast.LENGTH_SHORT).show()

                // Start reading data periodically every 1 second
                startReadingData()

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error opening USB", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "No USB device found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findUsbSerialDriver(usbManager: UsbManager): UsbSerialDriver? {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        return if (availableDrivers.isNotEmpty()) availableDrivers[0] else null
    }

    // Start reading data from the USB device every 1 second
    private fun startReadingData() {
        if (!isReading) {
            isReading = true
            handler.postDelayed(object : Runnable {
                override fun run() {
                    val receivedData = receiveData()
                    receivedData?.let {
                        // Process the received data
                        processReceivedData(it)
                    }
                    if (isReading) {
                        handler.postDelayed(this, 1000)  // Repeat every 1 second
                    }
                }
            }, 1000)
        }
    }

    // Read data from the USB device
    private fun receiveData(): ByteArray? {
        val buffer = ByteArray(64)  // Adjust buffer size as needed
        return try {
            val bytesRead = usbPort?.read(buffer, 1000)
            if (bytesRead != null && bytesRead > 0) {
                buffer.copyOf(bytesRead)
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Process the received data
    private fun processReceivedData(data: ByteArray) {
        runOnUiThread {
            binding.txtDataSize.text = data.size.toString()
        }

      //  val startByte = data[0].toInt() and 0xFF // Convert to unsigned
      //  val deviceType = data[1].toInt() and 0xFF // Convert to unsigned
      //  val command = data[3].toInt() and 0xFF // Convert to unsigned

        // Convert byte array to a human-readable string (e.g., hex or decimal values)
        val dataString = data.joinToString(separator = " ") { byte -> byte.toInt().and(0xFF).toString() } // Decimal values

       // println("startByte :::: $startByte")
      //  println("deviceType :::: $deviceType")
       // println("command :::: $command")
        println("Received Data: $dataString")

        runOnUiThread {
            binding.txtCardValue.text = dataString
        }
    }

    // Write data to the USB device
    private fun sendData(data: String) {
        try {
            val buffer = data.toByteArray()
            usbPort?.write(buffer, 1000)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Handle USB device detachment
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                // Handle USB device detachment
                stopReadingData() // Stop reading when USB is detached
                usbPort?.close()
                Toast.makeText(context, "USB Device Detached", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Request permission for USB device
    private fun requestUsbPermission(usbDevice: UsbDevice) {
        if (!usbManager.hasPermission(usbDevice)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION), // Use the constant here
                PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(usbDevice, permissionIntent)
        }
    }

    // Stop reading data from USB when the activity is paused or destroyed
    private fun stopReadingData() {
        isReading = false
        handler.removeCallbacksAndMessages(null) // Remove any pending posts
    }

    override fun onPause() {
        super.onPause()
        try {
            stopReadingData()  // Stop reading data when the activity is paused
            usbPort?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            stopReadingData()  // Stop reading data when the activity is destroyed
            usbPort?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check for device when the app resumes
        val driver = findUsbSerialDriver(usbManager)
        driver?.let {
            val connection = usbManager.openDevice(driver.device)
            usbPort = driver.ports[0] // Assuming first port is used
            try {
                usbPort?.open(connection)
                usbPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                Toast.makeText(this, "USB Serial Opened", Toast.LENGTH_SHORT).show()

                // Restart reading data when the app resumes
                startReadingData()

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error opening USB", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
