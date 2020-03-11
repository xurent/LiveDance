cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/include)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")
add_library( # Sets the name of the library.
        native-lib

        SHARED
        src/main/cpp/PushCallJava.cpp
        src/main/cpp/PushQueue.cpp
        src/main/cpp/RtmpPush.cpp
        src/main/cpp/native-lib.cpp
        src/main/cpp/WlCallJava.cpp
        src/main/cpp/WlFFmpeg.cpp
        src/main/cpp/WlAudio.cpp
        src/main/cpp/WlQueue.cpp
        src/main/cpp/WlPlaystatus.cpp
        src/main/cpp/Video.cpp

        src/main/cpp/librtmp/amf.c
        src/main/cpp/librtmp/hashswf.c
        src/main/cpp/librtmp/log.c
        src/main/cpp/librtmp/parseurl.c
        src/main/cpp/librtmp/rtmp.c
        )

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

add_library(avcodec-57 SHARED IMPORTED)
set_target_properties(avcodec-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavcodec-57.so)


add_library(avdevice-57 SHARED IMPORTED)
set_target_properties(avdevice-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavdevice-57.so)


add_library(avfilter-6 SHARED IMPORTED)
set_target_properties(avfilter-6
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavfilter-6.so)


add_library(avformat-57 SHARED IMPORTED)
set_target_properties(avformat-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavformat-57.so)


add_library(avutil-55 SHARED IMPORTED)
set_target_properties(avutil-55
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavutil-55.so)

add_library(postproc-54 SHARED IMPORTED)
set_target_properties(postproc-54
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libpostproc-54.so)

add_library(swresample-2 SHARED IMPORTED)
set_target_properties(swresample-2
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswresample-2.so)


add_library(swscale-4 SHARED IMPORTED)
set_target_properties(swscale-4
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswscale-4.so)


target_link_libraries( # Specifies the target library.
        native-lib
        avcodec-57
        avdevice-57
        avfilter-6
        avformat-57
        avutil-55
        postproc-54
        swresample-2
        swscale-4
        OpenSLES
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})