package com.example.carscandemo.helper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat


object Permission {

    const val CAMERA_REQUEST_CODE = 2
    const val GALLERY_REQUEST_CODE = 1
    const val AUDIO_REQUEST_CODE = 3
    const val CAMERA_AUDIO_REQUEST_CODE = 4
    const val BLUETOOTH_CONNECT_REQUEST_CODE = 5
    const val LOCATION_REQUEST_CODE = 101
    const val STORAGE_REQUEST_CODE = 102
    const val NOTIFICATION_REQUEST_CODE = 103


    fun isNotificationCanAccess(activity: Activity) =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

            else ->
                true
        }

    fun isCameraCanAccess(activity: Activity) =
        ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun isAudioCanAccess(activity: Activity) = ActivityCompat.checkSelfPermission(
        activity,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    fun isGalleryCanAccess(activity: Activity) =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                activity.isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)
            else -> {
                activity.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }


    fun isBluetoothConnectCanAccess(activity: Activity) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }

    fun isStorageCanAccess(activity: Activity): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            else -> {
                ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun isLocationCanAccess(activity: Activity) =
        ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun requestGalleryPermission(mActivity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                mActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_REQUEST_CODE
            )

        } else {
            ActivityCompat.requestPermissions(
                mActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_REQUEST_CODE
            )
        }
    }

    fun requestStoragePermission(mActivity: Activity) {
        val storagePermission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_IMAGES
            else -> Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, storagePermission)) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(storagePermission),
                STORAGE_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(storagePermission),
                STORAGE_REQUEST_CODE
            )
        }
    }

    fun requestCameraPermission(mActivity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                mActivity,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            //  permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                mActivity, arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    fun requestCameraAudioPermission(mActivity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                mActivity,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                CAMERA_AUDIO_REQUEST_CODE
            )
        } else {
            //  permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                mActivity, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                CAMERA_AUDIO_REQUEST_CODE
            )
        }
    }

    fun requestBluetoothConnectPermission(mActivity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                mActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        ) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                BLUETOOTH_CONNECT_REQUEST_CODE
            )
        } else {
            //  permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                mActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                BLUETOOTH_CONNECT_REQUEST_CODE
            )
        }
    }

    fun requestLocationPermission(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }

    }


    fun showAlertToEnablePermission(context: Activity, strTitle: String = "") {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(strTitle)
            .setCancelable(false)
            .setPositiveButton("Enable") { dialog, id ->
                dialog.cancel()
                context.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun Context.isPermissionGranted(permission:String) : Boolean{
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

interface OnRequestPermissionsResult {
    fun onResultPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
    }
}

/**
 * <string name="text_enable">Enable</string>
 * <string name="no">No</string>
 * <string name="enable_location_to_continue">You need to enable location to continue</string>
 * */