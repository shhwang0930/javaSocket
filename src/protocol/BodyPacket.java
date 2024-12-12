package protocol;

import lombok.Getter;

@Getter
public class BodyPacket extends HeaderPacket{
    //private final int msgLength;
    private final String message;
    public BodyPacket(String message) {
        super(PacketType.CL_MSG, 4+message.getBytes().length);
        this.message = message;
    }

    public byte[] getBodyBytes() {// 클라이언트에서 메세지길이 + 메세지를 바이트로 변환
        byte[] messageBytes = message.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToBytes(messageBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(messageBytes, 0, bodyBytes, 4, messageBytes.length);
        return bodyBytes;
    }

    public static BodyPacket byteToBodyPacket(byte[] bytes) {
        int messageLength = bytesToInt(bytes, 8, 11);
        String message = new String(bytes, 12, messageLength); //인덱스 15 + 부터 messageLength만큼 문자열로 변환

        return new BodyPacket(message);
    }
}