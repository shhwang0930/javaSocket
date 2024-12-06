package multiFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MfServer {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(15000);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        while(true) {
            Socket socket = server.accept();
            executorService.execute(new MfClientHandler(socket));
        }
    }
}
class MfClientHandler implements Runnable {
    private Socket sock;

    public MfClientHandler(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {
        try {
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            DataInputStream dis = new DataInputStream(sock.getInputStream());


            File dir = new File("C:/start/");
            File[] files = dir.listFiles();

            dos.writeInt(files.length);

            for (File f : files) {
                dos.writeUTF(f.getName());
            }
            String loadingFile = dis.readUTF();

            File file = new File("C:/start/" + loadingFile);
            FileInputStream fis = new FileInputStream(file);
            DataInputStream fsis = new DataInputStream(fis);

            byte[] filecontants = new byte[(int)file.length()];
            fsis.readFully(filecontants);

            dos.writeLong(filecontants.length);
            dos.write(filecontants);
            dos.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

