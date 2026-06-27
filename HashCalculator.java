package FileEditor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {

    public String md5;
    public String sha1;
    public String sha256;

    public static HashCalculator calculate(String path) throws IOException, NoSuchAlgorithmException {
        HashCalculator result = new HashCalculator();

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = raf.read(buffer)) != -1) {
                md5.update(buffer, 0, read);
                sha1.update(buffer, 0, read);
                sha256.update(buffer, 0, read);
            }
        }

        result.md5 = bytesToHex(md5.digest());
        result.sha1 = bytesToHex(sha1.digest());
        result.sha256 = bytesToHex(sha256.digest());

        return result;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

