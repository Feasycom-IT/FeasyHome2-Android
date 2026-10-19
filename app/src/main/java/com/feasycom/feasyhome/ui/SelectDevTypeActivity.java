package com.feasycom.feasyhome.ui;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.feasycom.feasyhome.R;
import com.feasycom.feasyhome.adapter.DevTypeAdapter;
import com.feasycom.feasyhome.base.BaseActivity;
import com.feasycom.feasyhome.bean.DevTypeBean;
import com.feasycom.feasyhome.bean.RoomBean;
import com.feasycom.feasyhome.callback.ItemSelectCallBack;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.fsdk.FscMeshSdk;


public class SelectDevTypeActivity extends BaseActivity implements ItemSelectCallBack {


    private RelativeLayout rela_back;
    private View view_status;
    private RecyclerView recycler_type;
    private DevTypeAdapter devTypeAdapter;
    private List<DevTypeBean> devTypeBeans = new ArrayList<>();
    private AppCompatButton btn_next;
    private Switch switch_adv;

    private RoomBean roombean;
    private int devtype = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(SelectDevTypeActivity.this);
        BarUtils.setStatusBarLightMode(SelectDevTypeActivity.this, true);
        setContentView(R.layout.selectdevtypeactivity);

        view_status = findViewById(R.id.view_status);
        ViewGroup.LayoutParams params = view_status.getLayoutParams();
        params.height = BarUtils.getStatusBarHeight();
        view_status.setLayoutParams(params);

        roombean = (RoomBean) getIntent().getSerializableExtra("roombean");


        rela_back = findViewById(R.id.rela_back);
        rela_back.setOnClickListener(listener);
        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(listener);
        switch_adv = findViewById(R.id.switch_adv);
        switch_adv.setOnCheckedChangeListener(onCheckedChangeListener);

        recycler_type = findViewById(R.id.recycler_type);
        recycler_type.setLayoutManager(new GridLayoutManager(SelectDevTypeActivity.this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        devTypeAdapter = new DevTypeAdapter(SelectDevTypeActivity.this, SelectDevTypeActivity.this);
        recycler_type.setAdapter(devTypeAdapter);
        devTypeBeans.add(new DevTypeBean((byte) 0x01, "lighting"));
        devTypeBeans.add(new DevTypeBean((byte) 0x02, "panel"));
        devTypeBeans.add(new DevTypeBean((byte) 0x05, "curtain"));
        devTypeBeans.add(new DevTypeBean((byte) 0x04, "voice"));

        devTypeAdapter.setDevTypeBeans(devTypeBeans);

        SetSpacing();


    }


    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (compoundButton.isPressed()) {

                FscMeshSdk.getApi2().OpenIndicatorByAdv(b);

            }

        }
    };

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == rela_back) {
                finish();
            } else if (view == btn_next) {

                if (devtype == 0) {
                    ToastUtils.showShort("请选择设备类型!");
                } else {

                    FscMeshSdk.getApi2().CloseAdv();
                    if (devTypeBeans.get(select_nub).getDevType() == (byte) 0x01) {
                        Intent intent = new Intent(SelectDevTypeActivity.this, SearchUnprovisionedLight.class);
                        intent.putExtra("devTypeBean", devTypeBeans.get(select_nub));
                        intent.putExtra("roombean", roombean);
                        startActivity(intent);
                    } else if (devTypeBeans.get(select_nub).getDevType() == (byte) 0x05) {
                        Intent intent = new Intent(SelectDevTypeActivity.this, SearchUnprovisionedCurtain.class);
                        intent.putExtra("devTypeBean", devTypeBeans.get(select_nub));
                        intent.putExtra("roombean", roombean);
                        startActivity(intent);
                    } else if (devTypeBeans.get(select_nub).getDevType() == (byte) 0x02) {
                        Intent intent = new Intent(SelectDevTypeActivity.this, SearchUnprovisionedPanel.class);
                        intent.putExtra("devTypeBean", devTypeBeans.get(select_nub));
                        intent.putExtra("roombean", roombean);
                        startActivity(intent);
                    } else if (devTypeBeans.get(select_nub).getDevType() == (byte) 0x04) {
                        Intent intent = new Intent(SelectDevTypeActivity.this, SearchUnprovisionedVoice.class);
                        intent.putExtra("devTypeBean", devTypeBeans.get(select_nub));
                        intent.putExtra("roombean", roombean);
                        startActivity(intent);
                    }

                    FscMeshSdk.getApi2().Disconnect();

                }

            }

        }
    };


    private void SetSpacing() {

        // 设置横向间距
        int spanCount = 4; // 列数，根据需求设置
        int spacing = ConvertUtils.dp2px(15); // 间距大小
        boolean includeEdge = false; // 是否包括边缘

        recycler_type.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int column = position % spanCount;

                if (includeEdge) {
                    outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((100 - spacing) / spanCount)
                    outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((100 - spacing) / spanCount)

                    if (position < spanCount) { // top edge
                        outRect.top = spacing;
                    }
                    outRect.bottom = spacing; // item bottom
                } else {
                    outRect.left = column * spacing / spanCount; // column * ((100 - spacing) / spanCount)
                    outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((100 - spacing) / spanCount)
                    if (position >= spanCount) {
                        outRect.top = spacing; // item top
                    }
                }
            }
        });

    }


    private int select_nub = 0;

    @Override
    public void onSelect(int p, boolean select) {
        select_nub = p;

        for (int i = 0; i < devTypeAdapter.getDevTypeBeans().size(); i++) {
            if (i == p) {
                devTypeAdapter.getDevTypeBeans().get(i).setSelect(true);
            } else {
                devTypeAdapter.getDevTypeBeans().get(i).setSelect(false);
            }

        }

        devTypeAdapter.notifyDataSetChanged();
        devtype = devTypeAdapter.getDevTypeBeans().get(p).getDevType();

        btn_next.setBackgroundResource(R.drawable.shap_blue_ss5dp);
        btn_next.setTextColor(getResources().getColor(R.color.white));

    }


    @Override
    protected void onStop() {
        super.onStop();

        FscMeshSdk.getApi2().CloseAdv();

    }

}
