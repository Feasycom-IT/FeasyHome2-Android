package com.feasycom.feasyhome.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.feasycom.feasyhome.R;

public class TipsDialogOne extends Dialog {


    public TipsDialogOne(@NonNull Context context) {
        super(context);
    }


    public TextView txt_title;
    public TextView txt_cancler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tipsdialogone);
        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawableResource(R.drawable.shap_white_ss6dp);
        txt_title = findViewById(R.id.txt_title);
        txt_cancler = findViewById(R.id.txt_cancler);


    }


}
