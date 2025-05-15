package matkuldesktop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
// Tambahkan import untuk ekspor:
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

public class laporan extends javax.swing.JFrame {
   
    private Connection conn;
    private JTable jtlaporan;
    private DefaultTableModel model;
    private JTextField tfCariIdAbsen, tfCariIdKaryawan, tfCariNama;
    private JComboBox<String> cbProdi, cbJabatan, cbShift, cbKeterangan;
    private JSpinner spinnerTanggalMulai, spinnerTanggalAkhir;
    private JButton btnPrev, btnNext, btnSearch, btnExportPDF, btnExportExcel;
    private JLabel lblHalaman, lblTotal;
    private int currentPage = 1, totalPage = 1, rowsPerPage = 30;
    
    
    private PreparedStatement pst;
    private ResultSet rs;

    private Statement stat;       

    public laporan() {
        setTitle("Laporan Absensi");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Koneksi DB
        connect();

        // Inisialisasi panel filter atas
        JPanel panelFilter = new JPanel(new GridLayout(2, 1));
        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCari.add(new JLabel("ID Absen:"));
        tfCariIdAbsen = new JTextField(5); panelCari.add(tfCariIdAbsen);
        panelCari.add(new JLabel("ID Karyawan:"));
        tfCariIdKaryawan = new JTextField(5); panelCari.add(tfCariIdKaryawan);
        panelCari.add(new JLabel("Nama:"));
        tfCariNama = new JTextField(10); panelCari.add(tfCariNama);
        btnSearch = new JButton("Search");
        panelCari.add(btnSearch);

        JPanel panelDropdown = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDropdown.add(new JLabel("Prodi:"));
        cbProdi = new JComboBox<>(); panelDropdown.add(cbProdi);
        panelDropdown.add(new JLabel("Jabatan:"));
        cbJabatan = new JComboBox<>(); panelDropdown.add(cbJabatan);
        panelDropdown.add(new JLabel("Shift:"));
        cbShift = new JComboBox<>(); panelDropdown.add(cbShift);
        panelDropdown.add(new JLabel("Keterangan:"));
        cbKeterangan = new JComboBox<>(); panelDropdown.add(cbKeterangan);
        panelDropdown.add(new JLabel("Tanggal Mulai:"));
        spinnerTanggalMulai = new JSpinner(new SpinnerDateModel());
        spinnerTanggalMulai.setEditor(new JSpinner.DateEditor(spinnerTanggalMulai, "yyyy-MM-dd"));
        panelDropdown.add(spinnerTanggalMulai);
        panelDropdown.add(new JLabel("Sampai:"));
        spinnerTanggalAkhir = new JSpinner(new SpinnerDateModel());
        spinnerTanggalAkhir.setEditor(new JSpinner.DateEditor(spinnerTanggalAkhir, "yyyy-MM-dd"));
        panelDropdown.add(spinnerTanggalAkhir);

        panelFilter.add(panelCari);
        panelFilter.add(panelDropdown);
        add(panelFilter, BorderLayout.NORTH);

        // Setup tabel
        model = new DefaultTableModel(new String[]{
            "ID Absen","ID Karyawan","Nama","Tanggal",
            "Jam Masuk","Jam Istirahat","Jam Kembali","Jam Pulang",
            "Hadir","Terlambat","Terlambat Kembali","Lembur",
            "Prodi","Jabatan","Shift","Keterangan"
        }, 0);
        jtlaporan = new JTable(model);
        jtlaporan.setAutoCreateRowSorter(true); // Sorting/filtering aktif:contentReference[oaicite:9]{index=9}
        JScrollPane scroll = new JScrollPane(jtlaporan);
        add(scroll, BorderLayout.CENTER);

        // Panel bawah (paging & ekspor)
        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

        // Muat opsi filter dari database
        loadFilterOptions();

        // Event handling
        btnSearch.addActionListener(e -> loadData(1));
        btnPrev.addActionListener(e -> loadData(currentPage - 1));
        btnNext.addActionListener(e -> loadData(currentPage + 1));
        btnExportExcel.addActionListener(e -> exportDialog("Excel"));
        btnExportPDF.addActionListener(e -> exportDialog("PDF"));

        setVisible(true);
        // Muat data awal halaman 1
        loadData(1);
    }
    
