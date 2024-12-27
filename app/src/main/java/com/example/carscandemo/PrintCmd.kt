package com.example.carscandemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.carscandemo.UtilsTools.convertToBlackWhite
import com.example.carscandemo.UtilsTools.encodeCN
import com.example.carscandemo.UtilsTools.encodeStr
import com.example.carscandemo.UtilsTools.isCN
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.UnsupportedEncodingException
import java.util.Locale

/*==============常用打印指令==============*/
object PrintCmd {
    /**
     * 3.1 设置指令模式
     * 描述：设置打印机指令模式
     * @param iMode 2 EPIC模式、3 EPOS模式
     */
    fun SetCommmandmode(iMode: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x79
        if ((iMode == 2) || (iMode == 3)) bCmd[iIndex++] = iMode.toByte()
        else bCmd[iIndex++] = 3
        return bCmd
    }

    /**
     * 3.2 清理缓存
     * 描述：清理缓存，清除之前设置的参数
     */
    fun SetClean(): ByteArray {
        val bCmd = ByteArray(2)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x40

        return bCmd
    }

    /**
     * 3.3 设定行间距
     * @param iLinespace 行间距，取值0-127，单位0.125mm
     */
    fun SetLinespace(iLinespace: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x33
        if (iLinespace > 127) {
            bCmd[iIndex++] = 127
        } else bCmd[iIndex++] = iLinespace.toByte()
        return bCmd
    }

    /**
     * 3.4 设置字符间距
     * @param iSpace 字符间距，取值0-64，单位0.125mm
     */
    fun SetSpacechar(iSpace: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x20
        if (iSpace > 64) {
            bCmd[iIndex++] = 64
        } else bCmd[iIndex++] = iSpace.toByte()
        return bCmd
    }

    /**
     * 3.5 设置汉字间距
     * @param iChsleftspace  汉字左空，取值0-64，单位0.125mm
     * @param iChsrightspace 汉字右空，取值0-64，单位0.125mm
     */
    fun SetSpacechinese(iChsleftspace: Int, iChsrightspace: Int): ByteArray {
        val bCmd = ByteArray(4)
        var iIndex = 0
        bCmd[iIndex++] = 0x1C
        bCmd[iIndex++] = 0x53
        if (iChsleftspace > 64) {
            bCmd[iIndex++] = 64
        } else bCmd[iIndex++] = iChsleftspace.toByte()

        if (iChsrightspace > 64) {
            bCmd[iIndex++] = 64
        } else bCmd[iIndex++] = iChsrightspace.toByte()
        return bCmd
    }

    /**
     * 3.6 设置左边界
     * @param iLeftspace 取值0-576，单位0.125mm
     */
    fun SetLeftmargin(iLeftspace: Int): ByteArray {
        val bCmd = ByteArray(4)
        var iIndex = 0
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x4C
        if (iLeftspace > 576) {
            bCmd[iIndex++] = 0
            bCmd[iIndex++] = 0
        } else {
            bCmd[iIndex++] = (iLeftspace % 256).toByte()
            bCmd[iIndex++] = (iLeftspace / 256).toByte()
        }
        return bCmd
    }

    /**
     * 3.7 设置黑标切纸偏移量
     * @param iOffset 偏移量，取值0-1600
     */
    fun SetMarkoffsetcut(iOffset: Int): ByteArray {
        var iOffset = iOffset
        val bCmd = ByteArray(6)
        var iIndex = 0
        bCmd[iIndex++] = 0x13
        bCmd[iIndex++] = 0x74
        bCmd[iIndex++] = 0x33
        bCmd[iIndex++] = 0x78
        if (iOffset > 1600) {
            iOffset = 1600
        } else {
            bCmd[iIndex++] = (iOffset shr 8).toByte()
            bCmd[iIndex++] = iOffset.toByte()
        }
        return bCmd
    }

    /**
     * 3.8 设置黑标打印进纸偏移量
     * @param iOffset 偏移量，取值 0-1600
     */
    fun SetMarkoffsetprint(iOffset: Int): ByteArray {
        var iOffset = iOffset
        val bCmd = ByteArray(6)
        var iIndex = 0
        bCmd[iIndex++] = 0x13
        bCmd[iIndex++] = 0x74
        bCmd[iIndex++] = 0x11
        bCmd[iIndex++] = 0x78
        if (iOffset > 1600) {
            iOffset = 1600
        } else {
            bCmd[iIndex++] = (iOffset shr 8).toByte()
            bCmd[iIndex++] = iOffset.toByte()
        }
        return bCmd
    }

    /**
     * 3.9 设置汉字放大
     * @param iHeight      倍高     0 无效  1 有效
     * @param iWidth       倍宽     0 无效  1 有效
     * @param iUnderline   下划线   0 无效  1 有效
     * @param iChinesetype 汉字字形   0: 24*24  1: 16*16
     */
    fun SetSizechinese(
        iHeight: Int, iWidth: Int,
        iUnderline: Int, iChinesetype: Int
    ): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        var height = iHeight
        var width = iWidth
        var underline = iUnderline
        if (height > 1) height = 1

        if (iWidth > 1) width = 1

        if (iUnderline > 1) underline = 1

        val iSize = height * 0x08 + width * 0x04 + underline * 0x80 + iChinesetype * 0x01

