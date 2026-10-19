package com.feasycom.feasyhome.utils;

import com.blankj.utilcode.util.SPUtils;

public class GenerateAddress {


    private static volatile GenerateAddress instance;

    public static GenerateAddress getInstance() {
        if (instance == null) {
            synchronized (GenerateAddress.class) {
                if (instance == null) {
                    instance = new GenerateAddress();
                }
            }
        }
        return instance;
    }



/*            1.4 组地址分配范围：
                普通灯具：0xC000-0xC03F（64 组）
                RGB 灯： 0xC040-0xC05F（32 组）
                窗帘电机：0xC060-0xC07F（32 组）
                人体感应: 0xC080-0xC09F（32 组）
                环境电器：0xC100-0xC11F（空调 32 组）、0xC120-0xC13F（新风 32 组）、0xC140-0xC15F（地暖 32 组）*/


    public int GenerateLightGroupAdr(int address_number) {
        //   普通灯具：0xC000-0xC03F（64 组）
    /*    if (SPUtils.getInstance().getInt("groupAdr", 0) == 0) {
            SPUtils.getInstance().put("groupAdr", 49152, true);
            return 49152;
        }
        if (SPUtils.getInstance().getInt("groupAdr") >= 49215) {
            SPUtils.getInstance().put("groupAdr", 49152, true);
        }
        SPUtils.getInstance().put("groupAdr", SPUtils.getInstance().getInt("groupAdr") + 1, true);
        return SPUtils.getInstance().getInt("groupAdr") + 1;*/
        return 49152 + address_number;
    }

    //   RGB 灯： 0xC040-0xC05F（32 组）
    public int GenerateCurtainGroupAdr(int address_number) {

    /*    if (SPUtils.getInstance().getInt("curtaingroupAdr", 0) == 0) {
            SPUtils.getInstance().put("curtaingroupAdr", 49248, true);
            return 49248;
        }
        if (SPUtils.getInstance().getInt("curtaingroupAdr") >= 49279) {
            SPUtils.getInstance().put("curtaingroupAdr", 49248, true);
        }
        SPUtils.getInstance().put("curtaingroupAdr", SPUtils.getInstance().getInt("curtaingroupAdr") + 1, true);
        return SPUtils.getInstance().getInt("curtaingroupAdr") + 1;*/
        return 49153+address_number;
    }


}
