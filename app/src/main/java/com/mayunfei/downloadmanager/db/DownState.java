package com.mayunfei.downloadmanager.db;

public interface DownState {
    int STATUS_UNSTART = -1; //还未开始
    int STATUS_WAITING = 0; //等待 数据库中不会出现这个状态
    int STATUS_PAUSE = 1;  //暂停
    int STATUS_DOWNLOADING = 2; //下载中
    int STATUS_ERROR = 3; //错误
    int STATUS_FINISH = 4; //完成
    int STATUS_DELETE = 5; //删除

}
