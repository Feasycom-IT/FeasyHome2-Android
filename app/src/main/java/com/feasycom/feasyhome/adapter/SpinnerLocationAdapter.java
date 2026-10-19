package com.feasycom.feasyhome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.bean.GroupLocationBean;

import java.util.List;

public class SpinnerLocationAdapter extends BaseAdapter {

    private Context context;
    private List<GroupLocationBean> datas;


    public SpinnerLocationAdapter(Context context, List<GroupLocationBean> datas) {
        this.context = context;
        this.datas = datas;
    }


    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = LayoutInflater.from(context).inflate(R.layout.spinner_item, null);
        TextView text1 = view.findViewById(R.id.text1);
        text1.setText(datas.get(i).getText());

        return view;
    }


}
