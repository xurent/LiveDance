//
// Created by hxuro on 2020/1/23.
//

#ifndef WLLIVEPUSHER_WLQUEUE_H
#define WLLIVEPUSHER_WLQUEUE_H

#include "Android.h"
#include "queue"
#include "pthread.h"


extern "C"{
#include "librtmp/rtmp.h"
};
class PushQueue {

public:
    std::queue<RTMPPacket*> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;

public:
    PushQueue();
    ~PushQueue();
    int putRtmpPacket(RTMPPacket* packet);
    RTMPPacket* getRtmpPacket();
    void clearQueue();
    void  notifyQueue();

};


#endif //WLLIVEPUSHER_WLQUEUE_H
