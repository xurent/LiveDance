//
// Created by hxuro on 2020/1/23.
//

#ifndef WLLIVEPUSHER_RTMPPUSH_H
#define WLLIVEPUSHER_RTMPPUSH_H

#include <malloc.h>
#include <string.h>
#include "PushQueue.h"
#include "pthread.h"
#include "PushCallJava.h"

extern "C"{
#include "librtmp/rtmp.h"

};

class RtmpPush {

public:

    RTMP *rtmp=NULL;
    char *url=NULL;
    PushQueue *queue=NULL;
    pthread_t  push_thread;
    PushCallJava *wlCallJava=NULL;
    bool  startPushing = false;
    long startTime=0;
public:
    RtmpPush(const char *url,PushCallJava *wlCallJava);
    ~RtmpPush();
    void init();
    void pushSPSPPS(char *sps,int sps_len,char *pps,int pps_len);
    void pushData(char *data,int data_len, bool keyframe);
    void pushAudioData(char *data, int data_len);
    void pushStop();

};


#endif //WLLIVEPUSHER_RTMPPUSH_H
