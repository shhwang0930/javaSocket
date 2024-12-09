package protocol;

import lombok.Getter;

@Getter
public class BodyPacket extends HeaderPacket{
    private final String message;
    public BodyPacket(String message) {
        super(PacketType.CL_MSG, 8+message.getBytes().length);
        this.message = message;
    }
    public byte[] getBodyBytes() {// 메세지길이 + 메세지를 바이트로 변환
        byte[] messageBytes = message.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToBytes(messageBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(messageBytes, 0, bodyBytes, 4, messageBytes.length);
        return bodyBytes;
    }

    public static BodyPacket byteToBodyPacket(byte[] bodyBytes) {
        int messageLength = bytesToInt(bodyBytes, 8, 11);
        String message = new String(bodyBytes, 12, messageLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        return new BodyPacket(message);
    }
}