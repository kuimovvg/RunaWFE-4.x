#include <jni.h>

extern "C" {
/*
 * Class:     org_jbpm_ui_util_InfoPathSupport
 * Method:    isXSNFileValid
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_jbpm_ui_util_InfoPathSupport_isXSNFileValid(JNIEnv *, jclass, jstring);

/*
 * Class:     org_jbpm_ui_util_InfoPathSupport
 * Method:    createInfoPathXSNFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_jbpm_ui_util_InfoPathSupport_createInfoPathXSNFile(JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     org_jbpm_ui_util_InfoPathSupport
 * Method:    extractFileFromXSN
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_jbpm_ui_util_InfoPathSupport_extractFileFromXSN(JNIEnv *, jclass, jstring, jstring, jstring, jstring);

/*
 * Class:     org_jbpm_ui_util_InfoPathSupport
 * Method:    extractAllFilesFromXSN
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_jbpm_ui_util_InfoPathSupport_extractAllFilesFromXSN(JNIEnv *, jclass, jstring, jstring, jstring);

}
