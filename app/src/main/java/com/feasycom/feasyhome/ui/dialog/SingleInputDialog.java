package com.feasycom.feasyhome.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.feasycom.feasyhome.R;

public class SingleInputDialog extends Dialog {


    public SingleInputDialog(@NonNull Context context) {
        super(context);


    }


    public TextView txt_title, txt_cancler, txt_ok;
    public EditText edit_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singleinputdialog);
        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawableResource(R.drawable.shap_white_ss6dp);
        txt_title = findViewById(R.id.txt_title);
        edit_name = findViewById(R.id.edit_name);
        txt_cancler = findViewById(R.id.txt_cancler);
        txt_ok = findViewById(R.id.txt_ok);

        // 监听回车键
        edit_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });


    }


}
