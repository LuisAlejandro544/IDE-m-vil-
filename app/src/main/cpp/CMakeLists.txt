# CMakeLists.txt for DevStudio C++ Native Library
cmake_minimum_required(VERSION 3.22.1)

project("devstudio_cpp")

add_library(
    devstudio_cpp
    SHARED
    devstudio_cpp.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    devstudio_cpp
    ${log-lib}
)
