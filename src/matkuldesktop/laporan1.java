package matkuldesktop;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.HashSet;
import java.util.Set;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

// Ekspor:
import javax.swing.SpinnerDateModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;


public class laporan1 extends javax.swing.JFrame {
    private int currentPage = 1, totalPage = 1, rowsPerPage = 30;
    private Connection conn;
    // komponen UI
    private JTable jtlaporan;
    private DefaultTableModel model;
    private JTextField tfCariIdAbsen, tfCariIdKaryawan, tfCariNama;
    private JComboBox<String> cbProdi, cbJabatan, cbShift, cbKeterangan, cbViewMode;
    private JCheckBox cbUrutShift;
    private JCheckBox cbUseDate;
    private JSpinner spinnerTanggalMulai, spinnerTanggalAkhir;
    private JButton btnPrev, btnNext, btnSearch, btnExportPDF, btnExportExcel;
    private JLabel lblHalaman, lblTotal;

        // semua kolom untuk reset/hide
    private final String[] allColumns = {
        "ID Absen","ID Karyawan","Nama","Tanggal",
        "Jam Masuk","Jam Istirahat","Jam Kembali","Jam Pulang",
        "Hadir","Terlambat","Terlambat Kembali","Lembur",
        "Prodi","Jabatan","Shift","Keterangan"
    };

