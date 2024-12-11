package protocol;

import lombok.Getter;

@Getter
public class ConnectPacket extends HeaderPacket {
    private static final String DEFAULT_MESSAGE = "사용자가 연결되었습니다.";
    private String message;
    public ConnectPacket() {
        super(PacketType.CL_CONNECT, 4+DEFAULT_MESSAGE.getBytes().length);
        this.message = DEFAULT_MESSAGE;
    }

    public byte[] getConnectBytes() {// 메세지길이 + 메세지를 바이트로 변환
        byte[] messageBytes = DEFAULT_MESSAGE.getBytes();
        byte[] bodyBytes = new byte[bodyLength];
        System.arraycopy(intToBytes(messageBytes.length), 0, bodyBytes, 0, 4);
        System.arraycopy(messageBytes, 0, bodyBytes, 4, messageBytes.length);
        return bodyBytes;
    }

    public static ConnectPacket byteToConnectPacket(byte[] bytes) {
        int messageLength = bytesToInt(bytes, 8, 11);
        String message = new String(bytes, 12, messageLength); //인덱스 15 + nameLength부터 messageLength만큼 문자열로 변환

        return new ConnectPacket();
    }
}
