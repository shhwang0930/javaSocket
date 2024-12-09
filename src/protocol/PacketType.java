package protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PacketType {
    CL_MSG(1),
    SER_TEST(2),
    CL_CONNECT(3),
    CL_DISCONNECT(4);
    private final int value;

    public static PacketType getPacketType(int value) {
        return switch (value){
            case 1 -> CL_MSG;
            case 2 -> SER_TEST;
            case 3 -> CL_CONNECT;
            case 4 -> CL_DISCONNECT;
            default -> null;
        };
    }
}
