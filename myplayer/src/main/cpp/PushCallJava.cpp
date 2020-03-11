//
// Created by hxr on 2020/1/05
//

#include "PushCallJava.h"

PushCallJava::PushCallJava(JavaVM *javaVM, JNIEnv *jniEnv, jobject *jobj) {

    this->javaVM = javaVM;
    this->jniEnv = jniEnv;
    this->jobj = jniEnv->NewGlobalRef(*jobj);

    jclass jlz = jniEnv->GetObjectClass(this->jobj);

    jmid_connecting =jniEnv->GetMethodID(jlz, "onConnecting", "()V");
    jmid_connectsuccess = jniEnv->GetMethodID(jlz, "onConnectSuccess", "()V");
    jmid_connectfail = jniEnv->GetMethodID(jlz, "onConnectFail", "(Ljava/lang/String;)V");
}

PushCallJava::~PushCallJava() {
    jniEnv->DeleteGlobalRef(jobj);
    javaVM = NULL;
    jniEnv = NULL;
}

void PushCallJava::onConnectint(int type) {

    if(type == CHILD_THRBAD)
    {
        JNIEnv *jniEnv;
        if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK)
        {
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_connecting);
        javaVM->DetachCurrentThread();
    }
    else
    {
        jniEnv->CallVoidMethod(jobj, jmid_connecting);
    }
}

void PushCallJava::onConnectsuccess() {
    JNIEnv *jniEnv;
    if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK)
    {
        return;
    }
    jniEnv->CallVoidMethod(jobj, jmid_connectsuccess);
    javaVM->DetachCurrentThread();
}

void PushCallJava::onConnectFail(char *msg) {

    JNIEnv *jniEnv;
    if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK)
    {
        return;
    }

    jstring jmsg = jniEnv->NewStringUTF(msg);

    jniEnv->CallVoidMethod(jobj, jmid_connectfail, jmsg);

    jniEnv->DeleteLocalRef(jmsg);
    javaVM->DetachCurrentThread();
}
