//
// Created by hxuro on 2019/11/20.
//



#include "WlFFmpeg.h"


WlFFmpeg::WlFFmpeg(WlPlaystatus *playstatus, WlCallJava *callJava1, const char *url) {

    this->callJava = callJava1;
    this->url = url;
    this->playstatus = playstatus;
    pthread_mutex_init(&init_mutex, NULL);
    pthread_mutex_init(&seek_mutex, NULL);

}

void *decodeFFmpeg(void *data) {

    WlFFmpeg *wlFFmpeg = (WlFFmpeg *) (data);
    wlFFmpeg->decodeFFmpegThread();
    return 0;

}

void WlFFmpeg::parpared() {

    pthread_create(&decodeThread, NULL, decodeFFmpeg, this);

}

int avformat_callback(void *ctx) {

    WlFFmpeg *fFmpeg = (WlFFmpeg *) (ctx);
    if (fFmpeg->playstatus->exit) {
        return AVERROR_EOF;
    }
    return 0;
}

void WlFFmpeg::decodeFFmpegThread() {

    LOGD("解码开始了------------");
    pthread_mutex_lock(&init_mutex);
    av_register_all();  //注册解码器
    avformat_network_init();
    pFormatCtx = avformat_alloc_context();

    pFormatCtx->interrupt_callback.callback = avformat_callback;
    pFormatCtx->interrupt_callback.opaque = this;

    if (avformat_open_input(&pFormatCtx, url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGD("can not open url :%s", url);
            callJava->onCallError(CHILD_THRBAD, 1001, "can not open url ");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGD("can not find stream from url :%s", url);
            callJava->onCallError(CHILD_THRBAD, 1002, "can not find stream from url ");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {

            if (audio == NULL) {
                audio = new WlAudio(playstatus, pFormatCtx->streams[i]->codecpar->sample_rate,
                                    callJava);
                audio->streamIndex = i;
                audio->codecpar = pFormatCtx->streams[i]->codecpar;
                audio->duration = pFormatCtx->duration / AV_TIME_BASE;
                audio->time_base = pFormatCtx->streams[i]->time_base;
                duration = audio->duration;
            }
        } else if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {

            if (video == NULL) {
                video = new Video(playstatus, callJava);
                video->streamIndex = i;
                video->codecParameters = pFormatCtx->streams[i]->codecpar;
                video->time_base = pFormatCtx->streams[i]->time_base;

                int num = pFormatCtx->streams[i]->avg_frame_rate.num;
                int den = pFormatCtx->streams[i]->avg_frame_rate.den;
                if (num != 0 && den != 0) {
                    int fps = num / den;//[25 / 1]
                    video->defaultDelayTime = 1.0 / fps;
                }

            }
        }
    }


    //成功了回调Java 层
    if (audio != NULL) {
        getCodecContext(audio->codecpar, &audio->avCodecContext);
    }
    if (video != NULL) {
        getCodecContext(video->codecParameters, &video->avCodecContext);
    }
    if (callJava != NULL) {
        if (playstatus != NULL && !playstatus->exit) {
            callJava->onCallParpared(CHILD_THRBAD);
        } else {
            exit = true;
        }
    }
    pthread_mutex_unlock(&init_mutex);
}

void WlFFmpeg::start() {
    if (audio == NULL) {
        if (LOG_DEBUG) {
            LOGD("audio is null");
            callJava->onCallError(CHILD_THRBAD, 1007, "audio is null");
        }
        return;

    }
    if (video == NULL) {
        return;
    }
    supportMediacodec = false;
    video->audio = audio;

    const char *codecName = ((const AVCodec *) video->avCodecContext->codec)->name;
    if (supportMediacodec = callJava->onCallIsSupportVideo(codecName)) {
        LOGE("当前设备支持硬解码当前视频");
        if (strcasecmp(codecName, "h264") == 0) {
            bsFilter = av_bsf_get_by_name("h264_mp4toannexb");
        } else if (strcasecmp(codecName, "h265") == 0) {
            bsFilter = av_bsf_get_by_name("hevc_mp4toannexb");
        }
        if (bsFilter == NULL) {
            goto end;
        }
        if (av_bsf_alloc(bsFilter, &video->abs_ctx) != 0) {
            supportMediacodec = false;
            goto end;
        }
        if (avcodec_parameters_copy(video->abs_ctx->par_in, video->codecParameters) < 0) {
            supportMediacodec = false;
            av_bsf_free(&video->abs_ctx);
            video->abs_ctx = NULL;
            goto end;
        }
        if (av_bsf_init(video->abs_ctx) != 0) {
            supportMediacodec = false;
            av_bsf_free(&video->abs_ctx);
            video->abs_ctx = NULL;
            goto end;
        }
        video->abs_ctx->time_base_in = video->time_base;
    }

    end:
    //LOGE("当前设备视频"+supportMediacodec);
    //supportMediacodec= false;
    if (supportMediacodec) {
        video->codectype = CODEC_MEDIACODEC;
        video->callJava->onCallInitMediacodec(
                codecName,
                video->avCodecContext->width,
                video->avCodecContext->height,
                video->avCodecContext->extradata_size,
                video->avCodecContext->extradata_size,
                video->avCodecContext->extradata,
                video->avCodecContext->extradata
        );
    }


    audio->play();
    video->play();
    //读取流
    while (playstatus != NULL && !playstatus->exit) {

        if (playstatus->seek) {
            av_usleep(1000 * 100);
            continue;
        }
        if (audio->queue->getQueueSize() > 40) {
            av_usleep(1000 * 100);
            continue;
        }

        AVPacket *avPacket = av_packet_alloc();

        pthread_mutex_lock(&seek_mutex);
        int ret = av_read_frame(pFormatCtx, avPacket);
        pthread_mutex_unlock(&seek_mutex);

        if (ret == 0) {

            if (avPacket->stream_index == audio->streamIndex) {

                audio->queue->putAvPacket(avPacket);
            } else if (avPacket->stream_index == video->streamIndex) {
                video->queue->putAvPacket(avPacket);
                //LOGD("获取到视频流")

            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
            }
        } else {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            while (playstatus != NULL && !playstatus->exit) {
                if (audio->queue->getQueueSize() > 0) {
                    av_usleep(1000 * 100);
                    continue;
                } else {
                    if (!playstatus->seek) {
                        av_usleep(1000 * 500);
                        playstatus->exit = true;
                    }

                    break;
                }
            }

        }
    }


    if (callJava != NULL) {
        callJava->onCallComplete(CHILD_THRBAD);
    }
    exit = true;

}

void WlFFmpeg::pause() {
    if (playstatus != NULL) {
        playstatus->pause = true;
    }

    if (audio != NULL) {
        audio->pause();
    }

}

void WlFFmpeg::resume() {
    if (playstatus != NULL) {
        playstatus->pause = false;
    }
    if (audio != NULL) {
        audio->resume();
    }

}

void WlFFmpeg::release() {

    playstatus->exit = true;

    pthread_join(decodeThread, NULL);

    pthread_mutex_lock(&init_mutex);

    int sleepCount = 0;
    while (!exit) {

        if (sleepCount > 1000) {
            exit = true;
        }
        if (LOG_DEBUG) {
            LOGE("wait ffmpeg exit: %d ", sleepCount);
        }
        sleepCount++;
        av_usleep(1000 * 10);  //10毫秒
    }
    if (audio != NULL) {
        audio->release();
        delete (audio);
        audio = NULL;
    }
    if (video != NULL) {
        video->release();
        delete (video);
        video = NULL;
    }

    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }
    if (playstatus != NULL) {
        playstatus = NULL;
    }
    if (callJava != NULL) {
        callJava = NULL;
    }

    pthread_mutex_unlock(&init_mutex);
}

