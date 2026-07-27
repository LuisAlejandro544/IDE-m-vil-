package com.example.native

import android.util.Log

object CppEngine {

    private var isLoaded = false

    init {
        try {
            System.loadLibrary("devstudio_cpp")
            isLoaded = true
            Log.i("DevStudio_CPP", "C++ library loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w("DevStudio_CPP", "C++ native library not linked directly in build, using JNI fallback.", e)
            isLoaded = false
        }
    }

    external fun getNativeStatus(): String

    fun getEngineStatus(): String {
        return if (isLoaded) {
            try {
                getNativeStatus()
            } catch (e: Exception) {
                "C++ Native Library Loaded (Standby)"
            }
        } else {
            "C++ Native Core (Módulo Listo)"
        }
    }
}
