package FileEditor;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReaderCore {

    public static final int SECTOR_SIZE = 512;

    private final RandomAccessFile raf;
    private final long totalSectors;

    public FileReaderCore(String path) throws IOException {
        this.raf = new RandomAccessFile(path, "rw");
        this.totalSectors = (raf.length() + SECTOR_SIZE - 1) / SECTOR_SIZE;
    }

    public byte[] readSector(long sectorNumber) throws IOException {
        long offset = sectorNumber * SECTOR_SIZE;

        if (offset >= raf.length()) {
            return new byte[0];
        }

        raf.seek(offset);

        byte[] buffer = new byte[SECTOR_SIZE];
        int read = raf.read(buffer);

        if (read < SECTOR_SIZE) {
            byte[] trimmed = new byte[Math.max(read, 0)];
            System.arraycopy(buffer, 0, trimmed, 0, trimmed.length);
            return trimmed;
        }

        return buffer;
    }

    public byte readByte(long offset) throws IOException {
        validateOffset(offset);
        raf.seek(offset);
        return raf.readByte();
    }

    public byte writeByte(long offset, byte newValue) throws IOException {
        validateOffset(offset);
        raf.seek(offset);
        byte oldValue = raf.readByte();
        raf.seek(offset);
        raf.write(newValue & 0xFF);
        return oldValue;
    }

    private void validateOffset(long offset) throws IOException {
        if (offset < 0 || offset >= raf.length()) {
            throw new IOException("Offset inválido. Use um valor entre 0 e " + (raf.length() - 1) + ".");
        }
    }

    public long getFileSize() throws IOException {
        return raf.length();
    }

    public long getTotalSectors() {
        return totalSectors;
    }

    public void close() throws IOException {
        raf.close();
    }
}

