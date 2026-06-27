package FileEditor;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileStatistics {

    public long totalBytes;
    public long zeros;
    public long printable;
    public long control;
    public long extended;
    public double entropy;
    public int mostFrequentByte;
    public long mostFrequentCount;

    public static FileStatistics calculate(String path) throws IOException {
        FileStatistics stats = new FileStatistics();
        long[] frequency = new long[256];

        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            stats.totalBytes = raf.length();
            byte[] buffer = new byte[8192];
            int read;

            while ((read = raf.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    int b = buffer[i] & 0xFF;
                    frequency[b]++;

                    if (b == 0) stats.zeros++;
                    else if (b >= 32 && b <= 126) stats.printable++;
                    else if (b < 32) stats.control++;
                    else stats.extended++;
                }
            }
        }

        if (stats.totalBytes == 0) {
            return stats;
        }

        for (int i = 0; i < 256; i++) {
            if (frequency[i] > 0) {
                double p = (double) frequency[i] / stats.totalBytes;
                stats.entropy -= p * (Math.log(p) / Math.log(2));
            }

            if (frequency[i] > stats.mostFrequentCount) {
                stats.mostFrequentCount = frequency[i];
                stats.mostFrequentByte = i;
            }
        }

        return stats;
    }

    public String getEntropyInterpretation() {
        if (entropy < 1.0) return "Muito baixa: dados repetitivos";
        if (entropy < 4.0) return "Baixa: provável texto estruturado";
        if (entropy < 6.0) return "Média: arquivo binário comum";
        if (entropy < 7.5) return "Alta: arquivo compactado";
        return "Muito alta: provável arquivo criptografado, compactado ou aleatório";
    }
}

