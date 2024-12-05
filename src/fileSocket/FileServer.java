package fileSocket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    public static void main(String[] args) throws Exception {
        try {
            ServerSocket server = new ServerSocket(15000);
            Socket sock = null;

            while(true) {
                System.out.println("클라이언트 접속 대기 중");
                sock =  server.accept();
                System.out.println(sock.getInetAddress()+ " 접속");
                try {
                    while(true) {
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
                    }
                } catch(Exception e){
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("연결 오류");
        }
    }
}
