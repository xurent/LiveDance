//
// Created by hxuro on 2019/11/20.
//

#ifndef WY_MUSIC_WLFFMPEG_H
#define WY_MUSIC_WLFFMPEG_H

#include "WlCallJava.h"
#include "pthread.h"
#include "WlAudio.h"
#include "Video.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/time.h>
};

class WlFFmpeg {

public:
    WlCallJava *callJava = NULL;
    const char *url = NULL;

    pthread_t decodeThread;
    AVFormatContext *pFormatCtx = NULL;
    WlAudio *audio = NULL;
    Video *video = NULL;
    WlPlaystatus *playstatus = NULL;
    pthread_mutex_t init_mutex;
    bool exit = false;
    int duration;
    pthread_mutex_t seek_mutex;

    bool supportMediacodec = false;

    const AVBitStreamFilter *bsFilter = NULL;
public:
    WlFFmpeg(WlPlaystatus *playstatus, WlCallJava *callJava1, const char *url);

    ~WlFFmpeg();

    void parpared();

    void decodeFFmpegThread();

    void start();

    void pause();

    void resume();

    void release();

    void seek(int64_t secds);

    int getCodecContext(AVCodecParameters *codecpar, AVCodecContext **avCodecContext);
};


#endif //WY_MUSIC_WLFFMPEG_H
