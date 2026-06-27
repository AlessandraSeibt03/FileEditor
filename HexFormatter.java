package FileEditor;

public class HexFormatter {

    public static String byteToHex(byte b) {
        return String.format("%02X", b & 0xFF);
    }

    public static char byteToAscii(byte b) {
        int value = b & 0xFF;
        return (value >= 32 && value <= 126) ? (char) value : '.';
    }
}
