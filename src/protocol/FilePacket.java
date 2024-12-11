package protocol;

import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Getter
public class FilePacket extends HeaderPacket{
    private final String fileName; // 파일 이름
    private final File file; //파일

    public FilePacket(String fileName, File file) throws IOException {
        super(PacketType.CL_FILE, 8+fileName.getBytes().length+ Files.readAllBytes(file.toPath()).length);
        this.fileName = fileName;
        this.file = file;
    }
    public byte[] getFileBytes() throws IOException {// 메세지길이 + 메세지를 바이트로 변환
        byte[] fileDirBytes = fileName.getBytes();
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] bodyBytes = new byte[bodyLength];
        System.out.println("file length : "+fileBytes.length);
        System.arraycopy(intToBytes(fileDirBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(fileDirBytes, 0, bodyBytes, 4, fileDirBytes.length);
        System.arraycopy(intToBytes(Files.readAllBytes(file.toPath()).length), 0, bodyBytes, 4+fileDirBytes.length, 4);
        System.arraycopy(Files.readAllBytes(file.toPath()), 0, bodyBytes, 8+fileDirBytes.length, Files.readAllBytes(file.toPath()).length);
        return bodyBytes;
    }

    public static FilePacket byteToFilePacket(byte[] bodyBytes) throws IOException {
        int fileNameLength = bytesToInt(bodyBytes, 8, 11);
        String fileName = new String(bodyBytes, 12, fileNameLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환
        int fileLength = bytesToInt(bodyBytes, 12+fileNameLength, 15+fileNameLength);
        File file = writeByteArrayToFile(bodyBytes, fileName);
        return new FilePacket(fileName,file);
    }

    public static File writeByteArrayToFile(byte[] byteArray, String fileName) throws IOException {
        File file = new File("C:/end/"+fileName);
        byte[] fileDirBytes = fileName.getBytes();
        byte [] fileDetails = new byte[byteArray.length-16-fileDirBytes.length];
        System.arraycopy(byteArray, 16+fileDirBytes.length, fileDetails, 0, fileDetails.length);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileDetails);
        }
        return file;
    }
}
