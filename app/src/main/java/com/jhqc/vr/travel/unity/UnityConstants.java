package com.jhqc.vr.travel.unity;

/**
 * Created by Solomon on 2017/10/29 0029.
 */

public class UnityConstants {

    /** 请求获取GPS状态, Unity调用Native函数名 */
    public static final String CLASS_ENTRANCE = "Entrance";


    /** 请求获取GPS状态, Unity调用Native函数名 */
    public static final String N_REQGPSSTATE = "ReqGPSState";
    /** 请求获取GPS, Unity调用Native函数名 */
    public static final String N_REQGPSINFO = "ReqGPSInfo";
    /** 请求拨打电话, Unity调用Native函数名 */
    public static final String N_REQCALL = "ReqCallPhone";
    /** 打印日志, Unity调用Native函数名 */
    public static final String N_REQLOG = "ReqCallLog";
    /** 是否播放, Unity调用Native函数名 */
    public static final String N_REQPLAYMUSIC = "ReqPlayMusic";
    /** 显示控制Bar, Unity调用Native函数名 */
    public static final String N_REQPLAYBUTTON = "ReqPlayButton";
    /** unity准备完成, Unity调用Native函数名 */
    public static final String N_REQREADY = "ReqTellReady";
    /** unity指令原生播放视频, Unity调用Native函数名 */
    public static final String N_REQPLAYVIDEO = "ReqPlayVideo";
    /** unity指令原生打开AR扫描, Unity调用Native函数名 */
    public static final String N_REQARSCAN = "ReqARScan";
    /** unity指令关闭视频播放控件, Unity调用Native函数名 */
    public static final String N_SHUTDOWNVIDEO = "ReqShutdownVideo";

    /** GPS状态回调， Native回调Unity函数名 */
    public static final String U_GPSSTATE = "OnGPSStateResult";
    /** GPS信息返回回调， Native回调Unity函数名 */
    public static final String U_GPSINFO = "OnGPSInfoResult";
    /** 智能导游状态变化回调， Native回调Unity函数名 */
    public static final String U_INTELLIGENTSSTATE = "OnIntelligentState";
    /** 视频播放结束回调， Native回调Unity函数名 */
    public static final String U_ONPLAYVIDEOEND = "OnPlayVideoState";


    /** GPS状态， 未设置（无） */
    public static final String GPSSTATE_NOT_DETEMINED = "kCLAuthorizationStatusNotDetermined";
    /** GPS状态， 不可用 */
    public static final String GPSSTATE_NONE = "kCLAuthorizationStatusDenied";
    /** GPS状态， 可用 */
    public static final String GPSSTATE_YES = "kCLAuthorizationStatusAuthorizedAlways";
}
