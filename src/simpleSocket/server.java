package simpleSocket;

import protocol.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
                ArrayList<Byte> byteArrayList = new ArrayList<>();
                byte[] buffer = new byte[200000];
                //이미지 파일 전송을 위해 크기 증가 시킴
                //하지만 너무 커지면 프로그램에서 사용하는 메모리양이 증가됨
                //채팅과 같은 단순 문자열을 전송할때에는 실제로 읽는 데이터보다 메모리의 낭비 예상됨
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
                        //System.out.println(clientPacketType.toString());

                        if(clientPacketType.equals(PacketType.CL_MSG)){
                            BodyPacket bodyPacket = byteToBodyPacket(receivedBytes);
                            String msg = bodyPacket.getMessage();

                            // 클라이언트가 보낸 메시지 출력
                            System.out.println("Received message: " + msg);

                            // 소켓을 닫지 않고 계속해서 새로운 패킷을 받음
                            byteArrayOutputStream.reset();  // 버퍼 초기화
                        }
                        else if(clientPacketType.equals(PacketType.CL_CONNECT)){
                            ConnectPacket connectPacket = ConnectPacket.byteToConnectPacket(receivedBytes);
                            String msg = connectPacket.getMessage();
                            System.out.println("Connect!! : " + msg);
                            byteArrayOutputStream.reset();
                        }
                        else if(clientPacketType.equals(PacketType.CL_DISCONNECT)){
                            DisconnectPacket disconnectPacket = DisconnectPacket.byteToDisconnectPacket(receivedBytes);
                            String msg = disconnectPacket.getMessage();
                            System.out.println("Disonnect!! : " + msg);
                            byteArrayOutputStream.reset();
                        }
                        else if(clientPacketType.equals(PacketType.CL_FILE)){
                            FilePacket filePacket = FilePacket.byteToFilePacket(receivedBytes);
                            String fileName = filePacket.getFileName();

                            System.out.println("fileName : "+fileName);
                            byteArrayOutputStream.reset();
                        }
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