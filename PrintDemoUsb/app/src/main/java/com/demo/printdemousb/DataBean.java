package com.demo.printdemousb;


import java.text.SimpleDateFormat;

public class DataBean {
    public int m_iSeqID = 0;
    public int m_iFunID = -1;
    private String m_strValue1 = "";
    private String m_strDataTime="";

    public DataBean(int iFunID, String strValue1){
        m_iSeqID++;
        m_iFunID=iFunID;
        m_strValue1 = strValue1;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
        m_strDataTime = sDateFormat.format(new java.util.Date());
    }
}
