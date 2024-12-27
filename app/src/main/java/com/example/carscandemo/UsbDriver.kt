package com.example.carscandemo

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Parcelable
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class UsbDriver(private val mManager: UsbManager?, context: Context?) {
    private var mPermissionIntent: PendingIntent? = null
    private val m_Device = arrayOfNulls<UsbDevice>(MAX_DEVICE_NUM)
    private val mInterface = arrayOfNulls<UsbInterface>(MAX_DEVICE_NUM)
    private val mDeviceConnection = arrayOfNulls<UsbDeviceConnection>(
        MAX_DEVICE_NUM
    )
    private var m_UsbDevIdx = -1 //UsbDevice 下标
    private val mFTDIEndpointIN = arrayOfNulls<UsbEndpoint>(MAX_DEVICE_NUM)
    private val mFTDIEndpointOUT = arrayOfNulls<UsbEndpoint>(MAX_DEVICE_NUM)
    private var m_iWaitTime = 3000

    //增加日志记录
    private var m_strLog_Path = "" // 日志文件在sdcard中的路径，空表示不记录日志
    private var syncLock = false

    @Synchronized
    fun lock() {
        while (syncLock == true) {
            try {
                (this as Object).wait()
            } catch (e: Exception) {
                //	Debug.warning(e);
            }
        }
        syncLock = true
    }

    @Synchronized
    fun unlock() {
        syncLock = false
        (this as Object).notifyAll()
    }

    //设置超时时间
    fun setFlowCtrl(iFlowCtrlFlag: Int) {
        m_iWaitTime = if (iFlowCtrlFlag == 0) {
            2000
        } else {
            0
        }
    }

    fun setPermissionIntent(pi: PendingIntent?) {
        mPermissionIntent = pi
    }

    // when insert the device USB plug into a USB port
    fun usbAttached(intent: Intent): Boolean {
        val usbDev = intent
            .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
        return usbAttached(usbDev)
    }

    fun usbAttached(usbDev: UsbDevice?): Boolean {
        m_UsbDevIdx = getUsbDevIndex(usbDev)
        m_Device[m_UsbDevIdx] = usbDev
        if (m_UsbDevIdx < 0) {
            Log.i(TAG, "Not support device : " + usbDev.toString())
            return false
        }


        //是否有权限
        if (mManager!!.hasPermission(m_Device[m_UsbDevIdx])) {
            return true
        } else {
            //请求权限
            mManager.requestPermission(m_Device[m_UsbDevIdx], mPermissionIntent)
            return false
        }
    }

    fun openUsbDevice(): Boolean {
        if (m_UsbDevIdx < 0) {
            for (device in mManager!!.deviceList.values) {
                //	Log.i(TAG, "Devices : " + device.toString());

                m_UsbDevIdx = getUsbDevIndex(device)
                if (m_UsbDevIdx >= 0) {
                    m_Device[m_UsbDevIdx] = device
                    break
                }
            }
        }

        if (m_UsbDevIdx < 0) return false
        return openUsbDevice(m_Device[m_UsbDevIdx])
    }

    fun openUsbDevice(usbDev: UsbDevice?): Boolean {
        m_UsbDevIdx = getUsbDevIndex(usbDev)
        if (m_UsbDevIdx < 0) return false


        //获取设备接口
        var iIndex = 0
        val iCount = m_Device[m_UsbDevIdx]!!.interfaceCount

        //Log.i(TAG, " m_Device[m_UsbDevIdx].getInterfaceCount():"+  iCount);

        //android5.X下面,当后接鼠标等USB设备时,getInterfaceCount会出现为0的情况,2017/05/19
        if (iCount == 0) return false

        iIndex = 0
        while (iIndex < iCount) {
            // 一般来说一个设备都是一个接口，可以通过getInterfaceCount()查看接口的个数
            // 这个接口上有两个端点，分别对应OUT 和 IN
            mInterface[m_UsbDevIdx] = m_Device[m_UsbDevIdx]!!.getInterface(iIndex)
            break
            iIndex++
        }


        //用UsbDeviceConnection 与 UsbInterface 进行端点设置和通讯
        if (mInterface[m_UsbDevIdx]!!.getEndpoint(1) != null) {
            mFTDIEndpointOUT[m_UsbDevIdx] = mInterface[m_UsbDevIdx]!!.getEndpoint(1)
        }
        if (mInterface[m_UsbDevIdx]!!.getEndpoint(0) != null) {
            mFTDIEndpointIN[m_UsbDevIdx] = mInterface[m_UsbDevIdx]!!.getEndpoint(0)
        }

        mDeviceConnection[m_UsbDevIdx] = mManager!!.openDevice(m_Device[m_UsbDevIdx])
        if (mDeviceConnection[m_UsbDevIdx] == null) return false

        if (mDeviceConnection[m_UsbDevIdx]!!.claimInterface(mInterface[m_UsbDevIdx], true)) {
            return true
        } else {
            mDeviceConnection[m_UsbDevIdx]!!.close()
            return false
        }
    }

    /**
     * Gets an USB permission if no permission
     *
     * @param device
     * @see setPermissionIntent
     */
    fun getPermission(device: UsbDevice?) {
        if (device != null && mPermissionIntent != null) {
            //Log.i(TAG, "------设置权限1--------");
            if (!mManager!!.hasPermission(device)) {
                //	Log.i(TAG, "------设置权限2--------");
                mManager.requestPermission(device, mPermissionIntent)
            }
        }
    }

    // Close the device
    fun closeUsbDevice() {
        if (m_UsbDevIdx < 0) return

        closeUsbDevice(m_Device[m_UsbDevIdx])
    }

    // Close the device
    fun closeUsbDevice(usbDev: UsbDevice?): Boolean {
        try {
            m_UsbDevIdx = getUsbDevIndex(usbDev)
            if (m_UsbDevIdx < 0) return false
            if (mDeviceConnection[m_UsbDevIdx] != null) {
                if (mInterface[m_UsbDevIdx] != null) {
                    mDeviceConnection[m_UsbDevIdx]!!.releaseInterface(mInterface[m_UsbDevIdx])
                    mInterface[m_UsbDevIdx] = null
                    mDeviceConnection[m_UsbDevIdx]!!.close()
                    mDeviceConnection[m_UsbDevIdx] = null
                    m_Device[m_UsbDevIdx] = null
                    mFTDIEndpointIN[m_UsbDevIdx] = null
                    mFTDIEndpointOUT[m_UsbDevIdx] = null
                }
            }
        } catch (e: Exception) {
            Log.i(TAG, "closeUsbDevice exception: " + e.message.toString())
        }
        return true
    }

    // when remove the device USB plug from a USB port
    fun usbDetached(intent: Intent): Boolean {
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
        return closeUsbDevice(device)
    }

    @JvmOverloads
    fun write(buf: ByteArray?, length: Int = buf?.size ?: 0): Int {
        if (m_UsbDevIdx < 0) return -1

        return write(buf, buf?.size ?: 0, m_Device[m_UsbDevIdx])
    }

    // Read Binary Data
    fun read(bufRead: ByteArray, bufWrite: ByteArray): Int {
        if (m_UsbDevIdx < 0) return -1

        return read(bufRead, bufWrite, m_Device[m_UsbDevIdx])
    }

    fun write(buf: ByteArray, usbDev: UsbDevice?): Int {
        return write(buf, buf.size, usbDev)
    }

    fun write(buf: ByteArray?, length: Int, usbDev: UsbDevice?): Int {
        m_UsbDevIdx = getUsbDevIndex(usbDev)
        if (m_UsbDevIdx < 0) return -1
        lock()
        var offset = 0
        var actual_length: Int
        try {
            val write_buf = ByteArray(WRITEBUF_SIZE)

            while (offset < length) {
                var write_size = WRITEBUF_SIZE

                if (offset + write_size > length) {
                    write_size = length - offset
                }
                System.arraycopy(buf, offset, write_buf, 0, write_size)
                actual_length = mDeviceConnection[m_UsbDevIdx]!!.bulkTransfer(
                    mFTDIEndpointIN[m_UsbDevIdx], write_buf, write_size, m_iWaitTime
                )

                //Log.i(TAG, "-----Length--------" + String.valueOf(actual_length));
                if (actual_length < 0) {
                    unlock()
                    return -1
                }
                if (m_strLog_Path != "") {
                    var str1 = ""
                    var str2 = ""
                    for (i1 in 0 until actual_length) {
                        str2 = String.format("%02X", write_buf[i1])
                        str1 = "$str1$str2 "
                    }
                    writeLogtoFile("write", "Length=$actual_length;Data=[$str1]")
                }

                offset += actual_length
            }
        } catch (e: Exception) {
        }

        unlock()
        return offset
    }

    fun read(bufRead: ByteArray, bufWrite: ByteArray, usbDev: UsbDevice?): Int {
        if (write(bufWrite, bufWrite.size, usbDev) < 0) return -1
        var len = 0
        lock()
        try {
            try {
                Thread.sleep(50)
                if (bufRead.size > 10) Thread.sleep(150)
            } catch (e: InterruptedException) {
                // TODO Auto-generated catch block
                //Log.i(TAG, e);
            }

            len = mDeviceConnection[m_UsbDevIdx]!!.bulkTransfer(
                mFTDIEndpointOUT[m_UsbDevIdx],
                bufRead, bufRead.size, 300
            ) // RX
            if (len == 0) len = mDeviceConnection[m_UsbDevIdx]!!.bulkTransfer(
                mFTDIEndpointOUT[m_UsbDevIdx],
                bufRead, bufRead.size, 300
            ) // RX

            //Log.i(TAG,"mFTDIEndpointOUT:"+len);
        } catch (e: Exception) {
        }
        unlock()
        return len
    }

    // 获取设备返回的对应设备下标
    private fun getUsbDevIndex(usbDev: UsbDevice?): Int {
        try {
            if (usbDev == null) return -1
            if ((usbDev.productId == 0x2013 && usbDev.vendorId == 0x519)) {
                return 0
            } else if ((usbDev.productId == 0x2015 && usbDev.vendorId == 0x519)) {
                return 1
            }
        } catch (e: Exception) {
            Log.i(TAG, "getUsbDevIndex exception: " + e.message.toString())
        }

        //Log.i(TAG, "Not support device : " + usbDev.toString());
        return -1
    }

    val isUsbPermission: Boolean
        get() {
            var blnRes = false
            try {
                if (m_UsbDevIdx < 0) return false

                if (mManager != null) blnRes = mManager.hasPermission(m_Device[m_UsbDevIdx])
            } catch (e: Exception) {
            }
            return blnRes
        }

    val isConnected: Boolean
        get() {
            if (m_UsbDevIdx < 0) return false

            return if (m_Device[m_UsbDevIdx] != null && mFTDIEndpointIN[m_UsbDevIdx] != null && mFTDIEndpointOUT[m_UsbDevIdx] != null) {
                true
            } else {
                false
            }
        }

    fun SetLogPath(strValue: String) {
        m_strLog_Path = strValue
    }

    private fun writeLogtoFile(tag: String, text: String) {
        if (m_strLog_Path == "") return

        val nowtime = Date()
        val needWriteFiel = logfile.format(nowtime)
        val needWriteMessage = LogSdf.format(nowtime) + " " + tag + " " + text
        try {
            val logdir = File(m_strLog_Path) // 如果没有log文件夹则新建该文件夹
            if (!logdir.exists()) {
                logdir.mkdirs()
            }
            val file = File(m_strLog_Path, "PrintSdk$needWriteFiel.log")

            val filerWriter = FileWriter(file, true) // 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            val bufWriter = BufferedWriter(filerWriter)
            bufWriter.write(needWriteMessage)
            bufWriter.newLine()
            bufWriter.close()
            filerWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val MAX_DEVICE_NUM: Int = 2 //支持多台打印机数量
        private const val TAG = "UsbDriver"
        const val WRITEBUF_SIZE: Int = 4096

        @SuppressLint("SdCardPath", "SimpleDateFormat")
        private val LogSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // 日志的输出格式
        private val logfile = SimpleDateFormat("yyyyMMdd") // 日志文件格式
    }
}
