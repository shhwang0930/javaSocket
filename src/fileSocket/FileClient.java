package fileSocket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) throws Exception{

        Scanner sc = new Scanner(System.in);
        Socket sock = new Socket("", 15000);

        DataInputStream dis = new DataInputStream(sock.getInputStream());
        DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

        while(true) {
            int length = dis.readInt();

            for (int i = 0; i<length; i++) {
                String getName = dis.readUTF();
                System.out.println(getName);
            }

            System.out.print("다운 받으실 파일명을 입력하세요 : ");
            String downFile = sc.nextLine();

            dos.writeUTF(downFile);

            byte[] fileContants = new byte[(int)dis.readLong()];
            dis.readFully(fileContants);

            File dest = new File("C:/end/"+downFile);
            FileOutputStream fos = new FileOutputStream(dest);
            DataOutputStream dfos = new DataOutputStream(fos);
            dfos.write(fileContants);
            dfos.flush();
            dfos.close();

            System.out.println(dest.getName()+" 수신 완료!");

            System.out.print("종료하시겠습니까? y | n : ");
            String menu = sc.nextLine();
            if (menu.equals("y")) {
                System.exit(0);
            } else {
                continue;
            }
        }
    }
}