   public laporan1() {
        super("Laporan Absensi");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        Koneksi.Getkoneksi();
        conn = Koneksi.Getkoneksi(); 

        initFilterPanel();
        initTable();
        initBottomPanel();

        btnSearch.addActionListener(e -> loadData(1));
        btnPrev.addActionListener(e -> loadData(currentPage - 1));
        btnNext.addActionListener(e -> loadData(currentPage + 1));
        btnExportExcel.addActionListener(e -> exportDialog("Excel"));
        btnExportPDF.addActionListener(e -> exportDialog("PDF"));

        setVisible(true);
        loadData(1);
    }
   private void initFilterPanel() {
        Font font = new Font("Segoe UI", Font.PLAIN, 12);

        tfCariIdAbsen = new JTextField(8);
        tfCariIdAbsen.setFont(font);

        tfCariIdKaryawan = new JTextField(8);
        tfCariIdKaryawan.setFont(font);

        tfCariNama = new JTextField(12);
        tfCariNama.setFont(font);

        cbViewMode = new JComboBox<>(new String[]{"Semua Data", "Tabel Harian", "Per Karyawan"});
        cbViewMode.setFont(font);

        cbUrutShift = new JCheckBox("Urut berdasarkan Shift");
        cbUrutShift.setFont(font);
        cbUrutShift.setOpaque(false);

        cbProdi = new JComboBox<>();
        cbJabatan = new JComboBox<>();
        cbShift = new JComboBox<>();
        cbKeterangan = new JComboBox<>();
        cbUseDate = new JCheckBox("Filter Tanggal");
        cbUseDate.setOpaque(false);
        cbProdi.setFont(font);
        cbJabatan.setFont(font);
        cbShift.setFont(font);
        cbKeterangan.setFont(font);
        cbUseDate.setFont(font);

        spinnerTanggalMulai = new JSpinner(new SpinnerDateModel());
        spinnerTanggalMulai.setEditor(new JSpinner.DateEditor(spinnerTanggalMulai, "yyyy-MM-dd"));
        spinnerTanggalMulai.setFont(font);

        spinnerTanggalAkhir = new JSpinner(new SpinnerDateModel());
        spinnerTanggalAkhir.setEditor(new JSpinner.DateEditor(spinnerTanggalAkhir, "yyyy-MM-dd"));
        spinnerTanggalAkhir.setFont(font);

        btnSearch = new JButton("Cari");
        btnSearch.setFont(font);

        // Panel utama atas
        JPanel panelAtas = new JPanel(new BorderLayout(10, 10));
        panelAtas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Mode tampilan
        JPanel panelMode = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelMode.setOpaque(false);
        JLabel lblMode = new JLabel("Mode Tampilan:");
        lblMode.setFont(font);
        panelMode.add(lblMode);
        panelMode.add(cbViewMode);
        panelMode.add(cbUrutShift);
        panelAtas.add(panelMode, BorderLayout.NORTH);

        // Panel filter utama
        JPanel panelFilter = new JPanel(new GridLayout(2, 1, 5, 5));
        panelFilter.setOpaque(false);

        // Baris pencarian
        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelCari.setOpaque(false);

        JLabel lblIdAbsen = new JLabel("ID Absen:");
        lblIdAbsen.setFont(font);
        panelCari.add(lblIdAbsen);
        panelCari.add(tfCariIdAbsen);

        JLabel lblIdKaryawan = new JLabel("ID Karyawan:");
        lblIdKaryawan.setFont(font);
        panelCari.add(lblIdKaryawan);
        panelCari.add(tfCariIdKaryawan);

        JLabel lblNama = new JLabel("Nama:");
        lblNama.setFont(font);
        panelCari.add(lblNama);
        panelCari.add(tfCariNama);

        panelCari.add(btnSearch);
        panelFilter.add(panelCari);

        // Baris dropdown dan tanggal
        JPanel panelDropdown = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelDropdown.setOpaque(false);

        JLabel lblProdi = new JLabel("Prodi:");
        lblProdi.setFont(font);
        panelDropdown.add(lblProdi);
        panelDropdown.add(cbProdi);

        JLabel lblJabatan = new JLabel("Jabatan:");
        lblJabatan.setFont(font);
        panelDropdown.add(lblJabatan);
        panelDropdown.add(cbJabatan);

        JLabel lblShift = new JLabel("Shift:");
        lblShift.setFont(font);
        panelDropdown.add(lblShift);
        panelDropdown.add(cbShift);

        JLabel lblKet = new JLabel("Keterangan:");
        lblKet.setFont(font);
        panelDropdown.add(lblKet);
        panelDropdown.add(cbKeterangan);

        panelDropdown.add(cbUseDate);

        JLabel lblMulai = new JLabel("Mulai:");
        lblMulai.setFont(font);
        panelDropdown.add(lblMulai);
        panelDropdown.add(spinnerTanggalMulai);

        JLabel lblSampai = new JLabel("Sampai:");
        lblSampai.setFont(font);
        panelDropdown.add(lblSampai);
        panelDropdown.add(spinnerTanggalAkhir);

        panelFilter.add(panelDropdown);
        panelAtas.add(panelFilter, BorderLayout.CENTER);
        add(panelAtas, BorderLayout.NORTH);

        // Load pilihan dropdown
        loadFilterOptions();
        cbUseDate.setSelected(false);

        // Listener perubahan langsung trigger loadData
        tfCariIdAbsen.addActionListener(e -> loadData(1));
        tfCariIdKaryawan.addActionListener(e -> loadData(1));
        tfCariNama.addActionListener(e -> loadData(1));

        cbProdi.addActionListener(e -> loadData(1));
        cbJabatan.addActionListener(e -> loadData(1));
        cbShift.addActionListener(e -> loadData(1));
        cbKeterangan.addActionListener(e -> loadData(1));
        cbUseDate.addActionListener(e -> loadData(1));
        cbUrutShift.addActionListener(e -> loadData(1));
        cbViewMode.addActionListener(e -> {
            applyViewMode();
            loadData(1);
        });

        spinnerTanggalMulai.addChangeListener(e -> {
            if (cbUseDate.isSelected()) loadData(1);
        });

        spinnerTanggalAkhir.addChangeListener(e -> {
            if (cbUseDate.isSelected()) loadData(1);
        });

        // Live search untuk input teks
        addLiveSearch(tfCariIdAbsen);
        addLiveSearch(tfCariIdKaryawan);
        addLiveSearch(tfCariNama);
    }

