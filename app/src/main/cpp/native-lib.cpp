#include <jni.h>
#include <string>

extern "C" int handle_command(struct cmd_struct *command, int argc, const char **argv);
extern "C" struct cmd_struct kvm_commands[];

extern "C"
JNIEXPORT int JNICALL
Java_com_example_kvmpro_MainActivity_startVMJni(JNIEnv *env, jobject thiz, jobject vm) {
    const char *args[80];
    int argc = 0;

    jclass cls = env->GetObjectClass(vm);

    args[argc++] = "run";

    // Extract the disk name:
    args[argc++] = "-d";
    jfieldID diskFid = env->GetFieldID(cls, "diskFileName", "Ljava/lang/String;");
    jstring diskImageS = (jstring)env->GetObjectField(vm, diskFid);
    args[argc++] = env->GetStringUTFChars(diskImageS, NULL);

    // Extract the kernel image name:
    args[argc++] = "-k";
    jfieldID kernelImageFid = env->GetFieldID(cls, "kernelImageFilename", "Ljava/lang/String;");
    jstring kernelImageS = (jstring)env->GetObjectField(vm, kernelImageFid);
    args[argc++] = env->GetStringUTFChars(kernelImageS, NULL);

    handle_command(kvm_commands, argc, args);
}