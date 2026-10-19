package com.feasycom.feasyhome;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.lky.toucheffectsmodule.TouchEffectsManager;
import com.lky.toucheffectsmodule.types.TouchEffectsViewType;
import com.lky.toucheffectsmodule.types.TouchEffectsWholeType;
import com.tencent.bugly.crashreport.CrashReport;

import me.jessyan.autosize.AutoSizeConfig;


public class HomeApp extends Application {


    static {

        TouchEffectsManager.build(TouchEffectsWholeType.SCALE)//设置全局使用哪种效果
                .addViewType(TouchEffectsViewType.ALL)//添加哪些View支持这个效果
                .setListWholeType(TouchEffectsWholeType.SCALE);//为父控件为列表的情况下，设置特定效果
        //  .setAspectRatioType(2f,TouchEffectsWholeType.SCALE);//宽高比大于4时启动水波纹

    }


    @Override
    public void onCreate() {
        super.onCreate();

        AutoSizeConfig.getInstance().setExcludeFontScale(true);
        //AutoSizeConfig.getInstance().setCustomFragment(true);
        //AutoSizeConfig.getInstance().setUseDeviceSize(true);
        AutoSizeConfig.getInstance().setBaseOnWidth(true);
    //    int screenHeight = (int) (AutoSizeConfig.getInstance().getInitScreenWidthDp() + (AutoSizeConfig.getInstance().getInitScreenWidthDp() * 0.1));
    //    Log.e("vvvv", "===========screenHeight============" + screenHeight);
        AutoSizeConfig.getInstance().setDesignWidthInDp(375);
        Utils.init(this);
        CrashReport.initCrashReport(getApplicationContext(), "3fbf2481a7", true);//建议在测试阶段建议设置成true，发布时设置为false。
    }


}
