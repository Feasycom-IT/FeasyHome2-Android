package com.feasycom.feasyhome.bean;

import java.io.Serializable;

public class DevTypeBean implements Serializable {

    private byte devType;
    private String devName;

    private boolean isSelect = false;


    public DevTypeBean(byte mdevType, String mdevName) {

        devType = mdevType;
        devName = mdevName;

    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public byte getDevType() {
        return devType;
    }

    public void setDevType(byte devType) {
        this.devType = devType;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }
}
