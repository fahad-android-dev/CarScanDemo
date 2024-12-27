package com.example.carscandemo

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ThumbnailUtils
import android.util.Base64
import android.util.Log
import com.example.carscandemo.PrintCmd.PrintDiskImagefile
import org.apache.http.util.EncodingUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import javax.xml.transform.OutputKeys

object UtilsTools {
    private const val hexString = "0123456789ABCDEF"
    fun convertToBlackWhite(bmp: Bitmap?): Bitmap {
        println(bmp!!.config)

        println("here is width " + bmp.width)
        var width = bmp.width
        val height = bmp.height
        if (width > 640) {
            width = 640
        }
        //        if (height > 2000){
//            return null;
//        }
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = IntArray(height * width)
        try {
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val grey = pixels[width * i + j]
                    val red = ((grey and 0x00FF0000) shr 16)
                    gray[width * i + j] = red
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "PrintBmp:" + e.message)
        }

        var e = 0
        for (i in 0 until height) {
            for (j in 0 until width) {
                val g = gray[width * i + j]
                if (g >= 128) {
                    pixels[width * i + j] = -0x1
                    e = g - 255
                } else {
                    pixels[width * i + j] = -0x1000000
                    e = g - 0
                }
                if (j < width - 1 && i < height - 1) {
                    gray[width * i + j + 1] += 3 * e / 8
                    gray[width * (i + 1) + j] += 3 * e / 8
                    gray[width * (i + 1) + j + 1] += e / 4
                } else if (j == width - 1 && i < height - 1) {
                    gray[width * (i + 1) + j] += 3 * e / 8
                } else if (j < width - 1 && i == height - 1) {
                    gray[width * (i) + j + 1] += e / 4
                }
            }
        }

        val newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        println("here is bitmap 1 created $newBmp")

        newBmp.setPixels(pixels, 0, width, 0, 0, width, height)

