import com.sun.jna.Native
import com.sun.jna.Library

// Define a companion object to hold the native instance
interface JNAData1 : Library {
    companion object {
        // Use Native.load to load the native library and create the instance
        val INSTANCE: JNAData1 = Native.load("YourNativeLibraryName", JNAData1::class.java)
    }

    // Declare the native methods
    fun Data1GetPrintDataA(): String?
    fun Data1Release(): Int
    fun Data1PrintDataMatrix(strData: String?, iSize: Int): Int
    fun Data1PrintQrcode(strData: String?, iLmargin: Int, iMside: Int, iRound: Int): Int
    fun Data1PrintDiskbmpfile(strPath: String?): Int
    fun Data1SetNvbmp(iNums: Int, strPath: String?): Int
}
