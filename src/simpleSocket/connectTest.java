package simpleSocket;

import protocol.BodyPacket;
import protocol.ConnectPacket;
import protocol.DisconnectPacket;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import static java.lang.System.out;

public class connectTest {
    public static void main(String[] args) {
        try{
            String serverIp = "127.0.0.1";
            out.println("서버에 연결중입니다. 서버 IP : " + serverIp);
            // 소켓을 생성하여 연결을 요청한다.
            Socket socket = new Socket(serverIp, 8000);

            // 소켓의 입력스트림을 얻는다
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);

            // 소켓으로 부터 받은 데이터를 출력한다
            String msg = "client test message";

            DisconnectPacket disconnectPacket = new DisconnectPacket();
            System.out.println("body type : "+disconnectPacket.getType().toString());
            System.out.println("length : "+disconnectPacket.getBodyLength());
            byte[] bodyBytes = disconnectPacket.getDisconnectBytes();
            byte[] headerBytes = disconnectPacket.getHeaderBytes(disconnectPacket.getType(), disconnectPacket.getBodyLength());

            byte[] packetbytedata = new byte[headerBytes.length + bodyBytes.length];
            System.arraycopy(headerBytes, 0, packetbytedata, 0, headerBytes.length);
            System.arraycopy(bodyBytes, 0, packetbytedata, headerBytes.length, bodyBytes.length);

            dos.write(packetbytedata);

            // 스트림과 소켓을 닫는다
            dos.close();
            socket.close();
            System.out.println("연결이 종료되었습니다.");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