        val resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height)

        println("here is resizeBmp width " + resizeBmp.width)
        return resizeBmp
    }


    fun convertToGrayscale(bmp: Bitmap): Bitmap {
        val width = bmp.width
        val height = bmp.height
        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (i in 0 until height) {
            for (j in 0 until width) {
                val pixel = bmp.getPixel(j, i)
                val red = (pixel shr 16) and 0xff
                val green = (pixel shr 8) and 0xff
                val blue = pixel and 0xff

                // Calculate the grayscale value
                val gray = (red + green + blue) / 3 // Simple average method

                // Alternatively, you can use luminosity method
                // int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                // Set the pixel in the new bitmap
                grayBitmap.setPixel(j, i, (0xff shl 24) or (gray shl 16) or (gray shl 8) or gray)
            }
        }

        return grayBitmap
    }


    fun convertToMonochrome(original: Bitmap): Bitmap {
        val monochrome =
            Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)

        for (x in 0 until original.width) {
            for (y in 0 until original.height) {
                val pixel = original.getPixel(x, y)
                val gray =
                    ((0.299 * ((pixel shr 16) and 0xff)) + (0.587 * ((pixel shr 8) and 0xff)) + (0.114 * (pixel and 0xff))).toInt()
                monochrome.setPixel(x, y, if (gray < 128) -0x1000000 else -0x1) // Black or white
            }
        }

        return monochrome
    }


    /*public static Bitmap convertToBlackWhite(Bitmap bmp) {
        System.out.println(bmp.getConfig());

        System.out.println("here is width " + bmp.getWidth());
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        // Limit the width if greater than 640
        if (width > 640) {
            width = 640;
        }

        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];

        // Apply contrast factor
        float contrastFactor = 1.8f;  // Adjust this to control contrast level (higher for more contrast)

        try {
            // Convert image to grayscale and apply contrast adjustment
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int grey = pixels[width * i + j];
                    int red = ((grey & 0x00FF0000) >> 16);  // Extract red component

                    // Apply contrast adjustment
                    red = (int) (contrastFactor * (red - 128) + 128);
                    red = Math.min(255, Math.max(0, red));  // Clamp the value between 0 and 255

                    gray[width * i + j] = red;  // Assign adjusted grayscale value
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "PrintBmp:" + e.getMessage());
        }

        int e = 0;
        // Error diffusion dithering (Floyd-Steinberg)
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];

                // Apply thresholding to determine black or white
                if (g >= 75) {
                    pixels[width * i + j] = 0xffffffff;  // Set to white
                    e = g - 255;
                } else {
                    pixels[width * i + j] = 0xff000000;  // Set to black
                    e = g;
                }

                // Distribute the error to neighboring pixels
                if (j < width - 1 && i < height - 1) {
                    gray[width * i + j + 1] += (7 * e) / 16; // Increase error spread
                    gray[width * (i + 1) + j] += (5 * e) / 16;
                    gray[width * (i + 1) + j + 1] += (3 * e) / 16;
                } else if (j == width - 1 && i < height - 1) {
                    gray[width * (i + 1) + j] += 3 * e / 8;
                } else if (j < width - 1 && i == height - 1) {
                    gray[width * i + j + 1] += e / 4;
                }
            }
        }

        // Create new monochrome bitmap
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        System.out.println("here is bitmap 1 created " + newBmp);

        // Set modified pixels to the new bitmap
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        // Resize if necessary
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);

        System.out.println("here is resizeBmp width " + resizeBmp.getWidth());
        return resizeBmp;
    }*/
    fun saveBmpFile(bitmap: Bitmap?, strFileName: String?): Int {
        //String filename = "/storage/emulated/0/Music/test.bmp";
        var iResult = -1
        if (bitmap == null) return iResult
        val nBmpWidth = bitmap.width
        val nBmpHeight = bitmap.height
        val bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4)
        try {
            val file = File(strFileName)
            if (!file.exists()) {
                file.createNewFile()
            }
            val fileos = FileOutputStream(strFileName)
            val bfType = 0x4d42
            val bfSize = (14 + 40 + bufferSize).toLong()
            val bfReserved1 = 0
            val bfReserved2 = 0
            val bfOffBits = (14 + 40).toLong()
            writeWord(fileos, bfType)
            writeDword(fileos, bfSize)
            writeWord(fileos, bfReserved1)
            writeWord(fileos, bfReserved2)
            writeDword(fileos, bfOffBits)
            val biSize = 40L
            val biWidth = nBmpWidth.toLong()
            val biHeight = nBmpHeight.toLong()
            val biPlanes = 1
            val biBitCount = 24
            val biCompression = 0L
            val biSizeImage = 0L
            val biXpelsPerMeter = 0L
            val biYPelsPerMeter = 0L
            val biClrUsed = 0L
            val biClrImportant = 0L
            writeDword(fileos, biSize)
            writeLong(fileos, biWidth)
            writeLong(fileos, biHeight)
            writeWord(fileos, biPlanes)
            writeWord(fileos, biBitCount)
            writeDword(fileos, biCompression)
            writeDword(fileos, biSizeImage)
            writeLong(fileos, biXpelsPerMeter)
            writeLong(fileos, biYPelsPerMeter)
            writeDword(fileos, biClrUsed)
            writeDword(fileos, biClrImportant)
            val bmpData = ByteArray(bufferSize)
            val wWidth = (nBmpWidth * 3 + nBmpWidth % 4)
            var nCol = 0
            var nRealCol = nBmpHeight - 1
            while (nCol < nBmpHeight) {
                var wRow = 0
                var wByteIdex = 0
                while (wRow < nBmpWidth) {
                    val clr = bitmap.getPixel(wRow, nCol)
                    bmpData[nRealCol * wWidth + wByteIdex] = Color.blue(clr).toByte()
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = Color.green(clr).toByte()
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = Color.red(clr).toByte()
                    wRow++
                    wByteIdex += 3
                }
                ++nCol
                --nRealCol
            }
            fileos.write(bmpData)
            fileos.flush()
            fileos.close()
            iResult = 0
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return iResult
    }

    @Throws(IOException::class)
    internal fun writeWord(stream: FileOutputStream, value: Int) {
        val b = ByteArray(2)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        stream.write(b)
    }

    @Throws(IOException::class)
    internal fun writeDword(stream: FileOutputStream, value: Long) {
        val b = ByteArray(4)
        b[0] = (value and 0xffL).toByte()
        b[1] = (value shr 8 and 0xffL).toByte()
        b[2] = (value shr 16 and 0xffL).toByte()
        b[3] = (value shr 24 and 0xffL).toByte()
        stream.write(b)
    }

    @Throws(IOException::class)
    internal fun writeLong(stream: FileOutputStream, value: Long) {
        val b = ByteArray(4)
        b[0] = (value and 0xffL).toByte()
        b[1] = (value shr 8 and 0xffL).toByte()
        b[2] = (value shr 16 and 0xffL).toByte()
        b[3] = (value shr 24 and 0xffL).toByte()
        stream.write(b)
    }

    fun readTxt(path: String?): String {
        var str = ""
        try {
            val urlFile = File(path)
            val isr = InputStreamReader(FileInputStream(urlFile), "UTF-8")
            val br = BufferedReader(isr)

            var mimeTypeLine: String? = null
            while ((br.readLine().also { mimeTypeLine = it }) != null) {
                str = str + mimeTypeLine
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return str
    }

    fun encodeCN(data: String): String {
        val bytes: ByteArray
        try {
            bytes = data.toByteArray(charset("gbk"))
            val sb = StringBuilder(bytes.size * 2)
            for (i in bytes.indices) {
                sb.append(hexString[bytes[i].toInt() and 0xf0 shr 4])
                sb.append(hexString[bytes[i].toInt() and 0x0f shr 0])
                // sb.append(" ");
            }
            return sb.toString()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return ""
    }

    fun encodeStr(data: String): String {
        var result = ""
        val b: ByteArray
        try {
            b = data.toByteArray(charset("gbk"))
            for (i in b.indices) {
                result += ((b[i].toInt() and 0xff) + 0x100).toString(16).substring(1)
                // result += " ";
            }
            return result
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return ""
    }

    fun isCN(data: String): Boolean {
        var flag = false
        val regex = "^[\u4e00-\u9fa5]*$"
        //		String regex = "^[һ-��]*$";
        if (data.matches(regex.toRegex())) {
            flag = true
        }
        return flag
    }

    //获取文件后缀名
    fun getExtensionName(filename: String?): String? {
        if ((filename != null) && (filename.length > 0)) {
            val dot = filename.lastIndexOf('.')
            if ((dot > -1) && (dot < (filename.length - 1))) {
                return filename.substring(dot + 1)
            }
        }
        return filename
    }


    /**
     * 字符串转含0x 16进制
     * @param paramString
     * @return
     */
    private fun hexStr2Bytesnoenter(paramString: String): ByteArray {
        val paramStr = paramString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val arrayOfByte = ByteArray(paramStr.size)

        for (j in paramStr.indices) {
            arrayOfByte[j] = Integer.decode("0x" + paramStr[j]).toByte()
        }
        return arrayOfByte
    }

    /// <summary>
    /// byte数组转int数组
    /// </summary>
    /// <param name="src">源byte数组</param>
    /// <param name="offset">起始位置</param>
    /// <returns></returns>
    fun bytesToInt(src: ByteArray, offset: Int): IntArray {
        var offset = offset
        val values = IntArray(src.size / 4)
        for (i in 0 until src.size / 4) {
            values[i] = ((src[offset].toInt() and 0xFF)
                    or ((src[offset + 1].toInt() and 0xFF) shl 8)
                    or ((src[offset + 2].toInt() and 0xFF) shl 16)
                    or ((src[offset + 3].toInt() and 0xFF) shl 24))
            offset += 4
        }
        return values
    }

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (i in bytes.indices) {
            val hex = Integer.toHexString(bytes[i].toInt() and 0xFF)
            if (hex.length < 2) {
                sb.append(0)
            }
            sb.append(hex)
        }
        return sb.toString()
    }

    fun unicodeToUtf8(s: String): String? {
        Log.e("TAG", getEncoding(s))
        try {
            if (getEncoding(s) === "UTF-8") {
                return String(s.toByteArray(charset("GBK")), charset("UTF-8"))
            } else if (getEncoding(s) === "GBK" || getEncoding(s) === "GB2312") {
                return String(s.toByteArray(charset("GBK")), charset("GBK"))
            } else if (getEncoding(s) === "ISO-8859-1") {
                return String(s.toByteArray(charset("ISO-8859-1")), charset("UTF-8"))
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    fun getEncoding(str: String): String {
        var encode = "GB2312"
        try {
            if (str == String(str.toByteArray(charset(encode)), charset(encode))) {
                val s = encode
                return s
            }
        } catch (exception: Exception) {
        }
        encode = "ISO-8859-1"
        try {
            if (str == String(str.toByteArray(charset(encode)), charset(encode))) {
                val s1 = encode
                return s1
            }
        } catch (exception1: Exception) {
        }
        encode = "UTF-8"
        try {
            if (str == String(str.toByteArray(charset(encode)), charset(encode))) {
                val s2 = encode
                return s2
            }
        } catch (exception2: Exception) {
        }
        encode = "GBK"
        try {
            if (str == String(str.toByteArray(charset(encode)), charset(encode))) {
                val s3 = encode
                return s3
            }
        } catch (exception3: Exception) {
        }
        return ""
    }


    /**
     * 截取byte[]
     * @param data 被截取数组
     * @param start 起始位置
     * @param length 截取长度
     * @return
     */
    fun byteSub(data: ByteArray, start: Int, length: Int): ByteArray {
        var bt = ByteArray(length)

        if (start + length > data.size) {
            bt = ByteArray(data.size - start)
        }

        var i = 0
        while (i < length && (i + start) < data.size) {
            bt[i] = data[i + start]
            i++
        }
        return bt
    }

    /**
     * 判断字符串内是不是16进制的值
     * @param str
     * @return
     */
    fun isHexStrValid(str: String?): Boolean {
        val pattern = "^[0-9A-F]+$"
        return Pattern.compile(pattern).matcher(str).matches()
    }


    // -------------------------------------------------------
    // 转hex字符串转字节数组
    fun hexToByteArr(inHex: String): ByteArray // hex字符串转字节数组
    {
        var inHex = inHex
        var hexlen = inHex.length
        val result: ByteArray
        if (isOdd(hexlen) == 1) { // 奇数
            hexlen++
            result = ByteArray((hexlen / 2))
            inHex = "0$inHex"
        } else { // 偶数
            result = ByteArray((hexlen / 2))
        }
        var j = 0
        var i = 0
        while (i < hexlen) {
            result[j] = hexToByte(inHex.substring(i, i + 2))
            j++
            i += 2
        }
        return result
    }

    fun hexToByte(inHex: String): Byte // Hex字符串转byte
    {
        return inHex.toInt(16).toByte()
    }

    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    fun isOdd(num: Int): Int {
        return num and 0x1
    }


    fun PrintBase64(file: String?): ByteArray {
        val imgWidth: Int
        val imgHeigh: Int
        val base64Data = readTxt(file).trim { it <= ' ' }
        var bitmap: Bitmap? = null
        try {
            val bytes = Base64.decode(base64Data.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1], Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bitmap = convertToBlackWhite(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        imgWidth = bitmap!!.width
        imgHeigh = bitmap.height
        val iDataLen = imgWidth * imgHeigh
        val pixels = IntArray(iDataLen)
        bitmap.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeigh)
        val data1 = pixels
        val base64: ByteArray = PrintDiskImagefile(data1, imgWidth, imgHeigh)

        //        mUsbDriver.write(PrintCmd.PrintFeedline(10));
//        mUsbDriver.write(PrintCmd.PrintCutpaper(0));
//        mUsbDriver.write(PrintCmd.SetClean());
//        mUsbDriver.closeUsbDevice();
        return base64
    }

    fun getBitmapParamsData(imgPath: String): IntArray {
        val imgWidth: Int
        val imgHeigh: Int
        var file: FileInputStream? = null
        try {
            file = FileInputStream(imgPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        var bitmap = BitmapFactory.decodeStream(file) //
        println(imgPath.substring(imgPath.indexOf(".") + 1))
        if (imgPath.substring(imgPath.indexOf(".") + 1) != "bmp") {
            bitmap = convertToBlackWhite(bitmap)
        }
        imgWidth = bitmap.width
        imgHeigh = bitmap.height
        val iDataLen = imgWidth * imgHeigh
        val pixels = IntArray(iDataLen)
        bitmap.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeigh)
        return pixels
    }

    //    -----------------------------------------------------------------------------------
    /**
     * 将字符串形式表示的十六进制数转换为byte数组
     */
    fun hexStringToBytes(hexString: String): ByteArray {
        var hexString = hexString
        hexString = hexString.lowercase(Locale.getDefault())
        val hexStrings = hexString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val bytes = ByteArray(hexStrings.size)
        for (i in hexStrings.indices) {
            val hexChars = hexStrings[i].toCharArray()
            bytes[i] = (charToByte(hexChars[0]).toInt() shl 4 or charToByte(
                hexChars[1]
            ).toInt()).toByte()
        }
        return bytes
    }

    private fun charToByte(c: Char): Byte {
        return "0123456789abcdef".indexOf(c).toByte()
        // 改成小写 return (byte) "0123456789ABCDEF".indexOf(c);
    }

    // -------------------------------------------------------
    fun byteArrToHex(inBytArr: ByteArray): String // 字节数组转转hex字符串
    {
        val strBuilder = StringBuilder()
        val j = inBytArr.size
        for (i in 0 until j) {
            strBuilder.append(Byte2Hex(inBytArr[i]))
            strBuilder.append(" ")
        }
        return strBuilder.toString()
    }

    // -------------------------------------------------------
    fun Byte2Hex(inByte: Byte?): String // 1字节转2个Hex字符
    {
        return String.format("%02x", inByte).uppercase(Locale.getDefault())
    }

    //打开txt文件获取内容
    fun ReadTxtFile(strFilePath: String): String {
        val path = strFilePath
        var str = ""
        val newList: List<String> = ArrayList()
        //打开文件
        val file = File(path)
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory) {
            Log.d("TestFile", "The File doesn't not exist.")
        } else {
            try {
                // 获取文件
                val fin = FileInputStream(strFilePath)
                // 获得长度
                val length = fin.available()
                // 创建字节数组
                val buffer = ByteArray(length)
                // 读取内容
                fin.read(buffer)
                // 获得编码格式
                val type = codetype(buffer)
                // 按编码格式获得内容
                str = EncodingUtils.getString(buffer, type)
            } catch (e: FileNotFoundException) {
                Log.d("TestFile", "The File doesn't not exist.")
            } catch (e: IOException) {
                Log.d("TestFile", e.message!!)
            }
        }
        return str
    }

    /**
     * // 获得编码格式
     * @param head
     * @return
     */
    private fun codetype(head: ByteArray): String {
        var type = ""
        val codehead = ByteArray(3)
        System.arraycopy(head, 0, codehead, 0, 3)
        type = if (codehead[0].toInt() == -1 && codehead[1].toInt() == -2) {
            "UTF-16"
        } else if (codehead[0].toInt() == -2 && codehead[1].toInt() == -1) {
            "UNICODE"
        } else if (codehead[0].toInt() == -17 && codehead[1].toInt() == -69 && codehead[2].toInt() == -65) {
            "UTF-8"
        } else {
            "GB2312"
        }
        return type
    }

    //从resources中的raw 文件夹中获取文件并读取数据
    fun getFromRaw(`in`: InputStream): String {
        var result = ""
        try {
            //获取文件的字节数
            val lenght = `in`.available()
            //创建byte数组
            val buffer = ByteArray(lenght)
            //将文件中的数据读到byte数组中
            `in`.read(buffer)
            result = EncodingUtils.getString(buffer, OutputKeys.ENCODING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }


    /**
     * 获取当前时间
     * @return
     */
    fun data(): String {
        val date = Date()

        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss ")

        val sim = dateFormat.format(date)
        return sim
    }
}