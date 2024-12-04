package simpleSocket;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(8000);
            System.out.println("open server");
        }catch (Exception e){
            e.printStackTrace();
        }

        while(true){
            try{
                System.out.println("waiting for connection");
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress()+"로 연결 요청이 들어옴");


                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);

                dos.writeUTF("test message");
                System.out.println("데이터 전송");

                out.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}