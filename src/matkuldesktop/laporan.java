package matkuldesktop;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

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


public class laporan extends javax.swing.JFrame {
    private int currentPage = 1, totalPage = 1, rowsPerPage = 30;
    private Connection conn;
    // komponen UI
    private JTable jtlaporan;
    private DefaultTableModel model;
    private JTextField tfCariIdAbsen, tfCariIdKaryawan, tfCariNama;
    private JComboBox<String> cbProdi, cbJabatan, cbShift, cbKeterangan, cbViewMode;
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

    public laporan() {
        super("Laporan Absensi");
        setSize(900, 600);
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
    JPanel panelFilter = new JPanel(new GridLayout(2,1,5,5));

    // baris pencarian
    JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
    panelCari.add(new JLabel("ID Absen:"));
    tfCariIdAbsen = new JTextField(5); panelCari.add(tfCariIdAbsen);
    panelCari.add(new JLabel("ID Karyawan:"));
    tfCariIdKaryawan = new JTextField(5); panelCari.add(tfCariIdKaryawan);
    panelCari.add(new JLabel("Nama:"));
    tfCariNama = new JTextField(10); panelCari.add(tfCariNama);
    btnSearch = new JButton("Search"); panelCari.add(btnSearch);
    panelFilter.add(panelCari);

    // baris dropdown + tanggal
    JPanel panelDropdown = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
    cbProdi = new JComboBox<>(); cbJabatan = new JComboBox<>();
    cbShift = new JComboBox<>(); cbKeterangan = new JComboBox<>();
    cbUseDate = new JCheckBox("Filter Tanggal");
    spinnerTanggalMulai = new JSpinner(new SpinnerDateModel());
    spinnerTanggalMulai.setEditor(new JSpinner.DateEditor(spinnerTanggalMulai,"yyyy-MM-dd"));
    spinnerTanggalAkhir = new JSpinner(new SpinnerDateModel());
    spinnerTanggalAkhir.setEditor(new JSpinner.DateEditor(spinnerTanggalAkhir,"yyyy-MM-dd"));
    panelDropdown.add(new JLabel("Prodi:")); panelDropdown.add(cbProdi);
    panelDropdown.add(new JLabel("Jabatan:")); panelDropdown.add(cbJabatan);
    panelDropdown.add(new JLabel("Shift:")); panelDropdown.add(cbShift);
    panelDropdown.add(new JLabel("Keterangan:")); panelDropdown.add(cbKeterangan);
    panelDropdown.add(cbUseDate);
    panelDropdown.add(new JLabel("Mulai:")); panelDropdown.add(spinnerTanggalMulai);
    panelDropdown.add(new JLabel("Sampai:")); panelDropdown.add(spinnerTanggalAkhir);
    panelFilter.add(panelDropdown);

    // mode tampilan
    JPanel panelMode = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
    panelMode.add(new JLabel("Mode Tampilan:"));
    cbViewMode = new JComboBox<>(new String[]{"Semua Data","Tabel Harian","Per Karyawan"});
    cbViewMode.addActionListener(e -> applyViewMode());
    panelMode.add(cbViewMode);

    // gabung filter + mode
    JPanel panelAtas = new JPanel();
    panelAtas.setLayout(new BorderLayout());
    panelAtas.add(panelMode, BorderLayout.NORTH);
    panelAtas.add(panelFilter, BorderLayout.CENTER);

    // tambahkan ke frame
    add(panelAtas, BorderLayout.NORTH);

    // load opsi filter
    loadFilterOptions();
    cbUseDate.setSelected(false);
}

    private void initTable() {
        model = new DefaultTableModel(allColumns, 0);
        jtlaporan = new JTable(model);
        jtlaporan.setAutoCreateRowSorter(true);
        add(new JScrollPane(jtlaporan), BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        btnPrev = new JButton("<< Prev");
        btnNext = new JButton("Next >>");
        lblHalaman = new JLabel("Halaman 0/0");
        lblTotal = new JLabel("Total: 0");
        btnExportPDF = new JButton("Export PDF");
        btnExportExcel = new JButton("Export Excel");
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
            while(rs.next()) cbShift.addItem(rs.getString(1)+" - "+rs.getString(1));
            cbKeterangan.addItem("All");
            rs = st.executeQuery("SELECT DISTINCT keterangan FROM tketerangan");
            while(rs.next()) cbKeterangan.addItem(rs.getString(1));
        } catch(SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal load filter: "+ex.getMessage());
        }
    }

    private void applyViewMode() {
        String mode = (String)cbViewMode.getSelectedItem();
        resetTableColumns();
        if("Tabel Harian".equals(mode)) {
            hideColumns(new String[]{"Hadir","Terlambat","Terlambat Kembali","Lembur"});
        } else if("Per Karyawan".equals(mode)) {
            hideColumns(new String[]{"Jam Masuk","Jam Istirahat","Jam Kembali","Jam Pulang","Keterangan"});
        }
    }

    private void resetTableColumns() {
        TableColumnModel cm = jtlaporan.getColumnModel();
        while(cm.getColumnCount()>0) cm.removeColumn(cm.getColumn(0));
        for(String name: allColumns) {
            TableColumn c = new TableColumn(model.findColumn(name));
            c.setHeaderValue(name);
            cm.addColumn(c);
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

    
    private void loadData(int page) {
    currentPage = page;
    model.setRowCount(0);  // kosongkan table dulu

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
    if (cbUseDate.isSelected()) {
        Date d1 = (Date)spinnerTanggalMulai.getValue();
        Date d2 = (Date)spinnerTanggalAkhir.getValue();
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
            "JOIN tketerangan ket ON t.id_keterangan = ket.id_keterangan "
            + where;
        PreparedStatement pstCount = conn.prepareStatement(sqlCount);
        for (int i = 0; i < params.size(); i++) {
            pstCount.setObject(i+1, params.get(i));
        }
        ResultSet rsCount = pstCount.executeQuery();
        int totalRows = rsCount.next() ? rsCount.getInt(1) : 0;
        totalPage = (int)Math.ceil(totalRows / (double)rowsPerPage);

        // 2b) Query data page
        int offset = (currentPage - 1) * rowsPerPage;
        String sqlData = 
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
            "JOIN tketerangan ket ON t.id_keterangan = ket.id_keterangan "
            + where +
            " ORDER BY t.tanggal DESC, t.idabsen DESC " +
            " LIMIT ? OFFSET ?";

        PreparedStatement pst = conn.prepareStatement(sqlData);
        int idx = 1;
        for (Object o : params) {
            pst.setObject(idx++, o);
        }
        pst.setInt(idx++, rowsPerPage);
        pst.setInt(idx, offset);

        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            Object[] row = new Object[] {
                rs.getInt("idabsen"),
                rs.getString("idkaryawan"),
                rs.getString("namakaryawan"),
                rs.getDate("tanggal"),
                rs.getTime("jammasuk"),
                rs.getTime("jamistirahat"),
                rs.getTime("jamkembali"),
                rs.getTime("jampulang"),
                // hadir & keterangan sebenarnya duplikat?
                // tapi sesuaikan urutan kolommu
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

        // 3) Update UI
        lblHalaman.setText("Halaman " + currentPage + " / " + totalPage);
        lblTotal.setText("Total: " + totalRows);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPage);

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error load data: " + ex.getMessage());
    }

    // 4) Apply view mode (sembunyi/ tampil kolom sesuai pilihan)
    applyViewMode();
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
        SwingUtilities.invokeLater(() -> new laporan().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
