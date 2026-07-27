#include <jni.h>
#include <string>
#include <android/log.log.h>

#define LOG_TAG "DevStudio_CPP"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_native_CppEngine_getNativeStatus(JNIEnv* env, jobject /* this */) {
    LOGI("DevStudio C++ Native Engine Initialized.");
    std::string message = "C++ Native Core (v1.0.0) Ready";
    return env->NewStringUTF(message.c_str());
}
