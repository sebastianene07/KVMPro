# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.10.2)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("app")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp)
# For cmake debug gadget
#get_cmake_property(_variableNames VARIABLES)
#foreach (_variableName ${_variableNames})
#    message( "${_variableName}=${${_variableName}}")
#endforeach()

string(REPLACE android- android ANDROID_TOOLCHAIN_PREFIX_SEB
        ${ANDROID_TOOLCHAIN_PREFIX}
        )
#
add_custom_command(
        OUTPUT ${CMAKE_CURRENT_SOURCE_DIR}/kvmtool/libkvmtool.so
        COMMAND make ARCH=${ANDROID_ARCH_NAME} CC=${ANDROID_TOOLCHAIN_PREFIX_SEB}${ANDROID_PLATFORM_LEVEL}-${ANDROID_TOOLCHAIN} LIBFDT_DIR=${CMAKE_CURRENT_SOURCE_DIR}/dtc/libfdt AR=${CMAKE_AR} LD=${CMAKE_LINKER} V=1 libkvmtool.so
        DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/dtc/libfdt/libfdt.a
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/kvmtool)

add_custom_command(
        OUTPUT ${CMAKE_CURRENT_SOURCE_DIR}/dtc/libfdt/libfdt.a
        COMMAND make ARCH=${ANDROID_ARCH_NAME} CC=${ANDROID_TOOLCHAIN_PREFIX_SEB}${ANDROID_PLATFORM_LEVEL}-${ANDROID_TOOLCHAIN} NO_PYTHON=1 AR=${CMAKE_AR} libfdt/libfdt.a
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/dtc
)

add_custom_target(
        libkvmtool
        DEPENDS ${CMAKE_CURRENT_SOURCE_DIR}/kvmtool/libkvmtool.so
)

add_dependencies(${CMAKE_PROJECT_NAME} libkvmtool)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log
        ${CMAKE_CURRENT_SOURCE_DIR}/kvmtool/libkvmtool.so)

add_custom_command(
        TARGET ${CMAKE_PROJECT_NAME}  POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy
        ${CMAKE_CURRENT_SOURCE_DIR}/kvmtool/libkvmtool.so
        ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/arm64-v8a/libkvmtool.so)