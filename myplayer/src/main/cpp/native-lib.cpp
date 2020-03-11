#include <jni.h>
#include <string>
#include "WlCallJava.h"
#include "WlFFmpeg.h"
#include "RtmpPush.h"
#include "PushCallJava.h"

extern "C" {
#include <libavformat/avformat.h>
}

JavaVM *javaVm = NULL;
WlCallJava *callJava = NULL;
WlFFmpeg *wlFFmpeg = NULL;
WlPlaystatus *playstatus = NULL;
bool nexit = true;
pthread_t thread_start;



extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    jint result = -1;
    javaVm = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return result;
    }

    return JNI_VERSION_1_6;

}


extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_player_WLPlayer_n_1parpared(JNIEnv *env, jobject thiz, jstring source_) {

    const char *source = env->GetStringUTFChars(source_, 0);
    if (wlFFmpeg == NULL) {
        if (callJava == NULL) {
            callJava = new WlCallJava(javaVm, env, &thiz);
        }
        callJava->onCallLoad(MAIN_THRBAD, true);
        playstatus = new WlPlaystatus();
        wlFFmpeg = new WlFFmpeg(playstatus, callJava, source);
        wlFFmpeg->parpared();
        LOGD("调用C++解码音视频")
    } else {
        wlFFmpeg->parpared();
    }


}

void *startCallBack(void *data) {

    WlFFmpeg *fFmpeg = (WlFFmpeg *) (data);
    fFmpeg->start();
    return 0;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_player_WLPlayer_n_1start(JNIEnv *env, jobject thiz) {

    if (wlFFmpeg != NULL) {
        // wlFFmpeg->start();
        pthread_create(&thread_start, NULL, startCallBack, wlFFmpeg);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_player_WLPlayer_n_1pause(JNIEnv *env, jobject thiz) {

    if (wlFFmpeg != NULL) {
        wlFFmpeg->pause();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_player_WLPlayer_n_1resume(JNIEnv *env, jobject thiz) {
    if (wlFFmpeg != NULL) {
        wlFFmpeg->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_player_WLPlayer_n_1stop(JNIEnv *env, jobject thiz) {

    if (!nexit) {
        return;
    }

    jclass jlz = env->GetObjectClass(thiz);
    jmethodID jmid_next = env->GetMethodID(jlz, "onCallNext", "()V");


    nexit = false;
    if (wlFFmpeg != NULL) {
        wlFFmpeg->release();

        pthread_join(thread_start, NULL);

        delete (wlFFmpeg);
        wlFFmpeg = NULL;
        if (callJava != NULL) {
            delete (callJava);
            callJava = NULL;
        }
        if (playstatus != NULL) {
            delete (playstatus);
            playstatus = NULL;
        }
    }
    nexit = true;
    env->CallVoidMethod(thiz, jmid_next);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_player_WLPlayer_n_1seek(JNIEnv *env, jobject thiz, jint sedc) {

    if (wlFFmpeg != NULL) {
        wlFFmpeg->seek(sedc);
    }

}
//推流
PushCallJava *Java = NULL;
RtmpPush *rtmpPush=NULL;
bool exits = true;
extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_push_PushVideo_initPush(JNIEnv *env, jobject thiz, jstring pushUrl_) {
    const char *pushUrl = env->GetStringUTFChars(pushUrl_, 0);

    // TODO
    if(Java == NULL)
    {
        exits = false;
        Java = new PushCallJava(javaVm, env, &thiz);
        rtmpPush = new RtmpPush(pushUrl, Java);
        rtmpPush->init();
    }
    env->ReleaseStringUTFChars(pushUrl_, pushUrl);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_push_PushVideo_pushSPSPPS(JNIEnv *env, jobject thiz, jbyteArray sps_,
                                                     jint sps_len, jbyteArray pps_, jint pps_len) {
    jbyte *sps = env->GetByteArrayElements(sps_, NULL);
    jbyte *pps = env->GetByteArrayElements(pps_, NULL);
    if(rtmpPush!=NULL){
        rtmpPush->pushSPSPPS(reinterpret_cast<char *>(sps), sps_len, reinterpret_cast<char *>(pps), pps_len);
    }

    env->ReleaseByteArrayElements(sps_, sps, 0);
    env->ReleaseByteArrayElements(pps_, pps, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_push_PushVideo_pushVideoData(JNIEnv *env, jobject thiz, jbyteArray data_,
                                                        jint data_len, jboolean keyframe) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    if(rtmpPush!=NULL){
        rtmpPush->pushData(reinterpret_cast<char *>(data), data_len,keyframe);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_push_PushVideo_pushAudioData(JNIEnv *env, jobject thiz, jbyteArray data_,
                                                        jint data_len) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    // TODO
    if(rtmpPush != NULL && !exits)
    {
        rtmpPush->pushAudioData(reinterpret_cast<char *>(data), data_len);
        LOGD("收到声音传输数据")
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xurent_myplayer_push_PushVideo_pushStop(JNIEnv *env, jobject thiz) {
    // TODO
    if(rtmpPush != NULL)
    {
        exits = true;
        rtmpPush->pushStop();
        delete(rtmpPush);
        delete(Java);
        rtmpPush = NULL;
        Java = NULL;
    }
}