import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {

        String encoding = System.getProperty("file.encoding");
        System.out.println("Default Encoding: " + encoding);
    }
}