package com.example.carscandemo.api_manager

import com.example.carscandemo.helper.AppController


object WebServices {
    private val IsUrlType = AppDomain.LIVE

    fun getDomainUrl(): String =
        when (IsUrlType) {
            AppDomain.LIVE -> ApiLive
            AppDomain.DEV -> ApiDev
        }

    private const val ApiDev = "http://192.168.1.106:4000/api/"
    private const val ApiLive = "http://192.168.1.106:4000/api/"

    private const val UsersWs = "options/users"



    private val context by lazy { AppController.instance.applicationContext }


    fun getUsersUrl(searchValue : String ? = ""): String {
        return "$UsersWs?search=$searchValue"
    }

}

enum class AppDomain {
    LIVE, DEV
}
