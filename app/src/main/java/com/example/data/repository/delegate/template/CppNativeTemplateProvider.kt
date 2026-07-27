package com.example.data.repository.delegate.template

import com.example.data.db.ProjectFileEntity

object CppNativeTemplateProvider {
    fun getFiles(projectId: Long): List<ProjectFileEntity> {
        return listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "engine.cpp",
                path = "/cpp/engine.cpp",
                extension = "cpp",
                parentPath = "/cpp",
                content = """
                    #include <jni.h>
                    #include <string>

                    extern "C" JNIEXPORT jstring JNICALL
                    Java_com_example_native_CppEngine_calculate(JNIEnv* env, jobject /* this */, jint a, jint b) {
                        int result = a * b;
                        std::string message = "C++ Engine Result: " + std::to_string(result);
                        return env->NewStringUTF(message.c_str());
                    }
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "CMakeLists.txt",
                path = "/CMakeLists.txt",
                extension = "txt",
                content = """
                    cmake_minimum_required(VERSION 3.22.1)
                    project("cppengine")

                    add_library(cppengine SHARED cpp/engine.cpp)
                """.trimIndent()
            )
        )
    }
}
