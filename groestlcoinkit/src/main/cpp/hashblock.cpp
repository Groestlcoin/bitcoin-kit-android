/**
 * Created by Hash Engineering on 4/24/14 for the X11 algorithm
 */
#include "hashblock.h"
#include <inttypes.h>

#include <jni.h>

extern "C" jbyteArray JNICALL Java_io_horizontalsystems_groestlcoinkit_GroestlHasher_groestldNative(JNIEnv *env,
                                                                                                    jobject cls, jbyteArray header, jint offset, jint length)
{
    jint Plen = (env)->GetArrayLength(header);
    jbyte *P = (env)->GetByteArrayElements(header, NULL);
    jbyteArray DK = NULL;

    if (P)
	{
        uint256 result = HashGroestl(P+offset, P+length+offset);
        DK = (env)->NewByteArray(32);
        if (DK)
        {
            (env)->SetByteArrayRegion(DK, 0, 32, (jbyte *) result.begin());
        }
        if (P) (env)->ReleaseByteArrayElements(header, P, JNI_ABORT);
	}
    return DK;
}