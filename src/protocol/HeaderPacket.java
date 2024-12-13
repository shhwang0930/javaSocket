package protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static protocol.PacketType.getPacketType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class HeaderPacket {
    protected PacketType type; // enum클래스로 int로 반환할 수 있도록
    protected int bodyLength;

    public byte[] getHeaderBytes(PacketType type, int bodyLength) {
        byte[] headerBytes = new byte[8];
        //arraycopy(원본, 첫시작 위치, 복사하려는 대상, 복사대상 시작 위치, 원본에서 복사본까지 읽어올 길이)
        System.arraycopy(intToBytes(type.getValue()), 0, headerBytes, 0, 4);
        System.arraycopy(intToBytes(bodyLength), 0, headerBytes, 4, 4);
        return headerBytes;
    }

    /*public byte[] intToBytes(int i) { // 패킷의 값들을 바이트 배열로 변환
        byte[] b = new byte[4];
        b[0] = (byte) ((i & 0xFF000000) >> 24);
        b[1] = (byte) ((i & 0x00FF0000) >> 16);
        b[2] = (byte) ((i & 0x0000FF00) >> 8);
        b[3] = (byte) (i & 0x000000FF);
        return b;
    }*/

    public byte[] intToBytes(int i) { // 리틀 엔디안 방식
        byte[] b = new byte[4];
        b[0] = (byte) (i & 0x000000FF);         // LSB
        b[1] = (byte) ((i & 0x0000FF00) >> 8);
        b[2] = (byte) ((i & 0x00FF0000) >> 16);
        b[3] = (byte) ((i & 0xFF000000) >> 24); // MSB
        return b;
    }

    public static int bytesToInt(byte[] b, int start, int end) {
        int result;
        result = ((b[start] & 0xFF) << 24) |
                ((b[start + 1] & 0xFF) << 16) |
                ((b[start + 2] & 0xFF) << 8) |
                (b[start + 3] & 0xFF);
        return result;
    }

    public static PacketType byteToPacketType(byte[] b) {
        int type = bytesToInt(b, 0, 3);
        return getPacketType(type);
    }

}