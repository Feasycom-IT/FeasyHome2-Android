package com.feasycom.feasyhome.api;




public class Api {
    //public static String FeasyHomeServer = "http://159.138.6.72:3030";//服务器地址
    public static String FeasyHomeServer = "https://mesh.feasycom.com";//服务器地址
    //public static String FeasyHomeServer = "https://api.feasycom.com/feasyhome";//服务器地址
    public static String IndexPageDate = FeasyHomeServer + "/api/family/index";//首页综合数据
    public static String Login = FeasyHomeServer + "/api/user/mobilelogin";//密码登录
    public static String FamilyList = FeasyHomeServer + "/api/family/familyList";//家庭列表数据
    public static String CreateFamily = FeasyHomeServer + "/api/family/createFamily";//创建家庭
    public static String DissolveNetwork = FeasyHomeServer + "/api/family/dissolveNetwork";//解散全网
    public static String DeleteFamily = FeasyHomeServer + "/api/family/deleteFamily";//删除家庭
    public static String EditFamilyName = FeasyHomeServer + "/api/family/modifyFamilyName";//修改家庭名称
    public static String RoomList = FeasyHomeServer + "/api/family/roomList";//房间列表
    public static String MemberList = FeasyHomeServer + "/api/family/memberList";//成员列表
    public static String ModifyRoomName = FeasyHomeServer + "/api/family/modifyRoomName";//修改房间名称
    public static String DeleteRoom = FeasyHomeServer + "/api/family/deleteRoom";//删除房间
    public static String CreateRoom = FeasyHomeServer + "/api/family/createRoom";//创建房间
    public static String GroupAddress = FeasyHomeServer + "/api/family/groupAddress";// 获取可用组类型
    public static String GroupList = FeasyHomeServer + "/api/device/groupList";//群组列表
    public static String curtainGroupList = FeasyHomeServer + "/api/curtain/groupList";//群组列表
    public static String getLocationList = FeasyHomeServer + "/api/family/getLocationList";//组位置列表
    public static String getLightList = FeasyHomeServer + "/api/family/getLightList";//灯光名称列表
    public static String createGroup = FeasyHomeServer + "/api/device/createGroup";//创建分组
    public static String deleteGroup = FeasyHomeServer + "/api/device/deleteGroup";//删除组
    public static String deletecurtainGroup = FeasyHomeServer + "/api/curtain/deleteGroup";//删除组
    public static String createDevice = FeasyHomeServer + "/api/device/createDevice";// 添加设备入组
    public static String getDevlist = FeasyHomeServer + "/api/device/list";//获取群组内设备列表
    public static String delDevfromGroup = FeasyHomeServer + "/api/device/delete";//设备退网退组
    public static String curtainTypeList = FeasyHomeServer + "/api/curtain/curtainTypeList";//获取窗帘群组
    public static String curtainCreategroup = FeasyHomeServer + "/api/curtain/createGroup";//创建窗帘群组
    public static String enableGroupAddress = FeasyHomeServer + "/api/family/enableGroupAddress";//获取可用组地址
    public static String createCurtain = FeasyHomeServer + "/api/curtain/createCurtain";//添加窗帘入组
    public static String createPanel = FeasyHomeServer + "/api/panel/createPanel";//添加面板
    public static String getPanelinfo = FeasyHomeServer + "/api/panel/panelDetail";
    public static String deletePanel = FeasyHomeServer + "/api/panel/deletePanel";

    public static String panelGroupList = FeasyHomeServer + "/api/panel/groupList";
    public static String updateLayout = FeasyHomeServer + "/api/panel/updateLayout";
    public static String getCaptcha = FeasyHomeServer + "/api/user/getCaptcha";
    public static String sceneList = FeasyHomeServer + "/api/scene/index";
    public static String createVoice = FeasyHomeServer + "/api/voice/createVoice";
    public static String voiceDetail = FeasyHomeServer + "/api/voice/voiceDetail";
    public static String deleteVoice = FeasyHomeServer + "/api/voice/deleteVoice";
    public static String logout = FeasyHomeServer + "/api/user/logout";
    public static String sceneConfigList = FeasyHomeServer + "/api/scene/sceneConfigList";
    public static String scenegrouplist = FeasyHomeServer + "/api/scene/groupList";
    public static String sceneDetail = FeasyHomeServer + "/api/scene/sceneDetail";
    public static String nowScene = FeasyHomeServer + "/api/scene/nowScene";
    public static String addScene = FeasyHomeServer + "/api/scene/addScene";
    public static String deleteScene = FeasyHomeServer + "/api/scene/deleteScene";
    public static String joinSceneGroup = FeasyHomeServer + "/api/scene/joinSceneGroup";
    public static String deleteSceneGroup = FeasyHomeServer + "/api/scene/deleteSceneGroup";
    public static String modifyUserInfo = FeasyHomeServer + "/api/user/modifyUserInfo";
    public static String deleteUser = FeasyHomeServer + "/api/user/deleteUser";
    public static String upload = FeasyHomeServer + "/api/common/upload";
    public static String setPanel = FeasyHomeServer + "/api/panel/setPanel";
    public static String updateFamily = FeasyHomeServer + "/api/family/updateFamily";
    public static String shareQrcode = FeasyHomeServer + "/api/family/appShareQrcode";

    public static String joinFamily = FeasyHomeServer + "/api/family/joinFamily";
    public static String inviteUserPage = FeasyHomeServer + "/api/family/inviteUserPage";

    public static String feedback = FeasyHomeServer + "/api/feedback/add";


}