     private void connect(){//begin
        try {
               Class.forName("com.mysql.jdbc.Driver");
               conn=DriverManager.getConnection("jdbc:mysql://localhost/db_absensi", "root", "");
               stat=conn.createStatement();
             } catch (ClassNotFoundException | SQLException e) {
               JOptionPane.showMessageDialog(null, e);
             }
      }//end begin
    
    private void loadFilterOptions() {
        try {
            Statement st = conn.createStatement();
            // Prodi
            cbProdi.addItem("All");
            ResultSet rs = st.executeQuery("SELECT idprodi, prodi FROM tprodi");
            while (rs.next()) {
                cbProdi.addItem(rs.getString("idprodi") + " - " + rs.getString("prodi"));
            }
            // Jabatan
            cbJabatan.addItem("All");
            rs = st.executeQuery("SELECT idjabatan, jabatan FROM tjabatan");
            while (rs.next()) {
                cbJabatan.addItem(rs.getString("idjabatan") + " - " + rs.getString("jabatan"));
            }
            // Shift
            cbShift.addItem("All");
            rs = st.executeQuery("SELECT idshift, namashift FROM tshift");
            while (rs.next()) {
                cbShift.addItem(rs.getString("idshift") + " - " + rs.getString("namashift"));
            }
            // Keterangan
            cbKeterangan.addItem("All");
            rs = st.executeQuery("SELECT DISTINCT keterangan FROM tketerangan");
            while (rs.next()) {
                cbKeterangan.addItem(rs.getString("keterangan"));
            }
            rs.close(); st.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal muat filter: " + ex.getMessage());
        }
    }
    
    private void loadData(int halaman) {
        model.setRowCount(0); // kosongkan tabel
        try {
            String where = " WHERE 1=1";
            // ... (filter building remains unchanged) ...

            // Hitung total baris
            String sqlCount = "SELECT COUNT(*) FROM tabsensi " +
                    "JOIN tkaryawan ON tabsensi.idkaryawan = tkaryawan.idkaryawan " +
                    "JOIN tprodi ON tkaryawan.idprodi = tprodi.idprodi " +
                    "JOIN tjabatan ON tkaryawan.idjabatan = tjabatan.idjabatan " +
                    "JOIN tshift ON tkaryawan.idshift = tshift.idshift " +
                    "JOIN tketerangan ON tabsensi.id_keterangan = tketerangan.id_keterangan" +
                    where;
            Statement stCount = conn.createStatement();
            ResultSet rsCount = stCount.executeQuery(sqlCount);
            int totalRows = 0;
            if (rsCount.next()) totalRows = rsCount.getInt(1);
            rsCount.close(); stCount.close();

            // Jika tidak ada data, tampilkan pesan dan keluar
            if (totalRows == 0) {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan.");
                // Reset paging ke 0/0
                lblHalaman.setText("Halaman 0/0");
                lblTotal.setText("Total: 0");
                btnPrev.setEnabled(false);
                btnNext.setEnabled(false);
                return;
            }

            // Hitung totalPage minimal 1
            totalPage = (int) Math.ceil((double) totalRows / rowsPerPage);
            if (totalPage < 1) totalPage = 1;
            currentPage = Math.min(Math.max(1, halaman), totalPage);

            // Query data halaman
            int offset = (currentPage - 1) * rowsPerPage;
            String sqlData = "SELECT tabsensi.idabsen, tabsensi.idkaryawan, tkaryawan.namakaryawan, " +
                    "tabsensi.tanggal, tabsensi.jammasuk, tabsensi.jamistirahat, " +
                    "tabsensi.jamkembali, tabsensi.jampulang, " +
                    "tkaryawan.hadir, tkaryawan.terlambat, tkaryawan.terlambat_kembali, tkaryawan.lembur, " +
                    "tprodi.prodi, tjabatan.jabatan, tshift.namashift, tketerangan.keterangan " +
                    "FROM tabsensi " +
                    "JOIN tkaryawan ON tabsensi.idkaryawan = tkaryawan.idkaryawan " +
                    "JOIN tprodi ON tkaryawan.idprodi = tprodi.idprodi " +
                    "JOIN tjabatan ON tkaryawan.idjabatan = tjabatan.idjabatan " +
                    "JOIN tshift ON tkaryawan.idshift = tshift.idshift " +
                    "JOIN tketerangan ON tabsensi.id_keterangan = tketerangan.id_keterangan " +
                    where +
                    " ORDER BY tabsensi.tanggal DESC, tabsensi.jammasuk DESC " +
                    " LIMIT " + rowsPerPage + " OFFSET " + offset;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sqlData);
            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getString("idabsen"),
                    rs.getString("idkaryawan"),
                    rs.getString("namakaryawan"),
                    rs.getString("tanggal"),
                    rs.getString("jammasuk"),
                    rs.getString("jamistirahat"),
                    rs.getString("jamkembali"),
                    rs.getString("jampulang"),
                    rs.getInt("hadir"),
                    rs.getInt("terlambat"),
                    rs.getInt("terlambat_kembali"),
                    rs.getInt("lembur"),
                    rs.getString("prodi"),
                    rs.getString("jabatan"),
                    rs.getString("namashift"),
                    rs.getString("keterangan")
                });
            }
            rs.close(); st.close();

            // Update label dan tombol
            lblHalaman.setText("Halaman " + currentPage + "/" + totalPage);
            lblTotal.setText("Total: " + totalRows);
            btnPrev.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPage);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error load data: " + ex.getMessage());
        }
    }
