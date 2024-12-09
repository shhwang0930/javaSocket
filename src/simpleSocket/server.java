package simpleSocket;

import protocol.BodyPacket;
import protocol.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static protocol.BodyPacket.byteToBodyPacket;
import static protocol.HeaderPacket.byteToPacketType;

public class server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(8000);
            System.out.println("open server");
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                System.out.println("waiting for connection");
                socket = serverSocket.accept();
                System.out.println(socket.getInetAddress() + "로 연결 요청이 들어옴");

                // 서버의 InputStream
                InputStream inputStream = socket.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                // 클라이언트로부터 데이터를 지속적으로 읽어들임
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    System.out.println("read");
                    // 데이터를 다 읽었다면 처리 시작
                    byte[] receivedBytes = byteArrayOutputStream.toByteArray();
                    if (receivedBytes.length > 0) {
                        // 헤더부분의 패킷에서 메세지타입 추출
                        PacketType clientPacketType = byteToPacketType(receivedBytes);
                        System.out.println(clientPacketType.toString());

                        BodyPacket bodyPacket = byteToBodyPacket(receivedBytes);
                        String msg = bodyPacket.getMessage().toString();

                        // 클라이언트가 보낸 메시지 출력
                        System.out.println("Received message: " + msg);

                        // 소켓을 닫지 않고 계속해서 새로운 패킷을 받음
                        byteArrayOutputStream.reset();  // 버퍼 초기화
                    }
                }
                byteArrayOutputStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}