   private void addLiveSearch(JTextField textField) {
        textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loadData(1);
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loadData(1);
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                loadData(1);
            }
        });
    }

   private void initTable() {
    model = new DefaultTableModel(allColumns, 0);
    jtlaporan = new JTable(model) {
        // Stripe baris
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            } else {
                c.setBackground(new Color(204, 229, 255)); // saat dipilih
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    };

    // Umum
    jtlaporan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    jtlaporan.setForeground(Color.DARK_GRAY);
    jtlaporan.setRowHeight(28);
    jtlaporan.setGridColor(new Color(220, 220, 220));
    jtlaporan.setShowGrid(true);
    jtlaporan.setIntercellSpacing(new Dimension(1, 1));
    jtlaporan.setFillsViewportHeight(true);
    jtlaporan.setAutoCreateRowSorter(true);

    // Header
    JTableHeader header = jtlaporan.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 12));
    header.setBackground(new Color(240, 240, 240));
    header.setForeground(Color.BLACK);
    header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));

    // Scroll pane + border
    JScrollPane scrollPane = new JScrollPane(jtlaporan);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
    scrollPane.getViewport().setBackground(Color.WHITE);

    add(scrollPane, BorderLayout.CENTER);
}

   private void initBottomPanel() {
    JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    panelBawah.setBackground(Color.WHITE); 
    Font font = new java.awt.Font("Segoe UI", Font.PLAIN, 12);

    btnPrev = new JButton("<< Prev");
    btnNext = new JButton("Next >>");
    lblHalaman = new JLabel("Halaman 0/0");
    lblTotal = new JLabel("Total: 0");
    btnExportPDF = new JButton("Export PDF");
    btnExportExcel = new JButton("Export Excel");

    // Terapkan font ke semua komponen
    btnPrev.setFont(font);
    btnNext.setFont(font);
    btnExportPDF.setFont(font);
    btnExportExcel.setFont(font);
    lblHalaman.setFont(font);
    lblTotal.setFont(font);

    // Styling tombol agar modern
    Color btnColor = new Color(70, 130, 180); // steel blue
    Color textColor = Color.WHITE;

    JButton[] buttons = {btnPrev, btnNext, btnExportPDF, btnExportExcel};
    for (JButton btn : buttons) {
        btn.setBackground(btnColor);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    panelBawah.add(btnPrev);
    panelBawah.add(btnNext);
    panelBawah.add(lblHalaman);
    panelBawah.add(lblTotal);
    panelBawah.add(btnExportPDF);
    panelBawah.add(btnExportExcel);

    add(panelBawah, BorderLayout.SOUTH);
}

   private void loadFilterOptions() {
        try (Statement st = conn.createStatement()) {
            cbProdi.addItem("All");
            ResultSet rs = st.executeQuery("SELECT idprodi, prodi FROM tprodi");
            while(rs.next()) cbProdi.addItem(rs.getString(1)+" - "+rs.getString(2));
            cbJabatan.addItem("All");
            rs = st.executeQuery("SELECT idjabatan, jabatan FROM tjabatan");
            while(rs.next()) cbJabatan.addItem(rs.getString(1)+" - "+rs.getString(2));
            cbShift.addItem("All");
            rs = st.executeQuery("SELECT idshift, namashift FROM tshift");
            while(rs.next()) cbShift.addItem(rs.getString(1)+" - "+rs.getString(2));
            cbKeterangan.addItem("All");
            rs = st.executeQuery("SELECT DISTINCT keterangan FROM tketerangan");
            while(rs.next()) cbKeterangan.addItem(rs.getString(1));
        } catch(SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal load filter: "+ex.getMessage());
        }
    }

  private void applyViewMode() {
    resetTableColumns(); // PENTING: kembalikan semua kolom dulu
    String mode = (String)cbViewMode.getSelectedItem();

    if ("Tabel Harian".equals(mode)) {
        hideColumns(new String[]{"Hadir", "Terlambat", "Terlambat Kembali", "Lembur"});
    } else if ("Semua Data".equals(mode)) {
        hideColumns(new String[]{"Hadir", "Terlambat", "Terlambat Kembali", "Lembur"});
    } else if ("Per Karyawan".equals(mode)) {
        hideColumns(new String[]{
            "ID Absen", "Tanggal", "Jam Masuk", "Jam Istirahat",
            "Jam Kembali", "Jam Pulang", "Shift", "Keterangan"
        });
}

}


private void resetTableColumns() {
    TableColumnModel columnModel = jtlaporan.getColumnModel();
    // Clear dulu semua kolom dari tampilan
    while (columnModel.getColumnCount() > 0) {
        columnModel.removeColumn(columnModel.getColumn(0));
    }

    // Tambah ulang semua kolom dari model
    TableModel model = jtlaporan.getModel();
    for (int i = 0; i < model.getColumnCount(); i++) {
        TableColumn column = new TableColumn(i);
        column.setHeaderValue(model.getColumnName(i));
        columnModel.addColumn(column);
    }
}

   private void hideColumns(String[] cols) {
    TableColumnModel cm = jtlaporan.getColumnModel();
    for (int i = cm.getColumnCount() - 1; i >= 0; i--) {
        String header = cm.getColumn(i).getHeaderValue().toString();
        // cek apakah header ini ada di array cols
        for (int j = 0; j < cols.length; j++) {
            if (header.equals(cols[j])) {
                cm.removeColumn(cm.getColumn(i));
                break;
            }
        }
    }
}
   private void resetTabelKosong() {
        model.setRowCount(0);
        lblHalaman.setText("Halaman 0 / 0");
        lblTotal.setText("Total: 0");
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
    }

private void loadData(int page) {
    currentPage = page;
    model.setRowCount(0); 

    // 1) Build WHERE clause
    String where = " WHERE 1=1 ";
    List<Object> params = new ArrayList<>();

    if (!tfCariIdAbsen.getText().trim().isEmpty()) {
        where += " AND t.idabsen LIKE ? ";
        params.add("%" + tfCariIdAbsen.getText().trim() + "%");
    }
    if (!tfCariIdKaryawan.getText().trim().isEmpty()) {
        where += " AND k.idkaryawan LIKE ? ";
        params.add("%" + tfCariIdKaryawan.getText().trim() + "%");
    }
    if (!tfCariNama.getText().trim().isEmpty()) {
        where += " AND k.namakaryawan LIKE ? ";
        params.add("%" + tfCariNama.getText().trim() + "%");
    }
    if (cbProdi.getSelectedIndex() > 0) {
        String idp = cbProdi.getSelectedItem().toString().split(" - ")[0];
        where += " AND k.idprodi = ? ";
        params.add(Integer.parseInt(idp));
    }
    if (cbJabatan.getSelectedIndex() > 0) {
        String idj = cbJabatan.getSelectedItem().toString().split(" - ")[0];
        where += " AND k.idjabatan = ? ";
        params.add(Integer.parseInt(idj));
    }
    if (cbShift.getSelectedIndex() > 0) {
        String ids = cbShift.getSelectedItem().toString().split(" - ")[0];
        where += " AND t.idshift = ? ";
        params.add(Integer.parseInt(ids));
    }
    if (cbKeterangan.getSelectedIndex() > 0) {
        where += " AND ket.keterangan = ? ";
        params.add(cbKeterangan.getSelectedItem().toString());
    }

    String mode = (String) cbViewMode.getSelectedItem();
    if ("Tabel Harian".equals(mode)) {
        LocalDate today = LocalDate.now();
        java.sql.Date todaySql = java.sql.Date.valueOf(today);
        where += " AND t.tanggal = ? ";
        params.add(todaySql);
    }

    if (cbUseDate.isSelected()) {
        Date d1 = (Date) spinnerTanggalMulai.getValue();
        Date d2 = (Date) spinnerTanggalAkhir.getValue();
        where += " AND t.tanggal BETWEEN ? AND ? ";
        params.add(new java.sql.Date(d1.getTime()));
        params.add(new java.sql.Date(d2.getTime()));
    }

    try {
        // 2a) Hitung total rows
        String sqlCount = 
            "SELECT COUNT(*) FROM tabsensi t " +
            "JOIN tkaryawan k ON t.idkaryawan = k.idkaryawan " +
            "JOIN tjabatan j ON k.idjabatan = j.idjabatan " +
            "JOIN tprodi p   ON k.idprodi   = p.idprodi   " +
            "JOIN tshift s   ON t.idshift   = s.idshift   " +
            "JOIN tketerangan ket ON t.id_keterangan = ket.id_keterangan " + where;

        PreparedStatement pstCount = conn.prepareStatement(sqlCount);
        for (int i = 0; i < params.size(); i++) {
            pstCount.setObject(i + 1, params.get(i));
        }

        ResultSet rsCount = pstCount.executeQuery();
        int totalRows = rsCount.next() ? rsCount.getInt(1) : 0;
        totalPage = (int) Math.ceil(totalRows / (double) rowsPerPage);

        if (!tfCariIdAbsen.getText().trim().isEmpty() && totalRows == 0) {
            JOptionPane.showMessageDialog(this, "Data dengan ID Absen tersebut tidak ditemukan.", "Pencarian Kosong", JOptionPane.INFORMATION_MESSAGE);
            resetTabelKosong();
            return;
        }
        if (!tfCariIdKaryawan.getText().trim().isEmpty() && totalRows == 0) {
            JOptionPane.showMessageDialog(this, "Data dengan ID Karyawan tersebut tidak ditemukan.", "Pencarian Kosong", JOptionPane.INFORMATION_MESSAGE);
            resetTabelKosong();
            return;
        }
        if (!tfCariNama.getText().trim().isEmpty() && totalRows == 0) {
            JOptionPane.showMessageDialog(this, "Data dengan nama karyawan tersebut tidak ditemukan.", "Pencarian Kosong", JOptionPane.INFORMATION_MESSAGE);
            resetTabelKosong();
            return;
        }

        // 2b) Query data per halaman
        boolean isPerKaryawan = "Per Karyawan".equals(mode);
        int offset = (currentPage - 1) * rowsPerPage;

        String sqlData;
        if (isPerKaryawan) {
            sqlData =
                "SELECT k.idkaryawan, k.namakaryawan, " +
                "       k.hadir, k.terlambat, k.terlambat_kembali, k.lembur, " +
                "       p.prodi, j.jabatan " +
                "FROM tkaryawan k " +
                "JOIN tprodi p ON k.idprodi = p.idprodi " +
                "JOIN tjabatan j ON k.idjabatan = j.idjabatan " +
                where.replace("t.", "k.") +
                " ORDER BY k.idkaryawan ASC " +
                "LIMIT ? OFFSET ?";
        } else {
            sqlData =
                "SELECT t.idabsen, t.idkaryawan, k.namakaryawan, t.tanggal, " +
                "       t.jammasuk, t.jamistirahat, t.jamkembali, t.jampulang, " +
                "       ket.keterangan AS Hadir, " +
                "       k.terlambat, k.terlambat_kembali, k.lembur, " +
                "       p.prodi, j.jabatan, s.namashift, ket.keterangan " +
                "FROM tabsensi t " +
                "JOIN tkaryawan k ON t.idkaryawan = k.idkaryawan " +
                "JOIN tjabatan j ON k.idjabatan = j.idjabatan " +
                "JOIN tprodi p   ON k.idprodi   = p.idprodi   " +
                "JOIN tshift s   ON t.idshift   = s.idshift   " +
                "JOIN tketerangan ket ON t.id_keterangan = ket.id_keterangan " +
                where +
                (cbUrutShift.isSelected()
                    ? " ORDER BY s.namashift ASC, t.tanggal DESC, t.idabsen DESC "
                    : " ORDER BY t.tanggal DESC, t.idabsen DESC ") +
                "LIMIT ? OFFSET ?";
        }

        PreparedStatement pst = conn.prepareStatement(sqlData);
        int idx = 1;
        for (Object o : params) {
            pst.setObject(idx++, o);
        }
        pst.setInt(idx++, rowsPerPage);
        pst.setInt(idx, offset);

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            if (isPerKaryawan) {
                Object[] row = new Object[] {
                    null,
                    rs.getString("idkaryawan"),
                    rs.getString("namakaryawan"),
                    null, null, null, null, null,
                    rs.getInt("hadir"),
                    rs.getInt("terlambat"),
                    rs.getInt("terlambat_kembali"),
                    rs.getInt("lembur"),
                    rs.getString("prodi"),
                    rs.getString("jabatan"),
                    null,
                    null
                };
                model.addRow(row);
            } else {
                Object[] row = new Object[] {
                    rs.getInt("idabsen"),
                    rs.getString("idkaryawan"),
                    rs.getString("namakaryawan"),
                    rs.getDate("tanggal"),
                    rs.getTime("jammasuk"),
                    rs.getTime("jamistirahat"),
                    rs.getTime("jamkembali"),
                    rs.getTime("jampulang"),
                    rs.getObject("Hadir"),
                    rs.getInt("terlambat"),
                    rs.getInt("terlambat_kembali"),
                    rs.getInt("lembur"),
                    rs.getString("prodi"),
                    rs.getString("jabatan"),
                    rs.getString("namashift"),
                    rs.getString("keterangan")
                };
                model.addRow(row);
            }
        }

        // Update UI bagian bawah
        lblHalaman.setText("Halaman " + currentPage + " / " + totalPage);
        lblTotal.setText("Total: " + totalRows);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPage);

        applyViewMode();

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error load data: " + ex.getMessage());
    }
}
    
