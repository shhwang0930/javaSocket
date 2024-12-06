package multiFile;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class MfClient2 {
    public static void main(String[] args) {
        try{
            Scanner sc = new Scanner(System.in);
            Socket socket = new Socket("", 15000);

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

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

                File dest = new File("C:/end2/"+downFile);
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
        }catch (IOException e){

        }
    }
}
