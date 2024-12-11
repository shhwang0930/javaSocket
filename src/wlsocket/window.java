package wlsocket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class window {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try{
            // 서버 소켓을 생성하여 7777번 포트와 결합시킨다.
            serverSocket = new ServerSocket(8888);
            System.out.println("서버가 준비되었습니다.");
        }catch(Exception e){
            e.printStackTrace();
        }

        while (true) {
            try {
                System.out.println("연결요청을 기다립니다.");
                // 서버소켓은 클라이언트의 연결요청이 올 때까지 실행을 멈추고 계속 기다린다.
                // 클라이언트의 연결요청이 오면 클라이언트 소켓과 통신할 새로운 소켓을 생성한다.
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress() +"로부터 연결 요청이 들어왔습니다.");

                // 소켓의 출력 스트림을 얻는다
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);

                // 원격 소켓(remote socket)에 데이터를 보낸다.
                dos.writeUTF("윈도우에서 리눅스로 메세지 전송 성공");
                System.out.println("데이터를 전송했습니다.");

                // 스트림과 소켓을 닫아준다
                out.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }// while
    }// main
}
