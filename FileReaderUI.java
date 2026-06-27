package FileEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;

public class FileReaderUI extends JFrame {

    private FileReaderCore reader;
    private String currentPath;

    private final JTable table;
    private final DefaultTableModel model;
    private final JSpinner sectorSpinner;
    private final JLabel statusLabel;
    private final JTextArea infoPanel;
    private boolean loadingTable = false;

    public FileReaderUI() {
        setTitle("Leitor de Arquivos Reais - Leitura e Edição de Byte");
        setSize(1250, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {"Offset", "Hexadecimal", "ASCII"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Permite editar somente a coluna Hexadecimal.
                return column == 1;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(520);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);

        JButton openButton = new JButton("Abrir arquivo real");
        JButton goButton = new JButton("Ler setor");
        sectorSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(openButton);
        toolbar.addSeparator();
        toolbar.add(new JLabel(" Setor: "));
        toolbar.add(sectorSpinner);
        toolbar.add(goButton);

        infoPanel = new JTextArea();
        infoPanel.setEditable(false);
        infoPanel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoPanel.setLineWrap(true);
        infoPanel.setWrapStyleWord(true);
        infoPanel.setText("Abra um arquivo real do computador para iniciar.\n\nO programa lê dados reais e permite editar somente um byte por vez.");

        JScrollPane infoScroll = new JScrollPane(infoPanel);
        infoScroll.setPreferredSize(new Dimension(420, 0));
        infoScroll.setBorder(BorderFactory.createTitledBorder("Informações reais do arquivo"));

        statusLabel = new JLabel(" Nenhum arquivo aberto");

        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(infoScroll, BorderLayout.EAST);
        add(statusLabel, BorderLayout.SOUTH);

        openButton.addActionListener(e -> openFile());
        goButton.addActionListener(e -> loadSector((Integer) sectorSpinner.getValue()));

        model.addTableModelListener(e -> {
            if (!loadingTable && e.getType() == TableModelEvent.UPDATE && e.getColumn() == 1) {
                editHexLine(e.getFirstRow());
            }
        });
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                if (reader != null) {
                    reader.close();
                }

                currentPath = file.getAbsolutePath();
                reader = new FileReaderCore(currentPath);

                statusLabel.setText(" " + file.getName() + " | " + reader.getFileSize() +
                        " bytes | " + reader.getTotalSectors() + " setores | leitura e edição de byte");

                sectorSpinner.setValue(0);
                loadSector(0);
                updateInfoPanel(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir arquivo: " + ex.getMessage());
            }
        }
    }

    private void loadSector(long sectorNumber) {
        if (reader == null) {
            JOptionPane.showMessageDialog(this, "Abra um arquivo primeiro.");
            return;
        }

        try {
            byte[] data = reader.readSector(sectorNumber);
            loadingTable = true;
            model.setRowCount(0);

            long baseOffset = sectorNumber * FileReaderCore.SECTOR_SIZE;

            for (int i = 0; i < data.length; i += 16) {
                StringBuilder hex = new StringBuilder();
                StringBuilder ascii = new StringBuilder();

                for (int j = 0; j < 16 && (i + j) < data.length; j++) {
                    hex.append(HexFormatter.byteToHex(data[i + j])).append(' ');
                    ascii.append(HexFormatter.byteToAscii(data[i + j]));
                }

                model.addRow(new Object[]{
                        String.format("0x%08X", baseOffset + i),
                        hex.toString().trim(),
                        ascii.toString()
                });
            }
            loadingTable = false;
        } catch (Exception ex) {
            loadingTable = false;
            JOptionPane.showMessageDialog(this, "Erro ao ler setor: " + ex.getMessage());
        }
    }

