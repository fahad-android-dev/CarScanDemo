package com.demo.printdemousb;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.msprintsdk.UsbDriver;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.demo.printdemousb.GetPathFromUri.getStoragePath;
import static com.msprintsdk.UtilsTools.ReadTxtFile;
import static com.msprintsdk.UtilsTools.getFromRaw;
import static javax.xml.transform.OutputKeys.ENCODING;


public class MainActivity extends AppCompatActivity {
    public static Spinner spinnerpaired;
    public static Spinner spinnerstutas;
    public static Button mbtn_getStatus;
    public static Button mbtn_receiptPrint;
    public static Button mbtn_imgPrint;
    public static Button mbtn_CyclesPrint;
    public static Button mbtn_findFile;
    public static CheckBox check_cut;

    public static CheckBox check_hex;
    public static Button mbtn_printContent;


    public static EditText m_edtTextList;
    public static EditText m_edtImage;
    public static EditText m_edtTextCycles;
    public static EditText edtText5;
    public static Queue<DataBean> m_printerQueueList = new LinkedList<DataBean>();
    static StringBuilder m_sbEdtText = new StringBuilder("");
    static SimpleDateFormat m_sdfDate = new SimpleDateFormat("HH:mm:ss ");
    private static final String ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION";
    static UsbDriver mUsbDriver;
    UsbDevice mUsbDevice = null;
    static final String[] m_nStr_001 = {"PrintSelfcheck","Example01", "PrintDrawable", "PrintBase64", "AutomaticOpen"};
    static final String[] m_nStr_002 = {"GetStatus", "GetDevices", "GetCashbox"};
    static final int FILE_SELECT_CODE = 0;
    static final int FILE_SELECT_TXT = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE, Environment.DIRECTORY_DOWNLOADS,ACTION_USB_PERMISSION,
           };
    //    ---==================================================================================================
    private final String TAG = "PrintDemoUsb";
    public static InputStream txt;
    public static String base64Data;
    public static Drawable drawable;
    public static Drawable logo;
    static int num = 0;
    public static AlertDialog alertDialog1;
    public AlertDialog.Builder builder;
    DataBean dataBean = new DataBean(0,"");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCtrl();
        mUsbDriver = new UsbDriver((UsbManager) getSystemService(Context.USB_SERVICE), this);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mUsbDriver.setPermissionIntent(permissionIntent);
        // Broadcast listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        this.registerReceiver(mUsbReceiver, filter);

        builder = new AlertDialog.Builder(MainActivity.this);
        txt = getResources().openRawResource(R.raw.txt);
        base64Data = getFromRaw(txt);
        drawable = ContextCompat.getDrawable(this,R.mipmap.timg);
        logo = ContextCompat.getDrawable(this,R.mipmap.logo);
        alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Continuous print")//标题
                .setIcon(R.mipmap.ic_launcher)//图标
                .setNeutralButton("Stop", new DialogInterface.OnClickListener() {//添加普通按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataBean.m_iFunID = 6;
                        m_printerQueueList.add(dataBean);
                        mbtn_receiptPrint.setEnabled(true);
                        mbtn_getStatus.setEnabled(true);
                        mbtn_imgPrint.setEnabled(true);
                        mbtn_CyclesPrint.setEnabled(true);
                        mbtn_findFile.setEnabled(true);
                        check_hex.setEnabled(true);
                        mbtn_printContent.setEnabled(true);
                        Button button = alertDialog1.getButton(AlertDialog.BUTTON_NEUTRAL);
                        if(null==button){
                            Log.i("carter", "button is null");
                        }else{
                            button.setText("Stop");
                        }
                    }
                }).create();
        alertDialog1.setCanceledOnTouchOutside(false);
        new Thread(new PrintThread()).start();


    }
    protected void onDestroy() {
        super.onDestroy();
        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void initCtrl() {

        spinnerpaired = (Spinner) findViewById(R.id.spinner_001);
        ArrayAdapter<String> adapter001 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_nStr_001);
        adapter001.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerpaired.setAdapter(adapter001);

        spinnerstutas = (Spinner) findViewById(R.id.spinner_002);
        ArrayAdapter<String> adapter002 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_nStr_002);
        adapter002.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerstutas.setAdapter(adapter002);

        mbtn_receiptPrint = (Button) findViewById(R.id.btn_Print1);
        mbtn_receiptPrint.setOnClickListener(new PrintClickListener());

        mbtn_imgPrint = (Button) findViewById(R.id.btn_Print3);
        mbtn_imgPrint.setOnClickListener(new PrintClickListener());

        mbtn_CyclesPrint = (Button) findViewById(R.id.btn_Print4);
        mbtn_CyclesPrint.setOnClickListener(new PrintClickListener());

        mbtn_findFile = (Button) findViewById(R.id.btn_Print6);
        mbtn_findFile.setOnClickListener(new CheckClickListener());

        check_hex = (CheckBox) findViewById(R.id.checkHex);
        check_hex.setOnClickListener(new PrintClickListener());

        mbtn_printContent = (Button) findViewById(R.id.btn_Print8);
        mbtn_printContent.setOnClickListener(new PrintClickListener());

        check_cut = (CheckBox) findViewById(R.id.checkCut);
        check_cut.setOnClickListener(new PrintClickListener());

        mbtn_getStatus = (Button) findViewById(R.id.btn_GetStatus);
        mbtn_getStatus.setOnClickListener(new GetClickListener());

        m_edtTextList = (EditText) findViewById(R.id.editText2);
        m_edtTextList.setText("");

        m_edtImage = (EditText) findViewById(R.id.editText3);
        m_edtImage.setText("");
        m_edtImage.setOnClickListener(new CheckClickListener());
        m_edtImage.setInputType(InputType.TYPE_NULL);

        m_edtTextCycles = (EditText) findViewById(R.id.editText4);

        edtText5 = (EditText) findViewById(R.id.editText5);
        edtText5.setText("");
    }

    class CheckClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            try {
                switch (view.getId()) {
                    case R.id.editText3:
                        showFileChooser();
                        break;
                    case R.id.btn_Print6:
                        showFileTXTChooser();
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "CheckClickListener:" + e.getMessage());
            }
        }
    }

    /**
     * 获取usb权限
     */
    public int usbDriverCheck() {
        int iResult = -1;
        try {

            if (!mUsbDriver.isUsbPermission()) {
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                mUsbDevice = null;
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                            || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
                        mUsbDevice = device;
                        ShowMessage( "DeviceClass:" + device.getDeviceClass() + ";DeviceName:" + device.getDeviceName());
                    }
                }
                if (mUsbDevice != null) {
                    iResult = 1;
                    if (mUsbDriver.usbAttached(mUsbDevice)) {
                        if (mUsbDriver.openUsbDevice(mUsbDevice))
                            iResult = 0;
                    }
                }
            } else {
                if (!mUsbDriver.isConnected()) {
                    if (mUsbDriver.openUsbDevice(mUsbDevice))
                        iResult = 0;
                } else {
                    iResult = 0;
                }
            }
        } catch (Exception e) {

            Log.e(TAG, "usbDriverCheck:" + e.getMessage());
        }

        return iResult;
    }

    /**
     * 打印机状态查询
     */
    class GetClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            try {
                switch (view.getId()) {
                    case R.id.btn_GetStatus:
                        String Value = spinnerstutas.getSelectedItem().toString();
                        if(Value.equals("GetDevices"))
                        {
                            CheckDevices();
                        }
                        else {
                            int iDriverCheck = usbDriverCheck();

                            if (iDriverCheck == -1) {
                                ShowMessage("Printer not connected!");
                                return;
                            }

                            if (iDriverCheck == 1) {
                                ShowMessage("Printer unauthorized!");
                                return;
                            }

                            if (Value.equals("GetStatus")) {
                                dataBean.m_iFunID = 2;
                                m_printerQueueList.add(dataBean);
                            } else if (Value.equals("GetCashbox")) {
                                dataBean.m_iFunID = 9;
                                m_printerQueueList.add(dataBean);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                ShowMessage(e.getMessage());
                Log.e(TAG, "GetClickListener:" + e.getMessage());
            }
        }
    }

    /**
     * 各功能打印
     */

    class PrintClickListener implements View.OnClickListener {
        int iDriverCheck;
        @Override
        public void onClick(View view) {
            try {
                     iDriverCheck = usbDriverCheck();
                if (iDriverCheck == -1) {
                    ShowMessage("Printer not connected!");
                    return;
                }

                if (iDriverCheck == 1) {
                    ShowMessage("Printer unauthorized!");
                    return;
                }

                switch (view.getId()) {
                    case R.id.btn_Print1:
                        dataBean.m_iFunID = 1;
                        m_edtTextList.getText();
                        m_printerQueueList.add(dataBean);
                        break;
                    case R.id.editText3:
                        showFileChooser();
                    case R.id.btn_Print3:
                        dataBean.m_iFunID = 3;
                        m_printerQueueList.add(dataBean);
                        break;
                    case R.id.btn_Print4:
                        dataBean.m_iFunID = 5;
                        m_printerQueueList.add(dataBean);
                        mbtn_receiptPrint.setEnabled(false);
                        mbtn_getStatus.setEnabled(false);
                        mbtn_imgPrint.setEnabled(false);
                        mbtn_CyclesPrint.setEnabled(false);
                        mbtn_findFile.setEnabled(false);
                        check_hex.setEnabled(false);
                        mbtn_printContent.setEnabled(false);
                        break;
                    case R.id.btn_Print8:
                        if(check_hex.isChecked()){
                            dataBean.m_iFunID = 8;
                            m_printerQueueList.add(dataBean);
                            break;
                        }else {
                            dataBean.m_iFunID = 7;
                            m_printerQueueList.add(dataBean);
                            break;
                        }
                    default:
                        break;
                }
            } catch (Exception e) {
                ShowMessage(e.getMessage());
                Log.e(TAG, "PrintClickListener:" + e.getMessage());
            }
        }
    }

    // handler对象，接收消息
    static class MyHandler extends Handler
    {
        WeakReference<Activity> mWeakReference;
        public MyHandler(Activity activity)
        {
            mWeakReference=new WeakReference<Activity>(activity);
        }
        @Override
        public void handleMessage(Message msg)
        {
            final Activity activity=mWeakReference.get();
            // 处理从子线程发送过来的消息
            int arg1 = msg.arg1;  //获取消息携带的属性值
            int arg2 = msg.arg2;
            int what = msg.what;
            Object result = msg.obj;
            switch(what)
            {
                case 0:
                    break;
                case 3:
                case 4:
                    ShowMessage(result.toString());
                    break;
                default:
                    break;
            }
        }
    }


    void CheckDevices() throws IOException {
        String strValue = "";
        int iIndex = 0;
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            iIndex++;
            strValue = strValue + String.valueOf(iIndex) + " DeviceClass:" + device.getDeviceClass() + "; DeviceId:"+device.getDeviceId() + "; DeviceName:" + device.getDeviceName() + "; VendorId:" + device.getVendorId() +
                    "; \r\nProductId:" + device.getProductId() + "; InterfaceCount:"+device.getInterfaceCount() +"; describeContents:" + device.describeContents() + ";\r\n" +
                    "DeviceProtocol:"+device.getDeviceProtocol() + ";DeviceSubclass:" + device.getDeviceSubclass() + ";\r\n" ;
            strValue = strValue + "****************\r\n";

        }
        if(strValue.equals(""))
        {
            strValue = "No USB device.";
        }

        ShowMessage(strValue);
    }

    //Show Message
    public static void ShowMessage(String sMsg) {
        m_sbEdtText.append(m_sdfDate.format(new Date()));
        m_sbEdtText.append(sMsg);
        m_sbEdtText.append("\r\n");
        m_edtTextList.setText(m_sbEdtText);
        m_edtTextList.setSelection(m_sbEdtText.length(), m_sbEdtText.length());
    }

    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                        || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
                    mUsbDriver.closeUsbDevice(device);
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) synchronized (this) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                            || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
                        //赋权限以后的操作
                    }
                } else {
//                    Toast.makeText(MainActivity.this, "permission denied for device",
                    ShowMessage( "permission denied for device");
                }
        }

        }
    };

    // 显示文件选择路径
    public void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a bmp file"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            ShowMessage("Please install a File Manager.");
        }
    }

    // 显示文件选择路径
    public void showFileTXTChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a txt file"), FILE_SELECT_TXT);
        } catch (android.content.ActivityNotFoundException ex) {
            ShowMessage("Please install a File Manager.");
        }
    }

    // 获取绝对路径
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Context context = getApplicationContext();
                    Uri uri = data.getData();
                    String file = GetPathFromUri.getPath(context, uri);
                    if ("File".equalsIgnoreCase(uri.getScheme()) || (file != null)) {//
                        System.out.println(file);
                        m_edtImage.setText(file);//uri.getPath().replace(":","/")uri.getPath()"/storage/emulated/0/pic/2222.bmp"
                        verifyStoragePermissions();
                    } else {
                        file = getStoragePath(context,true);
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        file = file +  "/" + split[1];//"/" + split[0] +
                        m_edtImage.setText(file);//uri.getPath().replace(":","/")uri.getPath()"/storage/emulated/0/pic/2222.bmp"
                        verifyStoragePermissions();
                    }
                }
                break;
            case FILE_SELECT_TXT:
                if (resultCode == RESULT_OK) {
                    Context context = getApplicationContext();
                    Uri uri = data.getData();
                    String file = GetPathFromUri.getPath(context, uri);
                    if ("File".equalsIgnoreCase(uri.getScheme()) || (file != null)) {//
                        String readTxt = ReadTxtFile(file);
                        edtText5.setText(readTxt);//uri.getPath().replace(":","/")uri.getPath()"/storage/emulated/0/pic/2222.bmp"
                        verifyStoragePermissions();
                    } else {
                        file = getStoragePath(context,true);
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        file = file +  "/" + split[1];
                        String readTxt = ReadTxtFile(file);
                        edtText5.setText(readTxt);
                        verifyStoragePermissions();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);//缺少什么权限就写什么权限
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }


}