// Dialog pilihan ekspor
private void exportDialog(String type) {
    String[] options = {"Halaman ini saja", "Semua halaman"};
    int choice = JOptionPane.showOptionDialog(this, 
        "Pilih opsi ekspor " + type + ":", type + " Options",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, options, options[0]);
    if (choice == 0) {
        if (type.equals("Excel")) exportToExcel(currentPage, currentPage);
        else exportToPDF(currentPage, currentPage);
    } else if (choice == 1) {
        if (type.equals("Excel")) exportToExcel(1, totalPage);
        else exportToPDF(1, totalPage);
    }
}

// Contoh stub: ekspor ke Excel (gunakan Apache POI misalnya)
private void exportToExcel(int startPage, int endPage) {
    // Buat workbook dan sheet baru per halaman 30 baris
    // (Library Apache POI diperlukan)
    try {
        Workbook workbook = new XSSFWorkbook();
        int sheetNo = 1;
        for (int pg = startPage; pg <= endPage; pg++) {
            Sheet sheet = workbook.createSheet("Laporan_" + pg);
            // Header kolom di sheet
            Row header = sheet.createRow(0);
            for (int col=0; col<model.getColumnCount(); col++) {
                Cell cell = header.createCell(col);
                cell.setCellValue(model.getColumnName(col));
            }
            // Copy data baris per halaman
            String query = ""; // Sesuaikan dengan paging: page=pg
            // (Bisa memanggil kembali loadData untuk halaman ini lalu baca model, atau jalankan query lagi)
            // Tambahkan baris ke sheet...
        }
        // Simpan file
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            FileOutputStream out = new FileOutputStream(fileChooser.getSelectedFile() + ".xlsx");
            workbook.write(out);
            out.close();
            workbook.close();
            JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke Excel");
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error ekspor Excel: " + ex.getMessage());
    }
}

// Contoh stub: ekspor ke PDF (gunakan iText atau library PDF)
private void exportToPDF(int startPage, int endPage) {
    // Buat dokumen PDF, isi header, dan tabel per halaman
    // (Library iText atau JasperReports diperlukan)
    try {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("laporan.pdf"));
        document.open();
        // Tambahkan judul dan konten tabel
        // Pilih data berdasar paging: pg = startPage..endPage
        // [...]
        document.close();
        JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke PDF");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error ekspor PDF: " + ex.getMessage());
    }
    
}


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Laporan Absensi");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(178, 178, 178)
                .addComponent(jLabel1)
                .addContainerGap(181, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addContainerGap(613, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(laporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(laporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(laporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(laporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
               SwingUtilities.invokeLater(() -> new laporan());
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
