package multithreading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class mServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started");

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        //최대 10개의 스레드를 동시 처리 가능

        //무한 반복문을 통해 언제든지 클라이언트의 연결을 받을 준비
        while(true){
            System.out.println("server wating");
            Socket socket = serverSocket.accept();
            executorService.execute(new ClientHandler(socket));
        }
    }
}
class ClientHandler implements Runnable{
    private Socket socket; //클라이언트와의 통신을 위한 소켓

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String message = dis.readUTF();
            System.out.println("message : "+ message);

            dos.writeUTF("메세지 수신 : "+ message);

            // 소켓 닫기
            // 닫지않으면 > 리소스 누수 발생, 버퍼내의 데이터 손실가능성, 불필요한 네트워크 자원 사용
            dis.close();
            dos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
