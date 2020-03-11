//
// Created by hxuro on 2019/11/20.
//

#ifndef WY_MUSIC_ANDROID_H
#define WY_MUSIC_ANDROID_H

#endif //WY_MUSIC_ANDROID_H

#include "android/log.h"


#define LOGD(FORMAT, ...) __android_log_print(ANDROID_LOG_DEBUG,"xurent",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"xurent",FORMAT,##__VA_ARGS__);

#define LOG_DEBUG  false