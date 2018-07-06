package com.jhqc.vr.travel.music;

/**
 * Created by Solomon on 2017/10/21 0021.
 */

public class VideoPlayerStatus {

    //错误状态
    public static final int STATE_ERROR = -1;

    //初始状态
    public static final int STATE_IDLE = 0;

    //准备中状态
    public static final int STATE_PREPARING = 1;

    //准备完成状态
    public static final int STATE_PREPARED = 2;

    //正在播放状态
    public static final int STATE_PLAYING = 3;

    //暂停状态
    public static final int STATE_PAUSED = 4;

    //播放完成状态
    public static final int STATE_COMPLETED = 5;

    //网络错误提示
    public static final int TYPE_SHOW_NETWORK_ERROR = 0;

    //视频加载失败提示
    public static final int TYPE_SHOW_VIDEO_LOAD_FAIED = 1;

    //视频播放错误提示
    public static final int TYPE_SHOW_VIDEO_PLAY_ERROR = 2;

    //试看完成提示
    public static final int TYPE_SHOW_VIDEO_TRIAL_COMPLETED = 3;

    //播放下一节提示
    public static final int TYPE_SHOW_VIDEO_PLAY_NEXT = 4;

    //播放完成提示
    public static final int TYPE_SHOW_VIDEO_PLAY_COMPLETED = 5;

    public static final int POSITION_CENTER_LEFT = 6;

    public static final int POSITION_CENTER_RIGHT = 7;

    public static final int POSITION_BOTTOM = 8;

    //未知错误
    public static final int ERROR_CODE_UNKNOWN = 1;

    //文件错误
    public static final int ERROR_CODE_FILE_ERROR = 2;

    //缓冲错误
    public static final int ERROR_CODE_FILE_CACHE_ERROR = 3;
}
