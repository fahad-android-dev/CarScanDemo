package com.example.carscandemo

import java.text.SimpleDateFormat
import java.util.Date

class DataBean(iFunID: Int, strValue1: String) {
    var m_iSeqID: Int = 0
    var m_iFunID: Int = -1
    private var m_strValue1 = ""
    private var m_strDataTime = ""

    init {
        m_iSeqID++
        m_iFunID = iFunID
        m_strValue1 = strValue1
        val sDateFormat = SimpleDateFormat("hh:mm:ss")
        m_strDataTime = sDateFormat.format(Date())
    }
}