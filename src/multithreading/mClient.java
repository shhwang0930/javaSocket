package multithreading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class mClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8080);

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        DataInputStream dis = new DataInputStream(socket.getInputStream());

        dos.writeUTF("hello");

        String reponse = dis.readUTF();
        System.out.println("서버의 응답 : "+reponse);

        dis.close();
        dos.close();
        socket.close();
    }
}
