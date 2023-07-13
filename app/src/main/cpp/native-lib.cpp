#include <jni.h>
#include <string>

extern "C" int handle_command(struct cmd_struct *command, int argc, const char **argv);
extern "C" struct cmd_struct kvm_commands[];

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_kvmpro_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    const char *args[80];
    args[0] = "run";
    args[1] = "-k";
    args[2] = "Image";

    // DEMO: ./lkvm-static run -k Image

    handle_command(kvm_commands, 4, args);

    return env->NewStringUTF(hello.c_str());
}