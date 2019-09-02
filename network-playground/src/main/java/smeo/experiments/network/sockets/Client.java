package smeo.experiments.network.sockets;

import java.nio.ByteBuffer;

/**
 * http://stackoverflow.com/questions/6470295/how-to-write-read-the-direct-bytebuffer-in-the-native
 * http://stackoverflow.com/questions/12187996/a-simple-rule-of-when-i-should-use-direct-buffers-with-java-nio-for-network-i-o
 * https://worldmodscode.wordpress.com/2012/12/14/the-java-bytebuffer-a-crash-course/
 */
public class Client {
    public static void main(String[] args) {
        ByteBuffer myBuffer = ByteBuffer.allocateDirect(128);

    }

//    JNIEXPORT jboolean JNICALL Java_nfore_android_bt_pro_nfhfp_rcvSco(JNIEnv *env, jobject this, jint fd, jobject buff){
//
//        int buff_size;
//        int socketfd;
//
//        jbyte *BUFF = (*env)->GetDirectBufferAddress(env, buff);
//        buff_size = (*env)->GetDirectBufferCapacity(env, buff);
//
//        socketfd = fd; .....}
}
