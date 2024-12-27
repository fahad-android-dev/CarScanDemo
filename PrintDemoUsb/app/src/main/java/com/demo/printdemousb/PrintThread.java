package com.demo.printdemousb;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;

import com.msprintsdk.PrintCmd;
import com.msprintsdk.UsbDriver;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;
import static com.demo.printdemousb.MainActivity.check_cut;
import static com.demo.printdemousb.MainActivity.alertDialog1;
import static com.demo.printdemousb.MainActivity.base64Data;
import static com.demo.printdemousb.MainActivity.m_edtImage;
import static com.demo.printdemousb.MainActivity.m_edtTextCycles;
import static com.demo.printdemousb.MainActivity.edtText5;
import static com.demo.printdemousb.MainActivity.m_printerQueueList;
import static com.demo.printdemousb.MainActivity.num;
import static com.msprintsdk.PrintCmd.PrintFeedDot;
import static com.msprintsdk.UtilsTools.PrintBase64;
import static com.msprintsdk.UtilsTools.convertToBlackWhite;
import static com.msprintsdk.UtilsTools.data;
import static com.msprintsdk.UtilsTools.getExtensionName;
import static com.msprintsdk.UtilsTools.hexStringToBytes;
import static com.msprintsdk.UtilsTools.hexToByteArr;
import static com.msprintsdk.PrintCmd.PrintDiskImagefile;
import static com.msprintsdk.UtilsTools.unicodeToUtf8;

public class PrintThread extends Activity implements Runnable {
    UsbDriver mUsbDriver = MainActivity.mUsbDriver;
    public boolean m_blnRun = true;
    MainActivity mainActivity = new MainActivity();
    Handler handler =  new MainActivity.MyHandler(mainActivity);

