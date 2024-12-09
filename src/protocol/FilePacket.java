package protocol;

import java.io.File;

public class FilePacket extends HeaderPacket{
    private final String fileName; // 파일 이름
    private final File file; //파일

    public FilePacket(String fileName, File file) {
        super(PacketType.CL_MSG, 8+fileName.getBytes().length);
        this.fileName = fileName;
        this.file = file;
    }
    public byte[] getBodyBytes() {// 메세지길이 + 메세지를 바이트로 변환
        byte[] fileDirBytes = fileName.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToBytes(fileDirBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(fileDirBytes, 0, bodyBytes, 4, fileDirBytes.length);
        return bodyBytes;
    }

    public static BodyPacket byteToBodyPacket(byte[] bodyBytes) {
        int messageLength = bytesToInt(bodyBytes, 8, 11);
        String message = new String(bodyBytes, 12, messageLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        return new BodyPacket(message);
    }
}