WlFFmpeg::~WlFFmpeg() {

    pthread_mutex_destroy(&seek_mutex);
    pthread_mutex_destroy(&init_mutex);
}

void WlFFmpeg::seek(int64_t secds) {
    LOGE("seek time %d", secds);
    if (duration <= 0) {
        return;
    }
    if (secds >= 0 && secds <= duration) {
        playstatus->seek = true;
        pthread_mutex_lock(&seek_mutex);
        int64_t rel = secds * AV_TIME_BASE;
        avformat_seek_file(pFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
        if (audio != NULL) {
            playstatus->seek = true;
            audio->queue->clearAvpacket();
            audio->clock = 0;
            audio->last_time = 0;

            pthread_mutex_lock(&audio->codecMutex);
            avcodec_flush_buffers(audio->avCodecContext);
            pthread_mutex_unlock(&audio->codecMutex);
        }

        if (video != NULL) {
            video->queue->clearAvpacket();
            video->clock = 0;
            pthread_mutex_lock(&video->codecMutex);
            avcodec_flush_buffers(video->avCodecContext);
            pthread_mutex_unlock(&video->codecMutex);
        }
        pthread_mutex_unlock(&seek_mutex);
        playstatus->seek = false;
    }


}

int WlFFmpeg::getCodecContext(AVCodecParameters *codecpar, AVCodecContext **avCodecContext) {


    AVCodec *dec = avcodec_find_decoder(codecpar->codec_id); //获取到解码器
    if (!dec) {

        if (LOG_DEBUG) {
            LOGD("can not find decoder ");
            callJava->onCallError(CHILD_THRBAD, 1003, "can not find decoder ");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return -1;
    }

    *avCodecContext = avcodec_alloc_context3(dec);
    if (!audio->avCodecContext) {
        if (LOG_DEBUG) {
            LOGD("can not alloc new  decoderContext ");
            callJava->onCallError(CHILD_THRBAD, 1004, "can not alloc new  decoderContext ");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return -1;
    }

    if (avcodec_parameters_to_context(*avCodecContext, codecpar) < 0) {

        if (LOG_DEBUG) {
            LOGD("can not fill decoderContext");
            callJava->onCallError(CHILD_THRBAD, 1005, "can not fill decoderContext");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return -1;
    }

    //打开解码器
    if (avcodec_open2(*avCodecContext, dec, 0) != 0) {
        //流不能打开
        if (LOG_DEBUG) {
            LOGD("can not open streams ");
            callJava->onCallError(CHILD_THRBAD, 1006, "can not open streams");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return -1;
    }

    return 0;
}
