//
// Created by hxuro on 2019/11/20.
//

#include "WlCallJava.h"


WlCallJava::WlCallJava(JavaVM *javaVm, JNIEnv *env, jobject *obj) {

    this->javaVm = javaVm;
    this->jniEnv = env;
    this->jobj = env->NewGlobalRef(*obj);

    jclass jls = jniEnv->GetObjectClass(jobj);

    if (!jls) {
        if (LOG_DEBUG) {
            LOGE("get jclass error");
            return;
        }

    }

    jmid_parpared = env->GetMethodID(jls, "onCallParpared", "()V");
    jmid__load = env->GetMethodID(jls, "onCallLoad", "(Z)V");
    jmid_timeinfo = env->GetMethodID(jls, "onCallTimeInfo", "(II)V");
    jmid_error = env->GetMethodID(jls, "onCallError", "(ILjava/lang/String;)V");
    jmid_complete = env->GetMethodID(jls, "onCallComplete", "()V");
    jmid_renderyuv = env->GetMethodID(jls, "onCallRenderYUV", "(II[B[B[B)V");
    jmid_supportvideo = env->GetMethodID(jls, "onCallIsSupportMediaCodec", "(Ljava/lang/String;)Z");
    jmid_initmediacodec = env->GetMethodID(jls, "initMediaCodec", "(Ljava/lang/String;II[B[B)V");
    jmid_decodeavpacket = env->GetMethodID(jls, "decodeAVPacket", "(I[B)V");

}

WlCallJava::~WlCallJava() {

}

void WlCallJava::onCallParpared(int Type) {

    if (Type == MAIN_THRBAD) {
        jniEnv->CallVoidMethod(jobj, jmid_parpared);
    } else if (Type == CHILD_THRBAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGD("get child thread jnienv error");
                return;
            }
        }
        jniEnv->CallVoidMethod(jobj, jmid_parpared);
        javaVm->DetachCurrentThread();
    }


}

void WlCallJava::onCallLoad(int type, bool load) {

    if (type == MAIN_THRBAD) {
        jniEnv->CallVoidMethod(jobj, jmid__load, load);
    } else if (type == CHILD_THRBAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGD("get child thread jnienv error");
                return;
            }
        }
        jniEnv->CallVoidMethod(jobj, jmid__load, load);
        javaVm->DetachCurrentThread();
    }

}

void WlCallJava::onCallTimeInfo(int type, int curr, int total) {
    if (type == MAIN_THRBAD) {
        jniEnv->CallVoidMethod(jobj, jmid_timeinfo, curr, total);
    } else if (type == CHILD_THRBAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGD("get child thread jnienv error");
                return;
            }
        }
        jniEnv->CallVoidMethod(jobj, jmid_timeinfo, curr, total);
        javaVm->DetachCurrentThread();
    }
}

void WlCallJava::onCallError(int type, int code, char *msg) {
    if (type == MAIN_THRBAD) {
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
    } else if (type == CHILD_THRBAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGD("get child thread jnienv error");
                return;
            }
        }
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
        javaVm->DetachCurrentThread();
    }

}

void WlCallJava::onCallComplete(int type) {
    if (type == MAIN_THRBAD) {
        jniEnv->CallVoidMethod(jobj, jmid_complete);
    } else if (type == CHILD_THRBAD) {
        JNIEnv *jniEnv;
        if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGD("get child thread jnienv error");
                return;
            }
        }
        jniEnv->CallVoidMethod(jobj, jmid_complete);

        javaVm->DetachCurrentThread();
    }
}

void WlCallJava::onCallRenderYUV(int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv) {


    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGD("get child thread jnienv error");
            return;
        }
    }
    jbyteArray y = jniEnv->NewByteArray(width * height);
    jniEnv->SetByteArrayRegion(y, 0, width * height, reinterpret_cast<const jbyte *>(fy));

    jbyteArray u = jniEnv->NewByteArray(width * height / 4);
    jniEnv->SetByteArrayRegion(u, 0, width * height / 4, reinterpret_cast<const jbyte *>(fu));

    jbyteArray v = jniEnv->NewByteArray(width * height / 4);
    jniEnv->SetByteArrayRegion(v, 0, width * height / 4, reinterpret_cast<const jbyte *>(fv));

    jniEnv->CallVoidMethod(jobj, jmid_renderyuv, width, height, y, u, v);

    jniEnv->DeleteLocalRef(y);
    jniEnv->DeleteLocalRef(u);
    jniEnv->DeleteLocalRef(v);
    javaVm->DetachCurrentThread();
}

bool WlCallJava::onCallIsSupportVideo(const char *ffcodecname) {
    bool support = false;
    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
        return support;
    }

    jstring type = jniEnv->NewStringUTF(ffcodecname);
    support = jniEnv->CallBooleanMethod(jobj, jmid_supportvideo, type);
    jniEnv->DeleteLocalRef(type);
    javaVm->DetachCurrentThread();
    return support;
}

void WlCallJava::onCallInitMediacodec(const char *mime, int width, int height, int csd0_size,
                                      int csd1_size, uint8_t *csd_0, uint8_t *csd_1) {

    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
    }

    jstring type = jniEnv->NewStringUTF(mime);
    jbyteArray csd0 = jniEnv->NewByteArray(csd0_size);
    jniEnv->SetByteArrayRegion(csd0, 0, csd0_size, reinterpret_cast<const jbyte *>(csd_0));
    jbyteArray csd1 = jniEnv->NewByteArray(csd1_size);
    jniEnv->SetByteArrayRegion(csd1, 0, csd1_size, reinterpret_cast<const jbyte *>(csd_1));

    jniEnv->CallVoidMethod(jobj, jmid_initmediacodec, type, width, height, csd0, csd1);

    jniEnv->DeleteLocalRef(csd0);
    jniEnv->DeleteLocalRef(csd1);
    jniEnv->DeleteLocalRef(type);
    javaVm->DetachCurrentThread();

}

void WlCallJava::onCallDecodeAVPacket(int datasize, uint8_t *packetdata) {

    JNIEnv *jniEnv;
    if (javaVm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
    }
    jbyteArray data = jniEnv->NewByteArray(datasize);
    jniEnv->SetByteArrayRegion(data, 0, datasize, reinterpret_cast<const jbyte *>(packetdata));
    jniEnv->CallVoidMethod(jobj, jmid_decodeavpacket, datasize, data);
    jniEnv->DeleteLocalRef(data);
    javaVm->DetachCurrentThread();


}