private void exportDialog(String type) {
    JFileChooser chooser = new JFileChooser();
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        String path = chooser.getSelectedFile().getAbsolutePath();
        if ("Excel".equalsIgnoreCase(type)) {
            exportToExcel(path.endsWith(".xlsx") ? path : path + ".xlsx");
        } else if ("PDF".equalsIgnoreCase(type)) {
            exportToPDF(path.endsWith(".pdf") ? path : path + ".pdf");
        }
    }
}

private void exportToExcel(String path) {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Laporan");
        Row header = sheet.createRow(0);

        // Buat header kolom
        for (int i = 0; i < model.getColumnCount(); i++) {
            header.createCell(i).setCellValue(model.getColumnName(i));
        }

        // Buat data
        for (int r = 0; r < model.getRowCount(); r++) {
            Row row = sheet.createRow(r + 1);
            for (int c = 0; c < model.getColumnCount(); c++) {
                Object val = model.getValueAt(r, c);
                row.createCell(c).setCellValue(val == null ? "" : val.toString());
            }
        }

        try (FileOutputStream out = new FileOutputStream(path)) {
            workbook.write(out);
        }

        JOptionPane.showMessageDialog(this, "Berhasil ekspor ke Excel.");
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal ekspor Excel: " + ex.getMessage());
    }
}

private void exportToPDF(String path) {
    try {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        PdfWriter.getInstance(document, new FileOutputStream(path));
        document.open();
        com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(model.getColumnCount());

        // header
        for (int i = 0; i < model.getColumnCount(); i++) {
            pdfTable.addCell(model.getColumnName(i));
        }

        // data
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int c = 0; c < model.getColumnCount(); c++) {
                Object val = model.getValueAt(r, c);
                pdfTable.addCell(val == null ? "" : val.toString());
            }
        }

        document.add(pdfTable);
        document.close();
        JOptionPane.showMessageDialog(this, "Berhasil ekspor ke PDF.");
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal ekspor PDF: " + ex.getMessage());
    }
}


 @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 554, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 680, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> new laporan1().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