        bCmd[iIndex++] = 0x1C
        bCmd[iIndex++] = 0x21
        bCmd[iIndex++] = iSize.toByte()
        return bCmd
    }

    /**
     * 3.10 设置字符放大
     * @param iHeight         倍高     0 无效  1 有效
     * @param iWidth          倍宽     0 无效  1 有效
     * @param iUnderline      下划线   0 无效  1 有效
     * @param iAsciitype   ASCII字形   0: 12*24  1: 9*17
     */
    fun SetSizechar(iHeight: Int, iWidth: Int, iUnderline: Int, iAsciitype: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        var height = iHeight
        var width = iWidth
        var underline = iUnderline
        var asciitype = iAsciitype
        if (height > 1) height = 1
        if (iWidth > 1) width = 1
        if (underline > 1) underline = 1
        if (asciitype > 1) asciitype = 1

        val iSize = height * 0x10 + width * 0x20 + underline * 0x80 + asciitype * 0x01
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x21
        bCmd[iIndex++] = iSize.toByte()
        return bCmd
    }

    /**
     * 3.11 设置文本放大
     * @param iWidth 宽度（1-8）
     * @param iHeight 高度（1-8）
     */
    fun SetSizetext(iWidth: Int, iHeight: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        var height = iHeight
        var width = iWidth

        if (height > 8) height = 8
        if (width > 8) width = 8

        val iSize = iHeight + iWidth * 0x10
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x21
        bCmd[iIndex++] = iSize.toByte()
        return bCmd
    }

    /**
     * 3.12 设置字符对齐
     * @param iAlignment 0 左对齐，1 居中，2 右对齐
     */
    fun SetAlignment(iAlignment: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x61
        if (iAlignment > 2) bCmd[iIndex++] = 2
        else bCmd[iIndex++] = iAlignment.toByte()
        return bCmd
    }

    /**
     * 3.13 设置字体加粗
     * @param iBold  0 不加粗,1 加粗
     */
    fun SetBold(iBold: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x47 // 0x47 都可以加粗
        if (iBold != 1) bCmd[iIndex++] = 0
        else bCmd[iIndex++] = 1
        return bCmd
    }

    /**
     * 3.14 设置字体旋转
     * @param iRotate 0 解除旋转,1 顺时针度旋转90°
     */
    fun SetRotate(iRotate: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x56
        if (iRotate != 1) bCmd[iIndex++] = 0
        else bCmd[iIndex++] = 1
        return bCmd
    }

    /**
     * 3.15 设置字体方向
     * @param iDirection 0 左至右，1 旋转180度
     */
    fun SetDirection(iDirection: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x7B
        if (iDirection != 1) bCmd[iIndex++] = 0
        else {
            bCmd[iIndex++] = 1
        }
        return bCmd
    }

    /**
     * 3.16 设定反白
     * @param iWhite  0  取消反白，1 设置反白
     */
    fun SetWhitemodel(iWhite: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x42
        if (iWhite != 1) bCmd[iIndex++] = 0
        else bCmd[iIndex++] = 1
        return bCmd
    }

    /**
     * 3.17 设定斜体
     * @param iItalic  0 取消斜体；1 设置斜体
     */
    fun SetItalic(iItalic: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x25
        if (iItalic == 1) bCmd[iIndex++] = 0x47
        else bCmd[iIndex++] = 0x48
        return bCmd
    }

    /**
     * 3.18 设定下划线
     * @param underline 0  无， 1 一个点下划线，2 两个点下划线 ；其他无效
     * 描述：设置下划线（字符，ASCII 都有效）
     */
    fun SetUnderline(underline: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x2D
        if (underline > 2) bCmd[iIndex++] = 2
        else bCmd[iIndex++] = underline.toByte()
        return bCmd
    }

    /**
     * 3.19 设置汉字模式
     * @param mode 0 进入汉字模式；1 退出汉字模式
     * 描述：设置汉字模式有无效
     */
    fun SetReadZKmode(mode: Int): ByteArray {
        val bCmd = ByteArray(2)
        var iIndex = 0
        bCmd[iIndex++] = 0x1C
        if (mode == 1) bCmd[iIndex++] = 0x2E
        else bCmd[iIndex++] = 0x26
        return bCmd
    }

    /**
     * 3.20 设置水平制表位置
     * @param bHTseat 水平制表的位置,从小到大,单位一个ASCII字符,不能为0
     * @param iLength 水平制表的位置数据的个数
     */
    fun SetHTseat(bHTseat: ByteArray, iLength: Int): ByteArray {
        val bCmd = ByteArray(35)
        var iIndex = 0
        val length = if (iLength > 32) 32
        else iLength

        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x44
        var x = 0
        while (x < length) {
            bCmd[iIndex++] = bHTseat[x]
            x++
        }
        bCmd[iIndex++] = 0x00
        return bCmd
    }

    /**
     * 3.21 设置区域国家和代码页
     * @param country  区域国家 0   美国   1	法国      2	德国           3  英国    4  丹麦 I
     * 5   瑞典   6	意大利  7	西班牙 I  8  日本    9  挪威  10 丹麦 II
     * @param CPnumber 代码页             0  PC437[美国欧洲标准]     1 	 PC737    2	PC775
     * 3   PC850   4	 PC852   5	PC855     6	 PC857    7	PC858   8  PC860   9  PC862
     * 10  PC863  11	 PC864  12	PC865    13	 PC866   14	PC1251 15 PC1252  16  PC1253
     * 17  PC1254 18	 PC1255 19	PC1256   20	 PC1257  21	PC928  22 Hebrew old
     * 23  IINTEL CHAR      18	Katakana 25    特殊符号00-1F  26	SPACE PAGE
     */
    fun SetCodepage(country: Int, CPnumber: Int): ByteArray {
        val bCmd = ByteArray(6)
        var iIndex = 0

        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x52
        if (country < 11) bCmd[iIndex++] = country.toByte()
        else bCmd[iIndex++] = 0x00

        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x74
        if (CPnumber < 27) bCmd[iIndex++] = CPnumber.toByte()
        else bCmd[iIndex++] = 0x00
        return bCmd
    }


    /**
     * 3.22 打印自检页
     */
    fun PrintSelfcheck(): ByteArray {
        val bCmd = ByteArray(7)
        var iIndex = 0
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x28
        bCmd[iIndex++] = 0x41
        bCmd[iIndex++] = 0x02
        bCmd[iIndex++] = 0x00
        bCmd[iIndex++] = 0x00
        bCmd[iIndex++] = 0x02
        return bCmd
    }

    /**
     * 3.23 打印走纸
     * @param iLine 走纸行数
     * 描述：走纸，单位字符行
     */
    fun PrintFeedline(iLine: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x64
        bCmd[iIndex++] = iLine.toByte()
        return bCmd
    }

    /**
     * 3.25 打印字符串
     * @param strData 打印的字符串内容
     * @param iImme   是否加换行指令0x0a： 0 加换行指令，1 不加换行指令
     * @throws UnsupportedEncodingException
     * 描述: 打印字符串，字符集为GB2312 入口参数：strData 打印的字符串内容，出口参数:byte[]数组
     */
    fun PrintString(strData: String, iImme: Int): ByteArray? {
        // 字符串转换为byte[]数组
        val strAarry: ByteArray
        try {
            strAarry = strData.toByteArray(charset("ISO-8859-6"))
            println("Array 1" + strAarry.contentToString())
            var iLen = strAarry.size
            if (iImme == 0) iLen = iLen + 1
            val bCmd = ByteArray(iLen)
            System.arraycopy(strAarry, 0, bCmd, 0, strAarry.size)
            if (iImme == 0) bCmd[iLen - 1] = 0x0A

            println("Array 3" + bCmd.contentToString())
            return bCmd
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 3.26 打印并换行
     * 描述：打印内容并换行，无打
     *
     *
     * 印内容的时候走1空白行
     */
    fun PrintChargeRow(): ByteArray {
        val bCmd = ByteArray(2)
        var iIndex = 0
        bCmd[iIndex++] = 0x0A
        return bCmd
    }

    /**
     * 3.27 打印细走纸
     * @param Lnumber  范围 0-250
     */
    fun PrintFeedDot(Lnumber: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x4A
        if (Lnumber > 250) bCmd[iIndex++] = 250.toByte()
        else bCmd[iIndex++] = Lnumber.toByte()
        return bCmd
    }

    /**
     * 3.28 执行到下一个水平制表位置
     * 描述：执行到下一个水平制表位置
     */
    fun PrintNextHT(): ByteArray {
        val bCmd = ByteArray(1)
        var iIndex = 0
        bCmd[iIndex++] = 0x09
        return bCmd
    }

    /**
     * 3.29 打印切纸
     * @param iMode 0 全切，1半切
     */
    fun PrintCutpaper(iMode: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        if (iMode != 1) {
            bCmd[iIndex++] = 0x1B
            bCmd[iIndex++] = 0x69
        } else {
            bCmd[iIndex++] = 0x1B
            bCmd[iIndex++] = 0x6D
        }
        bCmd[iIndex++] = iMode.toByte()
        return bCmd
    }

    /*
     * 3.30 检测黑标[之前有使用过]
     *     描述：黑标模式下检测黑标，停止在黑标位置
     */
    fun PrintMarkposition(): ByteArray {
        val bCmd = ByteArray(1)
        var iIndex = 0
        bCmd[iIndex++] = 0x0C
        return bCmd
    }

    /*
     * 3.31 检测黑标进纸到打印位置
     *     描述：黑标模式下检测黑标并进纸到打印位置（偏移量打印影响走纸距离）
     */
    fun PrintMarkpositionPrint(): ByteArray {
        val bCmd = ByteArray(2)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x0C
        return bCmd
    }

    /**
     * 3.32 检测黑标进纸到切纸位置
     * 描述：黑标模式下检测黑标并进纸到切纸位置（偏移量切纸影响走纸距离）
     */
    fun PrintMarkpositioncut(): ByteArray {
        val bCmd = ByteArray(2)
        var iIndex = 0
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x0C
        return bCmd
    }

    /**
     * 3.33 打印黑标切纸
     * @param iMode  0 检测黑标全切，1 不检测黑标半切
     */
    fun PrintMarkcutpaper(iMode: Int): ByteArray {
        val bCmd = ByteArray(4)
        var iIndex = 0
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x56
        if (iMode == 0) {
            bCmd[iIndex++] = 0x42
            bCmd[iIndex++] = 0x0
        } else {
            bCmd[iIndex++] = 0x1
        }
        return bCmd
    }


    // 13 51指令
    fun PrintQrcode51(strData: String?, iLmargin: Int, iMside: Int, iRound: Int): ByteArray? {
        /*     byte[] bCmd = new byte[50];
       QRCodeInfo codeInfo = new QRCodeInfo();
        codeInfo.setlMargin(iLmargin);
        codeInfo.setmSide(iMside);
        bCmd = codeInfo.Get51QRBCode(strData, iRound);
        if(bCmd.length != 0)
            return bCmd;
        else*/
        return null
    }

    /**
     * 主板专用二维码打印【T500II+MS532II】
     * @param strData
     * @return
     */
    fun PrintQrCodeT500II(mSize: Int, strData: String): ByteArray? {
        var mSize = mSize
        val strArray: ByteArray
        try {
            strArray = strData.toByteArray(charset("GB2312"))
            val bCmd = ByteArray(25 + strArray.size)
            var iIndex = 0
            bCmd[iIndex++] = 0x13
            bCmd[iIndex++] = 0x50
            bCmd[iIndex++] = 0x48
            bCmd[iIndex++] = 0x1
            bCmd[iIndex++] = 0x1

            bCmd[iIndex++] = 0x13
            bCmd[iIndex++] = 0x50
            bCmd[iIndex++] = 0x48
            bCmd[iIndex++] = 0x2
            bCmd[iIndex++] = 0x1

            bCmd[iIndex++] = 0x13
            bCmd[iIndex++] = 0x50
            bCmd[iIndex++] = 0x48
            bCmd[iIndex++] = 0x3
            if (mSize < 1 || mSize > 9) // 1-9
                mSize = 5
            else bCmd[iIndex++] = mSize.toByte()

            bCmd[iIndex++] = 0x13
            bCmd[iIndex++] = 0x50
            bCmd[iIndex++] = 0x48
            bCmd[iIndex++] = 0x4

            for (i in strArray.indices) {
                bCmd[iIndex++] = strArray[i]
            }
            bCmd[iIndex++] = 0x0
            bCmd[iIndex++] = 0x13
            bCmd[iIndex++] = 0x50
            bCmd[iIndex++] = 0x48
            bCmd[iIndex++] = 0x5
            return bCmd
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 3.36 打印PDF417码
     * @param iDotwidth    宽度，取值0-255
     * @param iDotheight   高度，取值0-255
     * @param iDatarows    行数
     * @param iDatacolumns 列数
     * @param strData      条码内容，若使用字符串模式，应包含结尾符；
     * 若使用字节流模式，长度应补齐为字节单位，未使用的位补为0
     * 1d 6b 4C 0A 30 31 32 33 34 35 36 37 38 39 00
     */
    fun PrintPdf417(
        iDotwidth: Int, iDotheight: Int,
        iDatarows: Int, iDatacolumns: Int, strData: String
    ): ByteArray {
        val width1 = if ((iDotwidth < 2) || (iDotwidth > 6)) {
            2
        } else {
            iDotwidth // 宽度
        }
        val height1 = iDotheight // 高度
        val length1 = strData.length // strData长度
        // 条码的宽度、高度、长度
        val bCmd = ByteArray(128)
        var iIndex = 0
        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x77
        bCmd[iIndex++] = width1.toByte()
        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x68
        bCmd[iIndex++] = height1.toByte() // '1');

        iIndex = 0
        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x6b
        bCmd[iIndex++] = 0x4c
        bCmd[iIndex++] = iDatarows.toByte()
        bCmd[iIndex++] = iDatacolumns.toByte()
        bCmd[iIndex++] = length1.toByte()

        var str = ByteArray(length1)
        str = strData.toByteArray()
        for (i in 0 until length1) {
            bCmd[iIndex++] = str[i]
        }
        return bCmd
    }

    /**
     * 3.37 打印一维条码
     * @param iWidth    条码宽度，取值2-6 单位0.125mm
     * @param iHeight   条码高度，取值1-255 单位0.125mm
     * @param iHrisize  条码显示字符字型0 12*24 1 9*17
     * @param iHriseat  条码显示字符位置0 无、1 上、 2 下、3 上下
     * @param iCodetype 条码的类型（UPC-A 0,UPC-E 1,EAN13 2,EAN8 3, CODE39 4,
     * ITF 5,CODABAR 6,Standard EAN13 7,
     * Standard EAN8 8,CODE93 9,CODE128 10)
     * @param strData  条码内容
     */
    fun Print1Dbar(
        iWidth: Int, iHeight: Int, iHrisize: Int,
        iHriseat: Int, iCodetype: Int, strData: String
    ): ByteArray {
        val bCmd = ByteArray(64)
        var iIndex = 0
        var length = 0
        var width = iWidth
        var height = iHeight
        var codetype = iCodetype // 条码类型
        if ((width < 2) || (width > 6)) width = 2
        if ((height < 24) || (height > 250)) height = 24
        if (codetype > 10) codetype = 10
        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x77
        bCmd[iIndex++] = width.toByte()
        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x68
        bCmd[iIndex++] = height.toByte()

        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x66
        if (iHrisize > 1) bCmd[iIndex++] = 0
        else bCmd[iIndex++] = iHrisize.toByte()
        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x48
        if (iHriseat > 3) bCmd[iIndex++] = 0
        else bCmd[iIndex++] = iHriseat.toByte()
        bCmd[iIndex++] = 0x1d
        bCmd[iIndex++] = 0x6b

        val strAarry = strData.toByteArray()
        length = strAarry.size

        var i = 0
        when (codetype) {
            0 -> {
                bCmd[iIndex++] = 0x00
                if (length < 11) return bCmd
                i = 0
                while (i < 11) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) return bCmd
                    i++
                }
                i = 0
                while (i < 11) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            1 -> {
                bCmd[iIndex++] = 0x01
                if (length < 11) return bCmd
                if (strAarry[0].toInt() != 0x30) return bCmd
                i = 1
                while (i < 11) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) return bCmd
                    i++
                }
                i = 0
                while (i < 11) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            2 -> {
                bCmd[iIndex++] = 0x02
                if (length < 12) return bCmd
                i = 0
                while (i < 12) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) return bCmd
                    i++
                }
                i = 0
                while (i < 12) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            3 -> {
                bCmd[iIndex++] = 0x03
                if (length < 7) return bCmd
                i = 0
                while (i < 7) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) return bCmd
                    i++
                }
                i = 0
                while (i < 7) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            4 -> {
                bCmd[iIndex++] = 0x04
                i = 0
                while (i < length) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            5 -> {
                bCmd[iIndex++] = 0x05
                i = 0
                while (i < length) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) return bCmd
                    i++
                }
                if (length % 2 == 1) length = length - 1
                i = 0
                while (i < length) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            6 -> {
                bCmd[iIndex++] = 0x06
                i = 0
                while (i < length) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            7 -> {
                bCmd[iIndex++] = 0x07
                if (length < 12) return bCmd
                i = 0
                while (i < 12) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) return bCmd
                    i++
                }
                i = 0
                while (i < 12) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            8 -> {
                bCmd[iIndex++] = 0x08
                if (length < 7) return bCmd
                i = 0
                while (i < 7) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) return bCmd
                    i++
                }
                i = 0
                while (i < 7) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
                bCmd[iIndex++] = 0x00
            }

            9 -> {
                bCmd[iIndex++] = 72
                bCmd[iIndex++] = length.toByte()
                i = 0
                while (i < length) {
                    bCmd[iIndex++] = strAarry[i]
                    i++
                }
            }

            10 -> {
                bCmd[iIndex++] = 73
                i = 0
                while (i < length) {
                    if ((strAarry[i] < 48) || (strAarry[i] > 57)) break
                    i++
                }
                if (i == length) {
                    if (length % 2 == 1) bCmd[iIndex++] = (length / 2 + 1 + 4).toByte()
                    else bCmd[iIndex++] = (length / 2 + 2).toByte()
                    bCmd[iIndex++] = 123
                    bCmd[iIndex++] = 67
                    i = 0
                    while (i < length) {
                        if ((i + 1) >= length) {
                            bCmd[iIndex++] = 123
                            bCmd[iIndex++] = 66
                            bCmd[iIndex++] = strAarry[i]
                            i++
                        } else {
                            bCmd[iIndex++] =
                                ((strAarry[i] - 0x30) * 10 + (strAarry[i + 1] - 0x30)).toByte()
                            i++
                        }
                        i++
                    }
                } else {
                    bCmd[iIndex++] = (length + 2).toByte()
                    bCmd[iIndex++] = 123
                    bCmd[iIndex++] = 66
                    i = 0
                    while (i < length) {
                        bCmd[iIndex++] = strAarry[i]
                        i++
                    }
                }
            }

            else -> {}
        }
        return bCmd
    }


    /**
     * 根据指定路径的图片打印
     * 支持图片内容为黑白两色的 BMP/JPG/PNG格式的文件
     * @param strPath 文件路径
     * @return
     */
    fun PrintDiskImagefile(strPath: String): ByteArray? {
        val bytes: ByteArray
        var file: FileInputStream? = null
        try {
            file = FileInputStream(strPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        var bitmap: Bitmap? = BitmapFactory.decodeStream(file) ?: return null

        if (strPath.substring(strPath.lowercase(Locale.getDefault()).indexOf(".") + 1) != "bmp") {
            bitmap = convertToBlackWhite(bitmap)
            val width = bitmap.width
            val heigh = bitmap.height
            val iDataLen = width * heigh
            val pixels = IntArray(iDataLen)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh)
            bytes = PrintDiskImagefile(pixels, width, heigh)
        } else {
            val width = bitmap?.width
            val heigh = bitmap?.height
            val iDataLen = heigh?.let { width?.times(it) }
            val pixels = iDataLen?.let { IntArray(it) }
            if (pixels != null) {
                if (width != null) {
                    bitmap?.getPixels(pixels, 0, width, 0, 0, width, heigh)
                }
            }
            bytes = pixels?.let { PrintDiskImagefile(it, width!!, heigh) }!!
        }

        return bytes
    }

    fun PrintDiskImagefile(pixels: IntArray, iWidth: Int, iHeight: Int): ByteArray {
        var iBw = iWidth / 8
        val iMod = iWidth % 8
        if (iMod > 0) iBw = iBw + 1
        val iDataLen = iBw * iHeight
        val bCmd = ByteArray(iDataLen + 8)
        var iIndex = 0
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x76
        bCmd[iIndex++] = 0x30
        bCmd[iIndex++] = 0x0
        bCmd[iIndex++] = iBw.toByte()
        bCmd[iIndex++] = (iBw shr 8).toByte()
        bCmd[iIndex++] = iHeight.toByte()
        bCmd[iIndex++] = (iHeight shr 8).toByte()

        var iValue1 = 0
        var iValue2 = 0
        var iRow = 0
        var iCol = 0
        var iW = 0
        var iValue3 = 0
        var iValue4 = 0
        iRow = 0
        while (iRow < iHeight) {
            iCol = 0
            while (iCol < iBw - 1) {
                iValue2 = 0

                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x80
                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x40
                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x20
                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x10
                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x8
                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x4
                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x2
                iValue1 = pixels[iW++]
                if (iValue1 < -1) iValue2 = iValue2 + 0x1
                if (iValue3 < -1) // w1
                    iValue4 = iValue4 + 0x10
                bCmd[iIndex++] = iValue2.toByte()
                iCol++
            }
            iValue2 = 0
            if (iValue4 > 0) // w2
                iValue3 = 1
            if (iMod == 0) {
                iCol = 8
                while (iCol > iMod) {
                    iValue1 = pixels[iW++]
                    if (iValue1 < -1) iValue2 = iValue2 + (1 shl iCol)
                    iCol--
                }
            } else {
                iCol = 0
                while (iCol < iMod) {
                    iValue1 = pixels[iW++]
                    if (iValue1 < -1) iValue2 = iValue2 + (1 shl (8 - iCol))
                    iCol++
                }
            }
            bCmd[iIndex++] = iValue2.toByte()
            iRow++
        }
        return bCmd
    }


    /**
     * 3.39 打印NV BMP 文件  【可用，已通过测试】
     * @param iNvindex  NV位图索引
     * @param iMode     48 普通、49 倍宽、50 倍高、51 倍宽倍高(4倍大小)
     * 描述：打印NV BMP文件，仅支持单色BMP文件
     */
    fun PrintNvbmp(iNvindex: Int, iMode: Int): ByteArray {
        val bCmd = ByteArray(4)
        var iIndex = 0
        var iValue = iMode
        if (iMode < 48) iValue = 48
        if (iMode > 51) iValue = 51

        bCmd[iIndex++] = 0x1C
        bCmd[iIndex++] = 0x70
        bCmd[iIndex++] = iNvindex.toByte()
        bCmd[iIndex++] = iValue.toByte()
        return bCmd
    }

    /**
     * 3.42 获取打印机状态
     * @return     0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
     * 3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
     */
    fun GetStatus(): ByteArray? {
        try {
            val b_send = ByteArray(12)
            var iIndex = 0
            // 01 判断打印机是否连接正常
            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x01
            // 02 判断打印机 机头打开
            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x02
            // 03 判断打印机 切刀   打印头温度
            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x03
            // 04 判断打印机纸尽  纸将尽
            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x04
            return b_send
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun GetStatus1(): ByteArray? {
        try {
            val b_send = ByteArray(3)
            var iIndex = 0

            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x01
            return b_send
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun GetStatus2(): ByteArray? {
        try {
            val b_send = ByteArray(3)
            var iIndex = 0

            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x02
            return b_send
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun GetStatus3(): ByteArray? {
        try {
            val b_send = ByteArray(3)
            var iIndex = 0

            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x03
            return b_send
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun GetStatus4(): ByteArray? {
        try {
            val b_send = ByteArray(3)
            var iIndex = 0

            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x04
            return b_send
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun GetStatus5(): ByteArray? {
        try {
            val b_send = ByteArray(3)
            var iIndex = 0

            b_send[iIndex++] = 0x10
            b_send[iIndex++] = 0x04
            b_send[iIndex++] = 0x05
            return b_send
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 检测打印机状态
     * @param  b_recv
     * @return int
     */
    fun CheckStatus(b_recv: ByteArray): Int {
        if ((b_recv[0].toInt() and 0x16) != 0x16) // 01 判断打印机是否连接正常
            return 2
        if ((b_recv[1].toInt() and 0x04) == 0x04) // 02 判断打印机 机头打开
            return 3
        if ((b_recv[2].toInt() and 0x08) == 0x08) // 03 判断打印机 切刀 打印头温度
            return 4
        if ((b_recv[2].toInt() and 0x40) == 0x40) return 5
        if ((b_recv[2].toInt() and 0x20) == 0x20) return 6
        if ((b_recv[3].toInt() and 0x60) == 0x60) // 04 判断打印机纸尽 纸将尽
            return 7
        if ((b_recv[3].toInt() and 0x0C) == 0x0C) return 8
        return 0
    }

    fun CheckStatus1(bRecv: Byte): Int {
        if ((bRecv.toInt() and 0x16) != 0x16) // 01 判断打印机是否连接正常
            return 2

        return 0
    }

    fun CheckStatus2(bRecv: Byte): Int {
        if ((bRecv.toInt() and 0x04) == 0x04) // 02 判断打印机 机头打开
            return 3
        return 0
    }

    fun CheckStatus3(bRecv: Byte): Int {
        if ((bRecv.toInt() and 0x08) == 0x08) // 03 判断打印机 切刀 打印头温度
            return 4
        if ((bRecv.toInt() and 0x40) == 0x40) return 5
        if ((bRecv.toInt() and 0x20) == 0x20) return 6

        return 0
    }

    fun CheckStatus4(bRecv: Byte): Int {
        if ((bRecv.toInt() and 0x60) == 0x60) // 04 判断打印机纸尽 纸将尽
            return 7
        if ((bRecv.toInt() and 0x0C) == 0x0C) return 8
        return 0
    }

    //10 04 05
    // 4 容纸器错误 5 堵纸  6 卡纸  7 拽纸 8 出纸传感器有纸
    fun CheckStatus5(bRecv: Byte): Int {
        if ((bRecv.toInt() and 0x80) == 0x80) return 4 //容纸器错误、


        if ((bRecv.toInt() and 0x01) == 0x01) return 5 //5 堵纸


        if ((bRecv.toInt() and 0x08) == 0x08) return 6 //6 卡纸


        if ((bRecv.toInt() and 0x02) == 0x02) return 7 //7 拽纸


        if ((bRecv.toInt() and 0x04) == 0x04) return 8 //8 出纸传感器有纸


        return 0
    }

    /**
     * 3.43 获取打印机特殊功能状态
     * 【描述：获取打印机特殊功能状态，仅适用于D347部分机型】
     * 返回值： 0 打印机正常、  1 打印机未连接或未上电、        2 打印机和调用库不匹配
     * 3 当前使用打印机无特殊功能、 4 容纸器错误、5 堵纸
     * 6 卡纸、            7 拽纸、           8 出纸传感器有纸
     * @return int
     */
    fun getStatusSpecial(): ByteArray? {
        return try {
            val bRecv = ByteArray(6)
            var iIndex = 0
            // 01 Check if the printer is properly connected
            bRecv[iIndex++] = 0x10
            bRecv[iIndex++] = 0x04
            bRecv[iIndex++] = 0x01
            // 05 Check printer special functions: paper cassette error, paper jam, paper stuck, paper drag, paper detected by sensor
            bRecv[iIndex++] = 0x10
            bRecv[iIndex++] = 0x04
            bRecv[iIndex++] = 0x05
            bRecv
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun GetStatusspecialDescriptionEn(iStatus: Int): String {
        var strResult = ""
        when (iStatus) {
            0 -> strResult = "Printer is ready"
            1 -> strResult = "Printer is offline or no power"
            2 -> strResult = "Printer called unmatched library"
            3 -> strResult = "No special functions"
            4 -> strResult = "Tray error"
            5 -> strResult = "Paper blocking"
            6 -> strResult = "Paper jam"
            7 -> strResult = "Drag paper or Empty data"
            8 -> strResult = "Output sensor has papers"
        }
        return strResult
    }


    /**
     * 3.44 获取打印机信息
     * @param iFstype  信息类型： 1 打印头型号ID、2 类型ID、       3 软件版本、
     * 4 生产厂商信息、  5 打印机型号、 6 支持的中文编码格式
     */
    fun GetProductinformation(iFstype: Int): ByteArray {
        val b_send = ByteArray(3)
        try {
            var iIndex = 0
            b_send[iIndex++] = 0x1D
            b_send[iIndex++] = 0x49
            b_send[iIndex++] = iFstype.toByte()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return b_send
    }

    /**
     * 解析打印机产品信息
     * @param b_recv
     */
    fun CheckProductinformation(b_recv: ByteArray?): String {
        var info = ""
        if (b_recv != null) {
            try {
                info = String(b_recv, charset("UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return info
        } else {
            return "Failed to get printer product information!"
        }
    }

    /**
     * 3.45 获取开发包信息
     * @return String
     */
    fun GetSDKinformation(): String {
        return "V3.0.0.0"
    }

    /**
     * 3.46 设置右边距
     * @param iRightspace
     * [范围] 0 ≤ n ≤ 255
     * [描述] 设置字符右侧的间距为n 个水平点距。 在倍宽模式下，字符右侧间距是正常
     * 值的两倍；当字符被放大时，字符右侧间距被放大同样的倍数。
     */
    fun SetRightmargin(iRightspace: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1B
        bCmd[iIndex++] = 0x20
        bCmd[iIndex++] = iRightspace.toByte()
        return bCmd
    }

    /**
     * 3.47 设置条码对齐方式
     * @param iAlign 0 左对齐 、1 居中对齐、2 右对齐
     * 描述：打印条形码时，根据iAlign可选值进行条码对齐
     */
    fun Set1DBarCodeAlign(iAlign: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x1D
        bCmd[iIndex++] = 0x50
        bCmd[iIndex++] = iAlign.toByte()
        return bCmd
    }

    /* END  ================常用打印接口说明============== */ // *************************************************************
    /* START================定制类打印接口说明(EP800)==============
     */
    /*
     * 4.0 设置指令模式
     *     描述：设置打印机指令模式
     * @param iMode 2 EPIC模式、3 EPOS模式
     */
    fun SetCommandmode(iMode: Int): ByteArray {
        return SetCommmandmode(iMode)
    }

    /*
     * 4.1 设置旋转打印模式 【----未通过测试----】
     *     描述：设置进入旋转打印模式
     */
    fun SetRotation_Intomode(): Int {
        var iRet = 1
        val bCmd = ByteArray(2)
        var iIndex = 0
        bCmd[iIndex++] = 0x13
        bCmd[iIndex++] = 0x44
        iRet = if (bCmd != null && bCmd.size > 1) 1
        else 0
        return iRet
    }

    /*
     * 4.2 打印旋转模式数据
     *    描述：打印进入旋转模式后保存的数据并退出旋转模式并默认EPOS指令模式
     */
    fun PrintRotation_Data(): ByteArray {
        val bCmd = ByteArray(1)
        var iIndex = 0
        bCmd[iIndex++] = 0x0b
        return bCmd
    }

    /*
     * 4.3 发送旋转模式数据
     * @param strData 文本数据
     * @param iImme   换行：0 不换行、1 换行
     *     描述：旋转模式下传文本数据
     */
    fun PrintRotation_Sendtext(strData: String, iImme: Int): ByteArray {
        var strData = strData
        val bCmd = ByteArray(1)
        var iLen = strData.length
        var strString: ByteArray? = ByteArray(iLen + 1)
        strString = strData.toByteArray()
        // memcpy(strString,strData,iLen);
        System.arraycopy(strString, 0, bCmd, 0, iLen)
        if (iImme != 1) strString[iLen++] = 0x0A
        // 获取变量，置空操作
        run {
            strData = System.getenv(String(strString!!))
            strData = ""
            strString = null
        }
        return bCmd
    }

    /*
     * 4.4 发送旋转模式条码
     * @param leftspace  条码左边距，单位mm
     * @param iWidth     条码宽度，取值2-6 单位0.125mm
     * @param iHeight    条码高度，取值1-255 单位0.125mm
     * @param iCodetype  条码的类型 （* UPC-A 0,* UPC-E 1,* EAN13 2,* EAN8 3,
     *                                 CODE39 4,* ITF 5,* CODABAR 6,* Standard EAN13 7,
     *                                 Standard EAN8 8,* CODE93 9,* CODE128 10)
     * @param iCodedata    条码内容
     * 	            描述：旋转模式下传条码数据
     */
    fun PrintRotation_Sendcode(
        leftspace: Int, iWidth: Int,
        iHeight: Int, iCodetype: Int, iCodedata: String
    ): ByteArray? {
        try {
            val bCmd = ByteArray(64)
            var iIndex = 0
            var length = 0
            var Codetype = 2
            bCmd[iIndex++] = 0x1B
            bCmd[iIndex++] = 0x62

            if (leftspace < 72) bCmd[iIndex++] = leftspace.toByte()
            else bCmd[iIndex++] = 0

            if ((iWidth >= 2) && (iWidth <= 6)) bCmd[iIndex++] = iWidth.toByte()
            else bCmd[iIndex++] = 2

            if ((iHeight >= 1) && (iHeight <= 10)) bCmd[iIndex++] = iHeight.toByte()
            else bCmd[iIndex++] = 1

            Codetype =
                if ((iCodetype <= 8) || (iCodetype == 12)) iCodetype
                else 2

            length = iCodedata.length
            var strData = ByteArray(length)
            strData = iCodedata.toByteArray()
            if (length < 2) return bCmd
            var i = 0
            when (Codetype) {
                0 -> {
                    bCmd[iIndex++] = 0x00
                    i = 0
                    while (i < length) {
                        if ((strData[i] < 48) || (strData[i] > 57)) return bCmd
                        i++
                    }
                    if (length % 2 == 1) length = length - 1
                    i = 0
                    while (i < length) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                1 -> {
                    bCmd[iIndex++] = 0x01
                    i = 0
                    while (i < length) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                2 -> {
                    bCmd[iIndex++] = 0x02
                    i = 0
                    while (i < length) {
                        if ((strData[i] < 48) || (strData[i] > 57)) break
                        i++
                    }
                    if (i == length) {
                        // if(length%2) bCmd[iIndex++] = length/2 + 1 + 2;
                        // else bCmd[iIndex++] = length/2 + 1;
                        bCmd[iIndex++] = 137.toByte()
                        i = 0
                        while (i < length) {
                            if ((i + 1) >= length) {
                                bCmd[iIndex++] = 136.toByte()
                                bCmd[iIndex++] = strData[i]
                                i++
                            } else {
                                bCmd[iIndex++] =
                                    ((strData[i] - 0x30) * 10 + (strData[i + 1] - 0x30)).toByte()
                                i++
                            }
                            i++
                        }
                    } else {
                        // bCmd[iIndex++]=length+1;
                        bCmd[iIndex++] = 136.toByte()
                        i = 0
                        while (i < length) {
                            bCmd[iIndex++] = strData[i]
                            i++
                        }
                    }
                    bCmd[iIndex++] = 0x03
                }

                3 -> {
                    bCmd[iIndex++] = 0x03
                    if (length < 11) return bCmd
                    i = 0
                    while (i < 11) {
                        if ((strData[i] < 48) || (strData[i] > 57)) return bCmd
                        i++
                    }
                    i = 0
                    while (i < 11) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                4 -> {
                    bCmd[iIndex++] = 0x04
                    if (length < 12) return bCmd
                    i = 0
                    while (i < 12) {
                        if ((strData[i] < 48) || (strData[i] > 57)) return bCmd
                        i++
                    }
                    i = 0
                    while (i < 12) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                5 -> {
                    bCmd[iIndex++] = 0x05
                    if (length < 11) return bCmd
                    if (strData[0].toInt() != 0x30) return bCmd
                    i = 1
                    while (i < 11) {
                        if ((strData[i] < 48) || (strData[i] > 57)) return bCmd
                        i++
                    }
                    i = 0
                    while (i < 11) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                6 -> {
                    bCmd[iIndex++] = 0x06
                    if (length < 7) return bCmd
                    i = 0
                    while (i < 7) {
                        if ((strData[i] < 48) || (strData[i] > 57)) return bCmd
                        i++
                    }
                    i = 0
                    while (i < 7) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                7 -> {
                    bCmd[iIndex++] = 0x07
                    i = 0
                    while (i < length) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                8 -> {
                    bCmd[iIndex++] = 0x08
                    i = 0
                    while (i < length) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                }

                12 -> {
                    bCmd[iIndex++] = 0x0C
                    if (length % 2 == 1) length = length - 1
                    if (length > 14) return bCmd
                    i = 0
                    while (i < length) {
                        if ((strData[i] < 48) || (strData[i] > 57)) return bCmd
                        i++
                    }
                    i = 0
                    while (i < length) {
                        bCmd[iIndex++] = strData[i]
                        i++
                    }
                    bCmd[iIndex++] = 0x03
                    bCmd[iIndex++] = 0x00
                }

                else -> {}
            }
            return bCmd
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /*
     * 4.5 发送旋转模式换行
     *     描述：旋转模式下换行
     */
    fun PrintRotation_Changeline(): ByteArray {
        val bCmd = ByteArray(1)
        var iIndex = 0
        bCmd[iIndex++] = 0x0a
        return bCmd
    }

    /*
     * 4.6 发送旋转模式左边距
     * @param iLeftspace 左边距，单位mm
     *     描述：设置旋转模式下左边距
     */
    fun SetRotation_Leftspace(iLeftspace: Int): ByteArray {
        val bCmd = ByteArray(3)
        var iIndex = 0
        bCmd[iIndex++] = 0x13
        bCmd[iIndex++] = 0x76
        if (iLeftspace < 72) bCmd[iIndex++] = iLeftspace.toByte()
        else bCmd[iIndex++] = 0x00
        return bCmd
    }


    /*
     * 4.7 设置打印机ID或名称
     * @param IDorNAME  打印机ID或名称
     * @return int
     */
    //	public static byte[] SetPrintIDorName(String IDorNAME) {
    //		byte[] b_send = new byte[64];
    //		int iIndex;
    //		int length, i;
    //		length = IDorNAME.length();
    //		byte[] strIDorNAME = new byte[length];
    //		strIDorNAME = IDorNAME.getBytes(); // 转变位数组
    //		if ((length == 0) || (length > 30))
    //			return b_send;
    //		iIndex = 0;
    //		b_send[iIndex++] = 0x13;
    //		b_send[iIndex++] = 0x75;
    //		b_send[iIndex++] = (byte) length;
    //		for (i = 0; i < length; i++) {
    //			b_send[iIndex++] = strIDorNAME[i];
    //		}
    //		return b_send;
    //	}
    /*
     * 4.8 获取打印机ID或名称
     * @param strIDorNAME  打印机ID或名称
     */
    //	public static byte[] GetPrintIDorName(byte[] strIDorNAME) {
    //		int iIndex = 0;
    //		byte[] b_send = new byte[2];
    //		byte[] b_recv = new byte[128];
    //
    //		b_send[iIndex++] = 0x13;
    //		b_send[iIndex++] = 0x76;
    //		System.arraycopy(b_send, iIndex, b_recv, 0, 32);// b_send拷贝到b_recv数组中去
    //		for (iIndex = 0; iIndex < 32; iIndex++) {
    //			strIDorNAME[iIndex] = b_recv[iIndex];
    //		}
    //		return strIDorNAME;
    //	}
    /** END================定制类打印接口说明(EP800)==============  */
    /** ========================Start【HexUtils之前用到的函数】=========================  */
    private fun getHexResult(targetStr: String): String {
        val hexStr = StringBuilder()
        val len = targetStr.length
        if (len > 0) {
            for (i in 0 until len) {
                val tempStr = targetStr[i]
                val data = tempStr.toString()
                if (isCN(data)) {
                    hexStr.append(encodeCN(data))
                } else {
                    hexStr.append(encodeStr(data))
                }
            }
        }
        return hexStr.toString()
    }


    /** ==================================End===================================  */
    /**=====================JNA========================= */
    /**
     * JNA字符串转数组
     * @param strData
     * @param iLen
     * @return
     */
    fun JNAStringToByte(strData: String, iLen: Int): ByteArray {
        var iIndex = 0
        val bData1 = ByteArray(iLen)
        var iValue1 = 0
        var strValue1 = ""

        iIndex = 0
        while (iIndex < iLen) {
            strValue1 = strData.substring(iIndex * 2, iIndex * 2 + 1)
            iValue1 = strValue1.toInt(16)
            iValue1 = iValue1 * 16

            strValue1 = strData.substring(iIndex * 2 + 1, iIndex * 2 + 2)
            iValue1 = iValue1 + strValue1.toInt(16)
            bData1[iIndex] = iValue1.toByte()
            iIndex++
        }

        return bData1
    }

    /**
     * JNA数组转字符串
     * @param bData
     * @return
     */
    fun JNAByteToString(bData: ByteArray): String {
        var iIndex = 0
        var iValue1 = 0
        var iValue2 = 0
        var strValue1 = ""
        val iLen = bData.size
        iIndex = 0
        while (iIndex < iLen) {
            iValue1 = (bData[iIndex] + 256) % 256
            iValue2 = (iValue1 shr 4) //0-15
            strValue1 = strValue1 + String.format("%x", iValue2)
            iValue2 = (iValue1 % 0x10) //0-15
            strValue1 = strValue1 + String.format("%x", iValue2)
            iIndex++
        }
        return strValue1.uppercase(Locale.getDefault())
    }

    fun JNAByteToString(bData: ByteArray, iIndex: Int, iLen: Int): String {
        var iIndex = iIndex
        var iValue1 = 0
        var iValue2 = 0
        var strValue1 = ""
        iIndex = iIndex
        while (iIndex < iLen) {
            try {
                iValue1 = (bData[iIndex] + 256) % 256
                iValue2 = (iValue1 shr 4) //0-15
                strValue1 = strValue1 + String.format("%x", iValue2)
                iValue2 = (iValue1 % 0x10) //0-15
                strValue1 = strValue1 + String.format("%x", iValue2)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            iIndex++
        }
        return strValue1.uppercase(Locale.getDefault())
    }



    /**
     * 3.22 打印QR码
     * @param strData  内容
     * @param iLmargin 左边距，取值0-27 单位mm
     * @param iMside   单位长度，即QR码大小，取值1-8，（有些打印机型只支持1-4）
     * @param iRound   环绕模式，1 环绕（混排，有些机型不支持）、0立即打印（不混排）
     */
    /*fun PrintQrcode(strData: String?, iLmargin: Int, iMside: Int, iRound: Int): ByteArray? {
        try {
            val iResult: Int = JNAData1.INSTANCE.Data1PrintQrcode(strData, iLmargin, iMside, iRound)
            if (iResult > 0) {
                val strPrintData: String = JNAData1.INSTANCE.Data1GetPrintDataA() ?: ""
                val bData = JNAStringToByte(strPrintData, iResult)
                JNAData1.INSTANCE.Data1Release()
                return bData
            }
        } catch (e: Exception) {
            println(e.message)
        }
        return null
    }*/

    /**
     * 打印磁盘BMP文件，仅支持单色BMP文件
     * @param strPath 图片路径
     */


    /**3.47
     * @param iNums    位图数量(单个文件最大64K，所有文件最大192K)
     * @param strPath  图像文件路径（若只有文件名则使用当前路径，
     * 若指定全路径则使用指定的路径），以”;”分隔，个数需和iNums参数一致
     */


    //状态值英文解析
    fun getStatusDescriptionEn(iStatus: Int): String {
        var strResult = ""
        when (iStatus) {
            0 -> strResult = "Printer is ready"
            1 -> strResult = "Printer is offline or no power"
            2 -> strResult = "Printer called unmatched library"
            3 -> strResult = "Printer head is opened"
            4 -> strResult = "Cutter is not reset"
            5 -> strResult = "Printer head temp is abnormal"
            6 -> strResult = "Printer does not detect blackmark"
            7 -> strResult = "Paper out"
            8 -> strResult = "Paper low"
        }
        return strResult
    }
}
