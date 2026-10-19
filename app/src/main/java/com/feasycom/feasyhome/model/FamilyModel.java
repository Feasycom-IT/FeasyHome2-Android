package com.feasycom.feasyhome.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.feasycom.feasyhome.bean.DevGroupBean;
import com.feasycom.feasyhome.bean.FamilyBean;
import com.feasycom.feasyhome.bean.RoomBean;

import java.util.ArrayList;


public class FamilyModel extends ViewModel {



    private MutableLiveData<String> needrefresh;
    private MutableLiveData<Integer> roomid;
    private MutableLiveData<ArrayList<FamilyBean>> familyBeans;
    private MutableLiveData<FamilyBean> current_familyBean;
    private MutableLiveData<ArrayList<RoomBean>> roomBeans;


    public MutableLiveData<ArrayList<FamilyBean>> getFamilyBeans() {
        if (null == familyBeans) {
            familyBeans = new MutableLiveData<>();
        }
        return familyBeans;
    }

    public MutableLiveData<FamilyBean> getCurrent_familyBean() {
        if (null == current_familyBean) {
            current_familyBean = new MutableLiveData<>();
        }
        return current_familyBean;
    }

    public MutableLiveData<ArrayList<RoomBean>> getRoomBeans() {
        if (null == roomBeans) {
            roomBeans = new MutableLiveData<>();
        }
        return roomBeans;
    }


    public MutableLiveData<Integer> getRoomid() {
        if (null == roomid) {
            roomid = new MutableLiveData<>();
        }
        return roomid;
    }


    public MutableLiveData<String> getNeedrefresh() {
        if (null == needrefresh) {
            needrefresh = new MutableLiveData<>();
        }
        return needrefresh;
    }

}
