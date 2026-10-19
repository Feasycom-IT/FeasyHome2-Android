package com.feasycom.feasyhome.logic;


import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.feasycom.feasyhome.model.FamilyModel;

public class FamilyModelFactory {


    private static volatile FamilyModelFactory singleton = null;
    private FamilyModel familyModel;

    private FamilyModelFactory() {
    }

    public static FamilyModelFactory getInstance() {
        if (singleton == null) {
            synchronized (FamilyModelFactory.class) {
                if (singleton == null) {
                    singleton = new FamilyModelFactory();
                }
            }
        }
        return singleton;
    }

    public void setFamilyModelContext(ViewModelStoreOwner activity) {
        familyModel = new ViewModelProvider(activity).get(FamilyModel.class);
    }

    public FamilyModel getFamilyModel() {
        return familyModel;
    }
}