    private void editHexLine(int row) {
        if (reader == null || currentPath == null || row < 0) {
            return;
        }

        try {
            String offsetText = String.valueOf(model.getValueAt(row, 0));
            long rowOffset = parseOffset(offsetText);
            long currentSector = (Integer) sectorSpinner.getValue();
            byte[] sectorData = reader.readSector(currentSector);
            int rowStartInsideSector = (int) (rowOffset - (currentSector * FileReaderCore.SECTOR_SIZE));
            int bytesInRow = Math.min(16, sectorData.length - rowStartInsideSector);

            if (bytesInRow <= 0) {
                loadSector(currentSector);
                return;
            }

            byte[] oldBytes = new byte[bytesInRow];
            System.arraycopy(sectorData, rowStartInsideSector, oldBytes, 0, bytesInRow);

            String newHexText = String.valueOf(model.getValueAt(row, 1));
            byte[] newBytes = parseHexLine(newHexText);

            if (newBytes.length != oldBytes.length) {
                JOptionPane.showMessageDialog(this,
                        "A linha precisa manter a mesma quantidade de bytes: " + oldBytes.length + ".\n" +
                                "Altere apenas os valores, sem remover ou adicionar bytes.");
                loadSector(currentSector);
                return;
            }

            StringBuilder changes = new StringBuilder();
            int changedCount = 0;
            for (int i = 0; i < oldBytes.length; i++) {
                if (oldBytes[i] != newBytes[i]) {
                    changedCount++;
                    changes.append(String.format("Offset 0x%08X: 0x%02X -> 0x%02X\n",
                            rowOffset + i, oldBytes[i] & 0xFF, newBytes[i] & 0xFF));
                }
            }

            if (changedCount == 0) {
                loadSector(currentSector);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Arquivo real: " + currentPath + "\n\n" +
                            "Alterações detectadas na linha hexadecimal:\n\n" + changes + "\n" +
                            "Essas alterações serão salvas diretamente no arquivo. Deseja continuar?",
                    "Confirmar edição na linha Hexadecimal",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                loadSector(currentSector);
                return;
            }

            for (int i = 0; i < oldBytes.length; i++) {
                if (oldBytes[i] != newBytes[i]) {
                    reader.writeByte(rowOffset + i, newBytes[i]);
                }
            }

            loadSector(currentSector);
            updateInfoPanel(new File(currentPath));
            statusLabel.setText(" Linha hexadecimal editada: " + changedCount + " byte(s) alterado(s).");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao editar a linha Hexadecimal: " + ex.getMessage() + "\n\n" +
                            "Use valores hexadecimais de 2 dígitos separados por espaço. Exemplo: 4A FF 00 1B");
            try {
                loadSector((Integer) sectorSpinner.getValue());
            } catch (Exception ignored) {
            }
        }
    }

    private byte[] parseHexLine(String text) {
        String cleaned = text.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return new byte[0];
        }

        String[] parts = cleaned.split(" ");
        byte[] bytes = new byte[parts.length];

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].replace("0x", "").replace("0X", "");
            if (part.length() != 2) {
                throw new NumberFormatException("Cada byte precisa ter exatamente 2 dígitos hexadecimais.");
            }
            int value = Integer.parseInt(part, 16);
            if (value < 0 || value > 255) {
                throw new NumberFormatException("Byte fora do intervalo 00 a FF.");
            }
            bytes[i] = (byte) value;
        }
        return bytes;
    }


    private void editByte() {
        if (reader == null || currentPath == null) {
            JOptionPane.showMessageDialog(this, "Abra um arquivo real primeiro.");
            return;
        }

        String offsetText = JOptionPane.showInputDialog(this,
                "Digite o offset do byte que deseja alterar.\nExemplos: 0x1A ou 26");

        if (offsetText == null || offsetText.trim().isEmpty()) {
            return;
        }

        String valueText = JOptionPane.showInputDialog(this,
                "Digite o novo valor em hexadecimal.\nExemplo: FF");

        if (valueText == null || valueText.trim().isEmpty()) {
            return;
        }

        try {
            long offset = parseOffset(offsetText.trim());
            int value = Integer.parseInt(valueText.trim().replace("0x", ""), 16);

            if (value < 0 || value > 255) {
                JOptionPane.showMessageDialog(this, "O valor precisa estar entre 00 e FF.");
                return;
            }

            byte oldValue = reader.readByte(offset);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Arquivo real: " + currentPath + "\n\n" +
                            "Offset: " + String.format("0x%08X", offset) + "\n" +
                            "Valor atual: 0x" + HexFormatter.byteToHex(oldValue) + "\n" +
                            "Novo valor: 0x" + String.format("%02X", value) + "\n\n" +
                            "Essa alteração será feita diretamente no arquivo. Deseja continuar?",
                    "Confirmar edição de byte",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            byte previous = reader.writeByte(offset, (byte) value);
            long sector = offset / FileReaderCore.SECTOR_SIZE;
            sectorSpinner.setValue((int) sector);
            loadSector(sector);
            updateInfoPanel(new File(currentPath));

            statusLabel.setText(" Byte editado em " + String.format("0x%08X", offset) +
                    ": 0x" + HexFormatter.byteToHex(previous) +
                    " -> 0x" + String.format("%02X", value));

            JOptionPane.showMessageDialog(this,
                    "Byte alterado com sucesso.\n\n" +
                            "Antes: 0x" + HexFormatter.byteToHex(previous) + "\n" +
                            "Depois: 0x" + String.format("%02X", value));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Use offset decimal/hex e byte em hexadecimal, como FF.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao editar byte: " + ex.getMessage());
        }
    }

    private long parseOffset(String text) {
        if (text.startsWith("0x") || text.startsWith("0X")) {
            return Long.parseLong(text.substring(2), 16);
        }
        return Long.parseLong(text);
    }

    private void updateInfoPanel(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] header = new byte[16];
            int read = raf.read(header);
            byte[] actualHeader = new byte[Math.max(read, 0)];
            System.arraycopy(header, 0, actualHeader, 0, actualHeader.length);

            FileStatistics stats = FileStatistics.calculate(file.getAbsolutePath());
            HashCalculator hashes = HashCalculator.calculate(file.getAbsolutePath());

            StringBuilder firstBytes = new StringBuilder();
            for (byte b : actualHeader) {
                firstBytes.append(HexFormatter.byteToHex(b)).append(' ');
            }

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            StringBuilder sb = new StringBuilder();
            sb.append("=== ARQUIVO REAL LIDO ===\n");
            sb.append("Nome: ").append(file.getName()).append('\n');
            sb.append("Caminho: ").append(file.getAbsolutePath()).append('\n');
            sb.append("Tamanho: ").append(file.length()).append(" bytes\n");
            sb.append("Última modificação: ").append(format.format(file.lastModified())).append("\n");
            sb.append("Permissão de leitura: ").append(file.canRead() ? "sim" : "não").append("\n");
            sb.append("Permissão de escrita no sistema: ").append(file.canWrite() ? "sim" : "não").append("\n");
            sb.append("Modo do programa: leitura real + edição controlada de um byte por vez\n\n");

            sb.append("=== INFORMAÇÕES DO DISCO/PARTIÇÃO ===\n");
            sb.append("Espaço total: ").append(file.getTotalSpace()).append(" bytes\n");
            sb.append("Espaço livre: ").append(file.getFreeSpace()).append(" bytes\n");
            sb.append("Espaço utilizável: ").append(file.getUsableSpace()).append(" bytes\n\n");

            sb.append("=== TIPO DETECTADO PELOS BYTES ===\n");
            sb.append(MagicNumberDetector.detect(actualHeader)).append("\n\n");

            sb.append("=== PRIMEIROS 16 BYTES ===\n");
            sb.append(firstBytes).append("\n\n");

            sb.append("=== ESTATÍSTICAS REAIS ===\n");
            sb.append("Total de bytes: ").append(stats.totalBytes).append("\n");
            sb.append("Bytes 0x00: ").append(stats.zeros).append("\n");
            sb.append("Bytes imprimíveis ASCII: ").append(stats.printable).append("\n");
            sb.append("Bytes de controle: ").append(stats.control).append("\n");
            sb.append("Bytes estendidos: ").append(stats.extended).append("\n");
            sb.append(String.format("Entropia: %.4f\n", stats.entropy));
            sb.append("Interpretação: ").append(stats.getEntropyInterpretation()).append("\n");
            sb.append("Byte mais frequente: 0x").append(String.format("%02X", stats.mostFrequentByte));
            sb.append(" (").append(stats.mostFrequentCount).append(" vezes)\n\n");

            sb.append("=== HASHES DO ARQUIVO REAL ===\n");
            sb.append("MD5: ").append(hashes.md5).append("\n");
            sb.append("SHA-1: ").append(hashes.sha1).append("\n");
            sb.append("SHA-256: ").append(hashes.sha256).append("\n\n");

            sb.append("=== MODO DE OPERAÇÃO ===\n");
            sb.append("O arquivo é aberto com RandomAccessFile no modo \"rw\" para permitir edição de byte.\n");
            sb.append("O programa NÃO cria arquivo falso e NÃO gera disco simulado.\n");
            sb.append("A edição altera apenas o byte escolhido pelo usuário em um arquivo real.\n");

            infoPanel.setText(sb.toString());
            infoPanel.setCaretPosition(0);
        } catch (Exception ex) {
            infoPanel.setText("Erro ao analisar arquivo: " + ex.getMessage());
        }
    }
}

