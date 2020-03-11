//
// Created by hxuro on 2019/11/27.
//

#ifndef WY_MUSIC_VIDEO_H
#define WY_MUSIC_VIDEO_H

#include "WlQueue.h"
#include "WlCallJava.h"
#include "cwchar"
#include "WlAudio.h"

#define CODEC_YUV 0
#define CODEC_MEDIACODEC 1
extern "C" {
#include "include/libavcodec/avcodec.h"
#include <libavutil/time.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
};

class Video {

public:
    int streamIndex = -1;
    AVCodecContext *avCodecContext = NULL;
    AVCodecParameters *codecParameters = NULL;
    WlQueue *queue = NULL;
    WlPlaystatus *playstatus = NULL;
    WlCallJava *callJava = NULL;
    AVRational time_base;
    pthread_t thread_play;
    WlAudio *audio = NULL;
    double clock = 0;
    double delayTime = 0;
    double defaultDelayTime = 0.04;

    pthread_mutex_t codecMutex;

    int codectype = CODEC_YUV;

    AVBSFContext *abs_ctx = NULL;
public:
    Video(WlPlaystatus *playstatus, WlCallJava *callJava);

    ~Video();

    void play();

    void release();

    double getFrameDiffTime(AVFrame *avFrame, AVPacket *avPacket);

    double getDelayTime(double diff);
};


#endif //WY_MUSIC_VIDEO_H
