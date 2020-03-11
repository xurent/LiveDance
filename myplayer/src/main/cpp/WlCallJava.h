//
// Created by hxuro on 2019/11/20.
//

#ifndef WY_MUSIC_WLCALLJAVA_H
#define WY_MUSIC_WLCALLJAVA_H

#include <cwchar>
#include "jni.h"
#include "Android.h"

#define  MAIN_THRBAD 0      //主线程调用
#define  CHILD_THRBAD 1      // 子线程调用

class WlCallJava {

public:

    JavaVM *javaVm = NULL;
    JNIEnv *jniEnv = NULL;
    jobject jobj;

    jmethodID jmid_parpared;
    jmethodID jmid__load;
    jmethodID jmid_timeinfo;
    jmethodID jmid_error;
    jmethodID jmid_complete;
    jmethodID jmid_renderyuv;
    jmethodID jmid_supportvideo;
    jmethodID jmid_initmediacodec;
    jmethodID jmid_decodeavpacket;


public:
    WlCallJava(JavaVM *javaVm, JNIEnv *env, jobject *obj);

    ~WlCallJava();

    void onCallParpared(int Type);

    void onCallLoad(int type, bool load);

    void onCallTimeInfo(int type, int curr, int total);

    void onCallError(int type, int code, char *msg);

    void onCallComplete(int type);

    void onCallRenderYUV(int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv);

    bool onCallIsSupportVideo(const char *ffcodecname);

    void onCallInitMediacodec(const char *mime, int width, int height, int csd0_size, int csd1_size,
                              uint8_t *csd_0, uint8_t *csd_1);

    void onCallDecodeAVPacket(int datasize, uint8_t *packetdata);




};


#endif //WY_MUSIC_WLCALLJAVA_H
