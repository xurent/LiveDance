//
// Created by hxuro on 2019/11/21.
//

#include "WlQueue.h"

WlQueue::WlQueue(WlPlaystatus *playstatus) {
    this->playstatus = playstatus;
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);


}

WlQueue::~WlQueue() {
    clearAvpacket();
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);
}

int WlQueue::putAvPacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);

    queuePacket.push(packet);
    if (LOG_DEBUG) {
       // LOGD("放入一个AvPacket到队列，个数为: %d", queuePacket.size());
    }

    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

    return 0;
}

int WlQueue::getAvPacket(AVPacket *packet) {
    pthread_mutex_lock(&mutexPacket);

    while (playstatus != NULL && !playstatus->exit) {

        if (queuePacket.size() > 0) {
            AVPacket *avPacket = queuePacket.front();
            if (av_packet_ref(packet, avPacket) == 0) {
                queuePacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            if (LOG_DEBUG) {
               // LOGD("从队列取出一个AvPacket，还剩下: %d", queuePacket.size());
            }
            break;
        } else {

            pthread_cond_wait(&condPacket, &mutexPacket);
        }
    }

    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int WlQueue::getQueueSize() {

    int size = 0;
    pthread_mutex_lock(&mutexPacket);
    size = queuePacket.size();
    pthread_mutex_unlock(&mutexPacket);
    return size;
}

void WlQueue::clearAvpacket() {

    pthread_cond_signal(&condPacket);
    pthread_mutex_lock(&mutexPacket);

    while (!queuePacket.empty()) {
        AVPacket *packet = queuePacket.front();
        queuePacket.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = NULL;

    }

    pthread_mutex_unlock(&mutexPacket);

}

void WlQueue::noticeQueue() {

    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
}

