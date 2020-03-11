//
// Created by hxuro on 2019/11/21.
//

#ifndef WY_MUSIC_WLQUEUE_H
#define WY_MUSIC_WLQUEUE_H
extern "C" {
#include <libavcodec/avcodec.h>
};

#include "WlPlaystatus.h"
#include "Android.h"
#include "queue"
#include "pthread.h"

class WlQueue {

public:
    std::queue<AVPacket *> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;
    WlPlaystatus *playstatus = NULL;
public:

    WlQueue(WlPlaystatus *playstatus);

    ~WlQueue();

    int putAvPacket(AVPacket *packet);

    int getAvPacket(AVPacket *packet);

    int getQueueSize();

    void clearAvpacket();

    void noticeQueue();
};


#endif //WY_MUSIC_WLQUEUE_H
