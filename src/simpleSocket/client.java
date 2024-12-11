package simpleSocket;

import protocol.BodyPacket;
import protocol.ConnectPacket;
import protocol.DisconnectPacket;
import protocol.FilePacket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.System.out;

public class client {
    public static void main(String[] args) {
        Socket socket = null;
        DataOutputStream dos = null;
        Scanner sc = new Scanner(System.in);
        String command = "";
        try{
            String serverIp = "127.0.0.1";
            out.println("서버에 연결중입니다. 서버 IP : " + serverIp);
            // 소켓을 생성하여 연결을 요청한다.
            socket = new Socket(serverIp, 8000);

            // 소켓의 입력스트림을 얻는다
            OutputStream out = socket.getOutputStream();
            dos = new DataOutputStream(out);

            while (true){
                System.out.println("명령을 입력하세요 : ");
                command = sc.nextLine();

                if(command.equals("1")){ // 메세지 전송
                    String msg;
                    System.out.println("채팅을 입력하세요 : ");
                    msg = sc.nextLine();

                    BodyPacket bodyPacket = new BodyPacket(msg);
                    System.out.println("body type : "+bodyPacket.getType().toString());
                    System.out.println("length : "+bodyPacket.getBodyLength());
                    byte[] bodyBytes = bodyPacket.getBodyBytes();
                    byte[] headerBytes = bodyPacket.getHeaderBytes(bodyPacket.getType(), bodyPacket.getBodyLength());
                    byte[] packetbytedata = new byte[headerBytes.length + bodyBytes.length];
                    System.arraycopy(headerBytes, 0, packetbytedata, 0, headerBytes.length);
                    System.arraycopy(bodyBytes, 0, packetbytedata, headerBytes.length, bodyBytes.length);

                    dos.write(packetbytedata);
                    dos.flush();
                }else if(command.equals("2")){ // 입장
                    ConnectPacket connectPacket = new ConnectPacket();
                    byte[] bodyBytes = connectPacket.getConnectBytes();
                    byte[] headerBytes = connectPacket.getHeaderBytes(connectPacket.getType(), connectPacket.getBodyLength());

                    byte[] packetbytedata = new byte[headerBytes.length + bodyBytes.length];
                    System.arraycopy(headerBytes, 0, packetbytedata, 0, headerBytes.length);
                    System.arraycopy(bodyBytes, 0, packetbytedata, headerBytes.length, bodyBytes.length);

                    dos.write(packetbytedata);
                    dos.flush();
                }else if(command.equals("4")){
                    String fileName;
                    System.out.println("파일이름을 입력하세요 : ");
                    fileName = sc.nextLine();
                    File file = new File("C:/start/"+fileName);
                    FilePacket filePacket = new FilePacket(fileName, file);

                    byte[] bodyBytes = filePacket.getFileBytes();
                    byte[] headerBytes = filePacket.getHeaderBytes(filePacket.getType(), filePacket.getBodyLength());

                    byte[] packetbytedata = new byte[headerBytes.length + bodyBytes.length];
                    System.arraycopy(headerBytes, 0, packetbytedata, 0, headerBytes.length);
                    System.arraycopy(bodyBytes, 0, packetbytedata, headerBytes.length, bodyBytes.length);
                    System.out.println("length : "+packetbytedata.length);
                    dos.write(packetbytedata);
                    dos.flush();
                }
                else if(command.equals("3")){ // 퇴장
                    DisconnectPacket disconnectPacket = new DisconnectPacket();
                    byte[] bodyBytes = disconnectPacket.getDisconnectBytes();
                    byte[] headerBytes = disconnectPacket.getHeaderBytes(disconnectPacket.getType(), disconnectPacket.getBodyLength());

                    byte[] packetbytedata = new byte[headerBytes.length + bodyBytes.length];
                    System.arraycopy(headerBytes, 0, packetbytedata, 0, headerBytes.length);
                    System.arraycopy(bodyBytes, 0, packetbytedata, headerBytes.length, bodyBytes.length);

                    dos.write(packetbytedata);
                    dos.flush(); //write후 데이터 전송

                    break;
                }

            }

            // 스트림과 소켓을 닫는다
            dos.close();
            socket.close();
            System.out.println("연결이 종료되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sc != null) sc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
