package pubsub;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class Pclient3 {
    public static void main(String[] args) {
        Thread systemIn;
        // 서버 p와 포트로 연결되는 소켓채널 생성
        try(SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 15000))){
            //모니터 출력에 출력할 채널 생성
            WritableByteChannel out = Channels.newChannel(System.out);

            //버퍼 생성
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //출력을 담당할 스레드 생성 및 실행
            systemIn = new Thread(new SystemIn(socket));
            systemIn.start();

            while (true){
                socket.read(buf);
                buf.flip();
                out.write(buf);
                buf.clear();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
