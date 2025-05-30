package matkuldesktop;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;



/**
 *
 * @author Dzikr
 */
public class absen2 extends javax.swing.JFrame {
    private JTextField tfIdKaryawan;
    private JTextField tfNama;
    private JLabel lblWaktuTanggal;
    private JButton btnMasuk, btnIstirahat, btnPulang;
    private JTable tableAbsensi;
    private DefaultTableModel tableModel;
    private JButton btnScan;
    private LocalDateTime masukTime;
    private LocalDateTime istirahatStart;
    private int currentShift;
    private int currentKeterangan;

    public absen2() {
        initComponents();
        Koneksi.Getkoneksi();
        
        setTitle("Aplikasi Absensi");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        buildUI();

        // Inisialisasi event handlers
        initEventHandlers();

        // Mulai jam & load data
        startClock();
        refreshTable();

    }
    
    private void initEventHandlers() {
        tfIdKaryawan.addActionListener(e -> lookupKaryawan());
        btnMasuk.addActionListener(e -> doMasuk());
        btnIstirahat.addActionListener(e -> doIstirahat());
        btnPulang.addActionListener(e -> doPulang());        
        btnScan.addActionListener(e -> startBarcodeScanner());

     
    }
    
    private void startBarcodeScanner() {
    try {
        // Panggil metode statis mulaiScan() dari kelas barcodeScanner
        barcodeScanner.mulaiScan();
    } catch (Exception e) {
        // Tangani kemungkinan kesalahan (misal kamera tidak ditemukan)
        e.printStackTrace();
    }
}


