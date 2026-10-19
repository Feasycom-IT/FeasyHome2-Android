package com.feasycom.feasyhome.bean;

import com.alibaba.fastjson.JSONArray;

import java.io.Serializable;

public class SceneBean implements Serializable {

    private int id;
    private String scene_name;
    private int family_id;
    private String scene_image;
    private String scene_code;
    private int show_popup;
    private int is_on;
    private JSONArray groupList;
    private JSONArray curtainGroupList;
    private boolean isSelect = false;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getScene_name() {
        return scene_name;
    }

    public void setScene_name(String scene_name) {
        this.scene_name = scene_name;
    }

    public int getFamily_id() {
        return family_id;
    }

    public void setFamily_id(int family_id) {
        this.family_id = family_id;
    }

    public String getScene_image() {
        return scene_image;
    }

    public void setScene_image(String scene_image) {
        this.scene_image = scene_image;
    }

    public String getScene_code() {
        return scene_code;
    }

    public void setScene_code(String scene_code) {
        this.scene_code = scene_code;
    }

    public int getShow_popup() {
        return show_popup;
    }

    public void setShow_popup(int show_popup) {
        this.show_popup = show_popup;
    }

    public int getIs_on() {
        return is_on;
    }

    public void setIs_on(int is_on) {
        this.is_on = is_on;
    }

    public JSONArray getGroupList() {
        return groupList;
    }

    public void setGroupList(JSONArray groupList) {
        this.groupList = groupList;
    }

    public JSONArray getCurtainGroupList() {
        return curtainGroupList;
    }

    public void setCurtainGroupList(JSONArray curtainGroupList) {
        this.curtainGroupList = curtainGroupList;
    }


    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
