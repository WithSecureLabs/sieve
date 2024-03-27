#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_withsecure_example_sieve_service_CryptoService_runNDKdecrypt(JNIEnv *env, jobject thiz,
                                                              jstring yaystrkeyyay, jbyteArray yayencryptedtextyay) {

    // get the length of the result array and key
    jsize yayencryptedlengthyay = env->GetArrayLength(yayencryptedtextyay);
    jsize yaykeylengthyay = env->GetStringLength(yaystrkeyyay);

    // get a pointer to the bytes of the result array
    jbyte *yayencryptedcharsyay = env->GetByteArrayElements(yayencryptedtextyay, nullptr);
    const jchar *yaykeycharsyay = env->GetStringChars(yaystrkeyyay, nullptr);

    // create a new char array to store the decrypted result
    jcharArray yaydecryptedarrayyay = env->NewCharArray(yayencryptedlengthyay);
    jchar *yaydecryptedcharsyay = env->GetCharArrayElements(yaydecryptedarrayyay, nullptr);

    // perform XOR operation on each byte of the result array
    // iterate through result array
    for (int i = 0; i < yayencryptedlengthyay; ++i) {
        // iterate through key
        for (int i2 = 0; i2 < yaykeylengthyay; ++i2) {
            // convert the result of XOR to jchar and store in the decrypted array
            jchar xor_key = yaykeycharsyay[i2]; // ken added
            yaydecryptedcharsyay[i] = static_cast<jchar>(yayencryptedcharsyay[i] ^ xor_key);
        }

    }

    // set the chars in the decrypted array
    env->SetCharArrayRegion(yaydecryptedarrayyay, 0, yayencryptedlengthyay, yaydecryptedcharsyay);

    // release resources
    env->ReleaseByteArrayElements(yayencryptedtextyay, yayencryptedcharsyay, 0);
    env->ReleaseCharArrayElements(yaydecryptedarrayyay, yaydecryptedcharsyay, 0);

    // convert the decrypted array to jstring
    jstring yaydecryptedstringyay = env->NewString(yaydecryptedcharsyay, yayencryptedlengthyay);

    // return the decrypted string
    return yaydecryptedstringyay;
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_withsecure_example_sieve_service_CryptoService_runNDKencrypt(JNIEnv *env, jobject thiz,
                                                              jstring yaystringkeyyay, jstring yayplaintextyay) {

    // get the length of the input string and key
    jsize yayplaintextlengthyay = env->GetStringLength(yayplaintextyay);
    jsize yaykeylengthyay = env->GetStringLength(yaystringkeyyay);

    // get a pointer to the characters of the input string and input key
    const jchar *yayplaintextcharsyay = env->GetStringChars(yayplaintextyay, nullptr);
    const jchar *yaykeycharsyay = env->GetStringChars(yaystringkeyyay, nullptr);

    // create a new byte array to store the result
    jbyteArray yayresultarrayyay = env->NewByteArray(yayplaintextlengthyay);

    // get a pointer to the bytes of the result array
    jbyte *yayresultcharsyay = env->GetByteArrayElements(yayresultarrayyay, nullptr);

    // perform XOR operation on each character of the input string
    // iterate through every character in the plaintext value
    for (int i = 0; i < yayplaintextlengthyay; ++i) {
        // iterate through every character in the key
        for (int i2 = 0; i2 < yaykeylengthyay; ++i2) {
            // convert the result of XOR to jbyte and store in the result array
            jchar xor_key = yaykeycharsyay[i2];
            yayresultcharsyay[i] = static_cast<jbyte>(yayplaintextcharsyay[i] ^ xor_key);
        }

    }

    // set the bytes in the result array
    env->SetByteArrayRegion(yayresultarrayyay, 0, yayplaintextlengthyay, yayresultcharsyay);

    // release resources
    env->ReleaseStringChars(yayplaintextyay, yayplaintextcharsyay);
    env->ReleaseByteArrayElements(yayresultarrayyay, yayresultcharsyay, 0);

    // return the encrypted or decrypted result array
    return yayresultarrayyay;
}