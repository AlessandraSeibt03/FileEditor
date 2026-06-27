package FileEditor;

import java.util.LinkedHashMap;
import java.util.Map;

public class MagicNumberDetector {

    private static final Map<String, String> MAGIC_NUMBERS = new LinkedHashMap<>();

    static {
        MAGIC_NUMBERS.put("89504E470D0A1A0A", "Imagem PNG");
        MAGIC_NUMBERS.put("FFD8FF", "Imagem JPEG");
        MAGIC_NUMBERS.put("47494638", "Imagem GIF");
        MAGIC_NUMBERS.put("424D", "Imagem BMP");
        MAGIC_NUMBERS.put("25504446", "Documento PDF");
        MAGIC_NUMBERS.put("504B0304", "ZIP / DOCX / XLSX / JAR");
        MAGIC_NUMBERS.put("4D5A", "Executável Windows EXE/DLL");
        MAGIC_NUMBERS.put("7F454C46", "Executável Linux ELF");
        MAGIC_NUMBERS.put("CAFEBABE", "Arquivo Java .class");
        MAGIC_NUMBERS.put("1F8B08", "Arquivo GZIP");
        MAGIC_NUMBERS.put("526172211A0700", "Arquivo RAR");
        MAGIC_NUMBERS.put("3C3F786D6C", "Documento XML");
        MAGIC_NUMBERS.put("7B", "JSON ou texto iniciado por chave");
    }

    public static String detect(byte[] firstBytes) {
        if (firstBytes == null || firstBytes.length == 0) {
            return "Arquivo vazio";
        }

        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < Math.min(16, firstBytes.length); i++) {
            hex.append(String.format("%02X", firstBytes[i] & 0xFF));
        }

        String fileHex = hex.toString();
        for (Map.Entry<String, String> entry : MAGIC_NUMBERS.entrySet()) {
            if (fileHex.startsWith(entry.getKey())) {
                return entry.getValue() + " | assinatura: " + entry.getKey();
            }
        }

        boolean text = true;
        for (byte b : firstBytes) {
            int value = b & 0xFF;
            if (!(value == 9 || value == 10 || value == 13 || (value >= 32 && value <= 126))) {
                text = false;
                break;
            }
        }

        return text ? "Texto simples ou arquivo sem assinatura conhecida" : "Binário/desconhecido";
    }
}