    private void buildUI() {
        tfIdKaryawan = new JTextField(10);
        tfNama = new JTextField(20);
        tfNama.setEditable(false);
        lblWaktuTanggal = new JLabel();
        lblWaktuTanggal.setFont(lblWaktuTanggal.getFont().deriveFont(16f));
        btnMasuk = new JButton("Masuk");
        btnIstirahat = new JButton("Istirahat");
        btnPulang = new JButton("Pulang");
        btnScan = new JButton("Scan");  
       

        tableModel = new DefaultTableModel(
            new String[]{"ID","Nama","Tanggal","Masuk","Istirahat","Kembali","Pulang","Shift","Ket"}, 0
        );
        tableAbsensi = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(tableAbsensi);

        JPanel panelTop = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.gridx = 0; c.gridy = 0;
        panelTop.add(new JLabel("Masukan ID:"), c);
        c.gridx = 1;
        panelTop.add(tfIdKaryawan, c);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
        panelTop.add(tfNama, c);
        c.gridy = 2;
        panelTop.add(lblWaktuTanggal, c);
        
        

        JPanel panelBtn = new JPanel();
        panelBtn.add(btnMasuk);
        panelBtn.add(btnIstirahat);
        panelBtn.add(btnPulang);
        panelBtn.add(btnScan);
        
        setLayout(new BorderLayout());
        add(panelTop, BorderLayout.NORTH);
        add(panelBtn, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
        // Pada inisialisasi GUI di absen2.java
        JButton btnScan = new JButton("Scan");
        btnScan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startBarcodeScanner();
            }
        });
       
        
    }

   
    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            String fmt = now.format(
                DateTimeFormatter.ofPattern("HH.mm EEEE dd MMMM yyyy", new Locale("id"))
            );
            lblWaktuTanggal.setText(fmt);
        });
        timer.start();
    }
    private void lookupKaryawan() {
        String id = tfIdKaryawan.getText().trim();
        if (id.isEmpty()) return;
        try {
            String sql = "SELECT namakaryawan FROM tkaryawan WHERE idkaryawan = ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfNama.setText(rs.getString("namakaryawan"));
            } else {
                tfNama.setText("ID tidak ditemukan");
                return;
            }
            LocalTime now = LocalTime.now();
            currentShift = now.isBefore(LocalTime.of(10,0)) ? 1
                         : now.isBefore(LocalTime.of(16,0)) ? 2 : 3;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void doMasuk() {
        String id = tfIdKaryawan.getText().trim();
        LocalTime now = LocalTime.now();
        masukTime = LocalDateTime.now();
        int ket = 1;
       LocalTime shiftStart;
        switch (currentShift) {
            case 1:
                shiftStart = LocalTime.of(8, 0);
                break;
            case 2:
                shiftStart = LocalTime.of(12, 0);
                break;
            default:
                shiftStart = LocalTime.of(17, 0);
                break;
        }
        if (now.isAfter(shiftStart)) {
            ket = 2;
            JOptionPane.showMessageDialog(this, "Anda terlambat!");
        }
        currentKeterangan = ket;
        try {
            String sql = "INSERT INTO tabsensi (idkaryawan, tanggal, jammasuk, idshift, id_keterangan) " +
                         "VALUES (?, CURDATE(), ?, ?, ?)";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, id);
           ps.setTime(2, java.sql.Time.valueOf(now));
            ps.setInt(3, currentShift);
            ps.setInt(4, currentKeterangan);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        refreshTable();
    }
    private void doIstirahat() {
    String id = tfIdKaryawan.getText().trim();
    if (id.isEmpty()) {
        JOptionPane.showMessageDialog(this, "ID karyawan belum diisi.");
        return;
    }

    LocalDateTime now = LocalDateTime.now();
    try {
        if (istirahatStart == null) {
            // Mulai istirahat: catat jamistirahat + pertahankan keterangan
            istirahatStart = now;
            String sql = "UPDATE tabsensi "
                       + "SET jamistirahat = ?, id_keterangan = ? "
                       + "WHERE idkaryawan = ? "
                       +   "AND tanggal = CURDATE() "
                       +   "AND jamistirahat IS NULL";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setTime(1, Time.valueOf(now.toLocalTime()));
            ps.setInt(2, currentKeterangan);       // bawa status awal (1=tepat,2=terlambat)
            ps.setString(3, id);
            int row = ps.executeUpdate();
            if (row > 0) {
                btnIstirahat.setText("Kembali");
                JOptionPane.showMessageDialog(this, 
                    "Mulai istirahat: " + now.toLocalTime());
            }
        } else {
            // Selesai istirahat: hitung durasi & update jamkembali (ubah keterangan 
            // hanya kalau istirahat >60 menit dan sebelumnya tidak terlambat)
            long menit = Duration.between(istirahatStart, now).toMinutes();
            int newKet = currentKeterangan;
            if (menit > 60 && currentKeterangan == 1) {
                newKet = 3; // istirahat terlalu lama
            }
            String sql = "UPDATE tabsensi "
                       + "SET jamkembali = ?, id_keterangan = ? "
                       + "WHERE idkaryawan = ? "
                       +   "AND tanggal = CURDATE() "
                       +   "AND jamkembali IS NULL";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setTime(1, Time.valueOf(now.toLocalTime()));
            ps.setInt(2, newKet);
            ps.setString(3, id);
            int row = ps.executeUpdate();
            if (row > 0) {
                btnIstirahat.setText("Istirahat");
                JOptionPane.showMessageDialog(this, 
                  "Selesai istirahat (" + menit + " menit).");
            }
            istirahatStart = null;
            currentKeterangan = newKet;
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, 
          "Error database: " + ex.getMessage());
    }
    refreshTable();
}
    private void doPulang() {
    String id = tfIdKaryawan.getText().trim();
    if (id.isEmpty()) {
        JOptionPane.showMessageDialog(this, "ID karyawan belum diisi.");
        return;
    }

    try {
        // 1) Ambil jammasuk dari DB
        String q = "SELECT jammasuk FROM tabsensi "
                 + "WHERE idkaryawan = ? AND tanggal = CURDATE() AND jampulang IS NULL";
        PreparedStatement psel = Koneksi.con.prepareStatement(q);
        psel.setString(1, id);
        ResultSet rs = psel.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(this, "Belum absen masuk hari ini atau sudah pulang.");
            return;
        }
        Time sqlJamMasuk = rs.getTime("jammasuk");
        LocalDateTime masukDT = LocalDateTime.of(LocalDate.now(), sqlJamMasuk.toLocalTime());

        // 2) Hitung jam kerja
        LocalDateTime now = LocalDateTime.now();
        long jamKerja = Duration.between(masukDT, now).toHours();
        int ket = currentKeterangan;
        if (jamKerja > 8 && ket == 1) {
            ket = 4; // lembur
            JOptionPane.showMessageDialog(this, "Anda lembur! Total jam kerja: " + jamKerja + " jam.");
        }

        // 3) Update jampulang
        String upd = "UPDATE tabsensi SET jampulang = ?, id_keterangan = ? "
                   + "WHERE idkaryawan = ? AND tanggal = CURDATE() AND jampulang IS NULL";
        PreparedStatement ps = Koneksi.con.prepareStatement(upd);
        ps.setTime(1, Time.valueOf(now.toLocalTime()));
        ps.setInt(2, ket);
        ps.setString(3, id);
        int row = ps.executeUpdate();
        if (row > 0) {
            JOptionPane.showMessageDialog(this, "Jam pulang dicatat: " + now.toLocalTime());
            btnPulang.setEnabled(false);  // disable biar gak diklik lagi
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan jam pulang atau sudah dicatat.");
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Kesalahan database: " + ex.getMessage());
    }

    refreshTable();
}

 private void refreshTable() {
    tableModel.setRowCount(0);
    String sql =
        "SELECT t.idkaryawan, k.namakaryawan, t.tanggal, t.jammasuk, " +
        "t.jamistirahat, t.jamkembali, t.jampulang, " +
        "s.namashift, c.keterangan " +
        "FROM tabsensi t " +
        "JOIN tkaryawan k ON k.idkaryawan = t.idkaryawan " +
        "JOIN tshift s ON s.idshift = t.idshift " +
        "JOIN tketerangan c ON c.id_keterangan = t.id_keterangan " +
        // ubah di sini: tambahkan DESC pada jammasuk
        "ORDER BY t.tanggal DESC, t.jammasuk DESC";
    try (ResultSet rs = Koneksi.stm.executeQuery(sql)) {
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                rs.getString("idkaryawan"), rs.getString("namakaryawan"),
                rs.getDate("tanggal"), rs.getTime("jammasuk"),
                rs.getTime("jamistirahat"), rs.getTime("jamkembali"),
                rs.getTime("jampulang"), rs.getString("namashift"),
                rs.getString("keterangan")
            });
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 747, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 699, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
       SwingUtilities.invokeLater(() -> new absen2().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
