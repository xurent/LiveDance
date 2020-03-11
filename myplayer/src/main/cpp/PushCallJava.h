//
// Created by hxr on 2020/1/5
//

#include <cwchar>
#include "jni.h"

#ifndef WLLIVEPUSHER_WLCALLJAVA_H
#define WLLIVEPUSHER_WLCALLJAVA_H

#define  MAIN_THRBAD 0      //主线程调用
#define  CHILD_THRBAD 1      // 子线程调用

class PushCallJava {

public:

    JNIEnv *jniEnv = NULL;
    JavaVM *javaVM = NULL;
    jobject jobj;

    jmethodID jmid_connecting;
    jmethodID jmid_connectsuccess;
    jmethodID jmid_connectfail;


public:
    PushCallJava(JavaVM *javaVM, JNIEnv *jniEnv, jobject *jobj);
    ~PushCallJava();

    void onConnectint(int type);

    void onConnectsuccess();

    void onConnectFail(char *msg);









};


#endif //WLLIVEPUSHER_WLCALLJAVA_H
