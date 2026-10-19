package com.feasycom.feasyhome.bean;

import java.io.Serializable;

public class FamilyBean implements Serializable {

    private int id;
    private int user_id;
    private String member_id;
    private String manage_id;
    private String family_name;
    private String network_data;
    private String network_id;
    private String family_str;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getManage_id() {
        return manage_id;
    }

    public void setManage_id(String manage_id) {
        this.manage_id = manage_id;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getNetwork_data() {
        return network_data;
    }

    public void setNetwork_data(String network_data) {
        this.network_data = network_data;
    }

    public String getNetwork_id() {
        return network_id;
    }

    public void setNetwork_id(String network_id) {
        this.network_id = network_id;
    }

    public String getFamily_str() {
        return family_str;
    }

    public void setFamily_str(String family_str) {
        this.family_str = family_str;
    }
}