    public static InputStream  txt = MainActivity.txt;
    @Override
    public  void run() {
        Looper.prepare();
        DataBean printData;
        try {
            while (m_blnRun) {
                if (mUsbDriver != null) {
                    while ((printData = m_printerQueueList.poll()) != null) {
                        System.out.println(m_printerQueueList.poll());
                        System.out.println(printData);
                        switch (printData.m_iFunID) {
                            case 1:
                                String strValue = mainActivity.spinnerpaired.getSelectedItem().toString();
                                try{
                                    ShowPrintClick(strValue);
                                }catch (Exception e){
                                    Message message = new Message();
                                    message.what = 4;
                                    message.obj = e.getMessage();;
                                    handler.sendMessage(message);
                                }
                                break;
                            case 2:
                                PrintStatus();
                                break;
                            case 3:
                                PrintBmp();
                                break;
                            case 5:
                                handler.postDelayed(runnable, 2000);
                                break;
                            case 6:
                                num=0;
                                handler.removeCallbacks(runnable);
                                break;
                            case 7:
                                String str = edtText5.getText().toString();
                                mUsbDriver.write(PrintCmd.SetClean());  // 初始化，清理缓存
                                mUsbDriver.write(PrintCmd.SetReadZKmode(0));
                                mUsbDriver.write(PrintCmd.PrintString(str, 0));
                                mUsbDriver.write(PrintCmd.PrintFeedline(5)); // 打印走纸2行
                                if(check_cut.isChecked()){
                                    mUsbDriver.write(PrintCmd.PrintCutpaper(0));
                                }
                                break;
                            case 8:
                                String textCOMA = edtText5.getText().toString();
                                textCOMA = textCOMA.replace(" ","");
                                if(isHexStrValid(textCOMA.toUpperCase())){
                                    byte[] bytes = hexToByteArr(textCOMA);//将字符串形式表示的十六进制数转换为byte数组-
                                    mUsbDriver.write(bytes);
                                    if(check_cut.isChecked()){
                                        mUsbDriver.write(PrintCmd.PrintFeedline(5)); // 打印走纸
                                        mUsbDriver.write(PrintCmd.PrintCutpaper(0));
                                    }
                                }else{
                                    Message message = new Message();
                                    message.what = 4;
                                    message.obj = "Please enter hexadecimal";
                                    handler.sendMessage(message);
                                }
                                break;
                            case 9:
                                CashboxStatus();
                                break;
                        }
                        Thread.sleep(200);
                    }
                } else {
                    Thread.sleep(100);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Looper.loop();
    }

    public static boolean isHexStrValid(String str) {
        String pattern = "^[0-9A-F]+$";
        return Pattern.compile(pattern).matcher(str).matches();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int iStatus = -1;
            iStatus = PrintStatus();
            // TODO Auto-generated method stub
            // 要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
            handler.postDelayed(this, 2000);
            int iCount = Integer.valueOf(m_edtTextCycles.getText().toString());
                String strValue = mainActivity.spinnerpaired.getSelectedItem().toString();
                ShowPrintClick(strValue);
                String Status="";
                if(iStatus == 0){
                    Status = "";
                }else

                    if (iStatus == -1){
                        Status = "     Status: Printer is offline or no power";
                    }else{
                        Status = "     Status:" + PrintCmd.getStatusDescriptionEn(iStatus);
                    }
                    String msg = iCount+" copies need to be printed and  "+(++num)+" copies has been printed"+'\n';
                    alertDialog1.setMessage(msg+Status);
                    alertDialog1.show();
                    Message message = Message.obtain();
                    message.what = 4;
                    message.obj = msg;
                    handler.sendMessage(message);
                if (num >= iCount || iStatus != 0) {
                    Button button = alertDialog1.getButton(AlertDialog.BUTTON_NEUTRAL);
                    if(null==button){
                        Log.i("carter", "button is null");
                    }else{
                        button.setText("Ok");
                    }
                    num=0;
                    handler.removeCallbacks(runnable);
                }
            }
    };

    //显示记录并走纸
    public void ShowPrintClick(String strValue) {
        try {
            int width, heigh;
            Message message = Message.obtain();
            message.what = 4;
            message.obj = strValue;
            handler.sendMessage(message);
            if (strValue.equals("PrintFeedline")) {
                mUsbDriver.write(PrintCmd.PrintFeedline(2));
            } else if (strValue.equals("PrintSelfcheck")) {
                mUsbDriver.write(PrintCmd.PrintSelfcheck());
            } else if (strValue.equals("PrintDrawable")) {
                Bitmap bitmap = null;
                BitmapDrawable bd = (BitmapDrawable) mainActivity.drawable;
                bitmap = bd.getBitmap();
                bitmap = convertToBlackWhite(bitmap);
                width = bitmap.getWidth();
                heigh = bitmap.getHeight();
                int iDataLen = width * heigh;
                int[] pixels = new int[iDataLen];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh);
                int[] data1 = pixels;
                mUsbDriver.write(PrintDiskImagefile(data1, width, heigh));
                mUsbDriver.write(PrintCmd.PrintFeedline(7)); // 打印走纸
                mUsbDriver.write(PrintCmd.PrintCutpaper(0));
            } else if (strValue.equals("PrintBase64")) {
                Bitmap bitmap = null;
                try {
                    byte[] bytes = Base64.decode(base64Data.split(",")[1], Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    bitmap = convertToBlackWhite(bitmap);

                    width = bitmap.getWidth();
                    heigh = bitmap.getHeight();
                    int iDataLen = width * heigh;
                    int[] pixels = new int[iDataLen];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh);
                    int[] data1 = pixels;
                    mUsbDriver.write(PrintDiskImagefile(data1, width, heigh));
                    mUsbDriver.write(PrintCmd.PrintFeedline(10));
                    mUsbDriver.write(PrintCmd.PrintCutpaper(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (strValue.equals("Example01")) {
                byte[] bSendData;
                String strdata = "1D 76 30 00 30 00 5D 00 00 00 00 00 00 00 00 00 00 00 03 FF C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3F FF F8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF FF FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF FF FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF FF FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F FF FF FF C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF E0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 F0 00 0F FF F0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 00 03 FF FE 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 00 03 FF FE 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 70 00 00 00 3F FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 1F FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF F0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F FF FF 80 00 01 FE 00 00 00 00 3F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F FF FF 80 00 01 FE 00 00 00 00 3F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F FF FF 80 00 03 FE 00 00 00 00 3F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F FF FF 80 00 03 FE 00 00 00 00 7F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F FF FF 80 00 03 FE 00 00 00 00 7F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F FF FF 80 00 03 FC 00 00 00 00 7F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C0 00 00 00 00 00 00 00 00 00 7F 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C0 00 00 00 00 00 00 00 00 00 7F 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C0 00 00 00 00 00 00 00 00 00 7F 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C0 00 00 00 0F FC 00 FF F8 0F FF FC 07 FF F0 0F F0 7F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C0 00 00 00 0F FC 00 FF F8 0F FF FC 07 FF F0 0F F0 7F C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 00 00 00 0F FC 07 FF FF 0F FF FC 3F FF F8 0F F3 FF E0 00 00 00 00 00 00 00 00 00 00 00 00 C0 00 00 30 C0 00 00 00 00 00 00 18 00 00 00 00 FF 00 00 00 00 0F F8 1F FF FF 1F FF FC FF FF FC 0F FF FF F0 00 00 00 30 00 30 00 00 00 00 00 00 E0 00 00 30 C0 00 00 33 06 00 00 1C 00 00 00 03 FF FF FE 03 C0 0F F8 1F F3 FF C0 FF 81 FF 8F FC 1F FF FF F0 00 00 00 30 07 F8 00 00 00 00 00 00 E0 00 00 71 C0 00 06 3B 1F 00 00 38 00 00 00 03 FF FF FE 03 C0 0F F8 FF C0 7F C0 FF 01 FF 03 FE 1F F0 3F F0 00 00 00 1F FC 30 00 01 80 00 00 60 E0 00 00 71 80 00 07 F3 F6 00 00 30 60 00 00 03 FF FF FE 03 C0 0F F8 FF C0 7F C0 FF 01 FF 03 FE 1F F0 3F F0 00 00 00 1C 30 30 00 01 80 00 00 30 C0 00 00 E1 80 C0 06 33 06 00 00 67 F0 00 00 03 FF FF FE 03 C0 0F F8 FF C0 7F C0 FF 01 FF 03 FE 1F F0 3F F0 00 00 00 18 30 30 00 00 C0 00 00 1C C0 00 00 C3 1F C0 06 33 06 00 00 FC E0 00 00 03 FF FF F0 0F C0 3F F9 FF C0 7F C0 FF 03 FC 03 FE 1F E0 3F F0 00 00 00 18 30 30 00 00 E0 00 00 1C C0 00 01 C7 F1 80 06 33 0C 00 01 C1 C0 00 00 03 FF FF F0 0F C0 3F F9 FF C1 FF C7 FF 03 FC 03 FE 1F E0 3F E0 00 00 00 18 30 30 00 00 60 00 00 0C C0 00 03 C6 33 00 06 33 6C 00 03 F3 80 00 00 03 FF 00 00 00 00 3F F1 FF FF FF C7 FF 0F FC 03 FE 1F E0 7F E0 00 00 00 18 37 B0 00 00 71 80 00 01 C0 00 07 CC 33 00 06 F3 3C 00 07 1F 00 00 00 0F FE 00 00 00 00 3F F1 FF FF FF C7 FF 0F FC 03 FC 1F E0 7F E0 00 00 00 1B FE 30 00 00 70 C0 00 01 80 C0 06 DB 30 00 07 F3 1C 00 0E 0E 00 00 00 0F FE 00 00 00 00 3F F3 FF 00 00 07 FE 0F F8 03 FC FF E0 7F E0 00 00 00 18 30 30 00 C0 00 60 00 01 9F C0 0C C3 BC 00 06 33 00 00 18 1F 80 00 00 0F FE 00 00 00 00 3F F3 FF 00 00 07 FE 0F F8 03 FC FF E0 7F E0 00 00 00 18 30 30 00 C0 00 38 00 3F F1 C0 18 C7 37 00 06 33 0F 00 00 39 C0 00 00 0F FE 00 00 00 00 3F F3 FF 00 00 07 FE 0F F8 03 FC FF E0 7F E0 00 00 00 18 30 30 00 D8 00 3C 01 F3 81 80 00 CE 33 80 06 33 FE 00 00 F8 F0 00 00 0F FE 00 00 00 00 7F F3 FF 00 00 07 FE 0F F8 03 FC FF C0 7F C0 00 00 00 18 31 B0 01 D8 00 1C 00 03 01 80 00 D8 31 C0 06 F3 06 00 01 DC 3C 00 00 0F FE 00 00 00 00 7F F3 FF 01 FF 0F FE 0F F8 0F F8 FF C0 7F C0 00 00 00 18 7F B0 01 D8 00 0C 00 03 01 80 00 F1 B0 C0 07 B3 C6 00 07 1C 1F 00 00 0F F8 00 00 00 00 7F 83 FF 03 FC 0F FE 0F F8 3F F8 FF C0 7F C0 00 00 00 1F F0 30 01 9C 00 00 00 07 01 80 01 C0 F0 00 0C 33 6C 00 1C 18 7F F0 00 1F FF FF E0 00 00 7F 81 FF DF FC 0F FF 0F FF 7F F1 FF C1 FF C0 00 00 00 30 30 30 01 8C 00 00 00 06 E1 80 00 C0 70 00 0C 33 6C 00 70 1F F3 00 00 1F FF FF E0 00 00 7F 81 FF FF F8 0F FF 83 FF FF 81 FF C1 FF C0 00 00 00 30 30 30 03 8C 03 00 00 06 73 80 00 06 3C 00 0C 33 3C 00 C7 F8 60 00 00 1F FF FF E0 00 00 7F 81 FF FF F8 0F FF 83 FF FF 81 FF C1 FF C0 00 00 00 30 30 30 07 86 03 00 00 0C 33 00 03 03 87 00 0C 33 18 00 00 38 60 00 00 1F FF FF E0 00 00 7F 81 FF FF F8 0F FF 83 FF FF 81 FF C1 FF C0 00 00 00 30 30 30 03 06 03 00 00 1C 33 00 03 61 C3 C0 0C 33 18 00 00 30 60 00 00 1F FF FF E0 00 00 FF 80 FF FF F0 0F FF 01 FF FF 01 FF 01 FF 00 00 00 00 60 30 30 03 03 01 80 00 18 03 00 03 60 C0 E0 18 33 3C 00 00 70 60 00 00 1F FF FF E0 00 00 FF 80 0F FE 00 07 FF 00 FF FC 01 FF 01 FF 00 00 00 00 60 30 30 00 03 81 80 00 30 03 00 07 30 00 60 18 33 7E 00 00 60 E0 00 00 00 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 E0 30 30 00 01 E1 80 00 71 87 00 07 18 00 00 18 33 67 00 00 E0 C0 00 00 00 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 31 B0 00 00 79 C0 00 E0 C6 00 0E 0C 0C 00 31 B3 C3 C0 01 D8 C0 00 00 00 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 80 30 F0 00 00 0F C0 01 C0 76 00 0E 07 0C 00 30 F3 C3 F0 03 8E C0 00 00 00 00 00 00 00 03 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 30 70 00 00 00 00 03 80 3E 00 00 01 CE 00 60 73 80 00 06 07 C0 00 00 00 00 00 00 00 03 FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 30 00 00 00 00 06 00 1C 00 00 00 7E 00 00 33 00 00 0C 03 80 00 00 00 00 00 00 00 1F FE 00 00 00 00 1F F0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 00 0C 00 00 00 00 00 00 00 00 00 38 01 80 00 00 00 00 00 00 00 1F FE 00 00 00 00 3F E0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F FC 00 00 00 00 FF E0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F FC 00 00 00 00 FF E0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 FF 80 00 00 00 01 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF 80 00 21 E0 01 83 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 0F FF 00 00 3E 40 01 02 00 00 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 0F FF 00 00 27 80 01 02 60 21 E4 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3F F8 00 00 78 80 02 3F 80 10 48 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 7F F0 00 00 40 80 02 44 00 10 30 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 C0 00 00 00 01 FF E0 00 00 4F 80 1F 87 80 02 3E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 C0 00 00 00 01 FF E0 00 00 F8 00 24 79 00 03 C2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 00 00 00 03 FF C0 00 00 A0 00 04 09 E0 12 44 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E0 00 00 00 0F FF 00 00 00 47 E0 06 FF 03 E3 F4 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E0 00 00 00 0F FF 00 00 00 FA 40 19 12 00 44 88 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 38 00 00 00 3F FC 00 00 03 24 80 68 1E 00 44 88 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 00 03 FF F8 00 00 04 48 81 8B F0 00 47 E8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 80 00 0F FF F0 00 00 18 91 00 11 20 00 49 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 80 00 0F FF F0 00 00 23 22 00 11 3C 00 89 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 FC 03 FF FF 80 00 00 04 42 00 12 20 00 9A A0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 FC 03 FF FF 80 00 00 18 84 00 23 C0 0F D2 60 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FC 00 00 00 23 28 01 24 60 00 38 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F FF FF F0 00 00 00 0C 30 00 C8 18 00 07 8C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF E0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF E0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF E0 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ";
                bSendData = hexStringToBytes(strdata);
                mUsbDriver.write(bSendData);

                mUsbDriver.write(PrintCmd.SetReadZKmode(0));
                PrintFeedDot(30);
                StringBuilder m_sbData;
                m_sbData = new StringBuilder("店号：8888          机号：100001");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("电话:0755-12345678");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                //PrintFeedDot(20);
                m_sbData = new StringBuilder("收银：01-店长");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("时间：" + data());
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("-------------------------------");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                byte[] bByte = new byte[3];
                bByte[0] = 12;
                bByte[1] = 18;
                bByte[2] = 26;
                mUsbDriver.write(PrintCmd.SetHTseat(bByte, 3));

                m_sbData = new StringBuilder("代码");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("单价");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("数量");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("金额");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));

                m_sbData = new StringBuilder("48572819");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("2.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("3.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("6.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("怡宝矿泉水");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("48572820");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("2.50");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("2.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("5.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("百事可乐(罐装)");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("-------------------------------");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("合计：");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("5.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("11.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));

                m_sbData = new StringBuilder("优惠：");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder(" 0.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));

                m_sbData = new StringBuilder("应付：");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("11.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));

                m_sbData = new StringBuilder("微信支付：");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder("11.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));

                m_sbData = new StringBuilder("找零：");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                mUsbDriver.write(PrintCmd.PrintNextHT());
                m_sbData = new StringBuilder(" 0.00");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));

                m_sbData = new StringBuilder("-------------------------------");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("会员：");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("券号：");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("-------------------------------");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                PrintFeedDot(20);
                m_sbData = new StringBuilder("手机易捷通：ejeton.com.cn ");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("客户热线：400-6088-160");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("微信号：ejeton ");
                mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
                m_sbData = new StringBuilder("http://weixin.qq.com/r/R3VZQQDEi130rUQi9yBV");
                mUsbDriver.write(PrintCmd.PrintQrcode(unicodeToUtf8(m_sbData.toString()), 10, 5, 0));
                mUsbDriver.write(PrintCmd.PrintFeedline(5));
                mUsbDriver.write(PrintCmd.PrintCutpaper(1));

            } else if (strValue.equals("AutomaticOpen")) {
                byte[] bytes = hexToByteArr("1378");
                mUsbDriver.write(bytes);
            }
        }catch (Exception e){
            Message message = Message.obtain();
            message.what = 4;
            message.obj = e.getMessage();
            handler.sendMessage(message);
        }
}



//打印图片
    public int PrintBmp() {
        int iResult = 1;
        byte [] byteImg = new byte[0];
        try {
            String strValue = m_edtImage.getText().toString().trim();//edtText3.getText().toString().trim()
            if (strValue.equals("")) {
                Message message = Message.obtain();
                message.what = 4;
                message.obj = "Please select bmp file...";
                handler.sendMessage(message);
            } else {
                if (getExtensionName(strValue).equals("txt")) {
                    try{
                        byteImg = PrintBase64(strValue);
                    }catch (Exception e){
                        Message message = new Message();
                        message.what = 4;
                        message.obj = "Base64 file is an error text file";
                        handler.sendMessage(message);
                    }

                    iResult = 0;
                }
                else{
//                    int[] data1 = getBitmapParamsData(strValue);//图片转数组
                         byteImg = PrintCmd.PrintDiskImagefile(strValue);
                    Message message = Message.obtain();
                    message.what = 4;
                    message.obj = "Print image:" + strValue;
                    handler.sendMessage(message);
                }
                    int iValue = mUsbDriver.write(byteImg);
                    mUsbDriver.write(PrintCmd.PrintFeedline(5));
                    mUsbDriver.write(PrintCmd.PrintCutpaper(0));

                    if (iValue > 0)
                        iResult = 0;

            }
        } catch (Exception e) {
            Message message = Message.obtain();
            message.what = 4;
            message.obj = e.getMessage();
            handler.sendMessage(message);
            Log.e(TAG, "PrintBmp:" + e.getMessage());
        }
        return iResult;
    }

    private int PrintStatus() {
        int iResult = 1;
        try {
            int iValue = -1;
            byte[] bRead1 = new byte[1];
            String strValue = "";
            Message message = Message.obtain();
            message.what = 4;
            if (mUsbDriver.read(bRead1, PrintCmd.GetStatus1()) > 0) {
                iValue = PrintCmd.CheckStatus1(bRead1[0]);
                if(iValue!=0) {
                    strValue = PrintCmd.getStatusDescriptionEn(iValue);
                    message.obj = strValue;
                    handler.sendMessage(message);
                }
            }

            if (iValue == 0) {
                iValue = -1;
                if (mUsbDriver.read(bRead1, PrintCmd.GetStatus2()) > 0) {
                    iValue = PrintCmd.CheckStatus2(bRead1[0]);
                    if(iValue!=0) {
                        strValue = PrintCmd.getStatusDescriptionEn(iValue);
                        message.obj = strValue;
                        handler.sendMessage(message);
                    }
                }
            }

            if (iValue == 0) {
                iValue = -1;
                if (mUsbDriver.read(bRead1, PrintCmd.GetStatus3()) > 0) {
                    iValue = PrintCmd.CheckStatus3(bRead1[0]);
                    if(iValue!=0) {
                        strValue = PrintCmd.getStatusDescriptionEn(iValue);
                        message.obj = strValue;
                        handler.sendMessage(message);
                    }
                }
            }
            if (iValue == 0) {
                iValue = -1;
                if (mUsbDriver.read(bRead1, PrintCmd.GetStatus4()) > 0) {
                    iValue = PrintCmd.CheckStatus4(bRead1[0]);
                    if(iValue!=0) {
                        strValue = PrintCmd.getStatusDescriptionEn(iValue);
                        message.obj = strValue;
                        handler.sendMessage(message);
                    }
                }
            }
            if(iValue==0) {
                strValue = PrintCmd.getStatusDescriptionEn(iValue);
                message.obj = strValue;
                handler.sendMessage(message);
            }
            iResult = iValue;
        } catch (Exception e) {
            Message message = Message.obtain();
            message.what = 4;
            message.obj = e.getMessage();
            handler.sendMessage(message);

            Log.e(TAG, "PrintStatus:" + e.getMessage());
        }
        return iResult;
    }

    private int CashboxStatus()
    {
        int iResult = -1;
        try {
            byte[] bRead1 = new byte[1];
            byte[] bCmd = new byte[7];
            int iIndex=0;
            bCmd[iIndex++]=0x1B;
            bCmd[iIndex++]=0x72;
            bCmd[iIndex++]=0x01;

            String strValue = "Cashbox unknown.";
            Message message = Message.obtain();
            message.what = 4;
            if (mUsbDriver.read(bRead1, bCmd) > 0)
            {
                if(bRead1[0]==1)
                {
                    strValue = "Cashbox open.";
                }
                else
                {
                    strValue = "Cashbox close.";
                }
            }
            message.obj = strValue;
            handler.sendMessage(message);
        } catch (Exception e) {
            Message message = Message.obtain();
            message.what = 4;
            message.obj = e.getMessage();
            handler.sendMessage(message);

            Log.e(TAG, "CashboxStatus:" + e.getMessage());
        }
        return iResult;
    }


}
