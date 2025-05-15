package matkuldesktop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Dzikr
 */
public class absen extends javax.swing.JFrame {
    private JTextField tfID, tfName, tfProdi;
    private JComboBox<String> cbShift;
    private JButton btnMasuk, btnIstirahat, btnPulang;
    private JTable table;
    private DefaultTableModel tableModel;

    public absen() {
            initComponents(); // Terlambat!
            table = new JTable(tableModel);
     setTitle("Sistem Absensi");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel atas untuk input
        JPanel panelTop = new JPanel(new GridLayout(2, 4, 5, 5));
        panelTop.add(new JLabel("ID Karyawan:"));
        tfID = new JTextField();
        panelTop.add(tfID);
        panelTop.add(new JLabel("Nama Karyawan:"));
        tfName = new JTextField(); tfName.setEditable(false);
        panelTop.add(tfName);
        panelTop.add(new JLabel("Prodi:"));
        tfProdi = new JTextField(); tfProdi.setEditable(false);
        panelTop.add(tfProdi);
        panelTop.add(new JLabel("Shift:"));
        cbShift = new JComboBox<>(new String[]{"Pagi (1)", "Siang (2)", "Sore (3)"});
        panelTop.add(cbShift);

        // Inisialisasi tableModel sebelum JTable
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableModel.setColumnIdentifiers(new Object[]{
            "ID Absen","ID Karyawan","Nama Karyawan","Nama Shift","Prodi",
            "Tanggal","Jam Masuk","Jam Istirahat","Jam Kembali","Jam Pulang","Keterangan"
        });
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Panel tombol
        JPanel panelButtons = new JPanel(new FlowLayout());
        btnMasuk = new JButton("Masuk");
        btnIstirahat = new JButton("Istirahat");
        btnPulang = new JButton("Pulang");
        panelButtons.add(btnMasuk);
        panelButtons.add(btnIstirahat);
        panelButtons.add(btnPulang);

        setLayout(new BorderLayout());
        add(panelTop, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);

        loadTableData();

        // Event handlers
        tfID.addActionListener(e -> fetchNameAndProdi());
        btnMasuk.addActionListener(e -> insertMasuk());
        btnIstirahat.addActionListener(e -> updateIstirahat());
        btnPulang.addActionListener(e -> updatePulang());
    }
      private void fetchNameAndProdi() {
         String id = tfID.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID Karyawan tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection conn = Koneksi.Getkoneksi();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT k.namakaryawan, p.prodi FROM tkaryawan k " +
                 "JOIN tprodi p ON k.idprodi = p.idprodi " +
                 "WHERE k.idkaryawan = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfName.setText(rs.getString("namakaryawan"));
                tfProdi.setText(rs.getString("prodi"));
            } else {
                JOptionPane.showMessageDialog(this, "ID Karyawan tidak ditemukan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                tfName.setText(""); tfProdi.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
  private void insertMasuk() {
    String idKar = tfID.getText().trim();
    if (idKar.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Masukkan ID Karyawan terlebih dahulu!",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Hitung shift
    int shiftIndex = cbShift.getSelectedIndex();     // 0=Pagi,1=Siang,2=Sore
    int idShift    = shiftIndex + 1;

    // Hitung keterangan (Hadir/Terlambat)
    LocalDate today = LocalDate.now();
    LocalTime now   = LocalTime.now();
    int idKeterangan = now.isAfter(LocalTime.of(8, 0)) ? 2 : 1;
    String pesan     = (idKeterangan == 2)
        ? "Anda telat" 
        : "Absensi masuk sukses";

    // Buka koneksi sekali
    try (Connection conn = Koneksi.Getkoneksi()) {
        conn.setAutoCommit(false);  // rollback kalau error

        
        // 2) Insert record masuk di tabsensi
        String sqlIns = "INSERT INTO tabsensi(idkaryawan, tanggal, jammasuk, id_keterangan, idshift) "
                      + "VALUES (?,?,?,?,?)";
        try (PreparedStatement psIns = conn.prepareStatement(sqlIns)) {
            psIns.setString(1, idKar);
            psIns.setDate(2, Date.valueOf(today));
            psIns.setTime(3, Time.valueOf(now));
            psIns.setInt(4, idKeterangan);
              psIns.setInt(5, idShift);          // ← shift dipakai di tabsensi
            psIns.executeUpdate();
        }
        
        // … kode sebelum ini: psIns.executeUpdate();

        // update hitung hadir/terlambat
        String sqlUpd;
        if (idKeterangan == 1) {
            sqlUpd = "UPDATE tkaryawan SET hadir = hadir + 1 WHERE idkaryawan = ?";
        } else {
            sqlUpd = "UPDATE tkaryawan SET terlambat = terlambat + 1 WHERE idkaryawan = ?";
        }

        try (PreparedStatement psUpd = conn.prepareStatement(sqlUpd)) {
            psUpd.setString(1, idKar);
            psUpd.executeUpdate();
        }

        // commit kalau semua OK
        conn.commit();

        
        conn.commit();  // kalau semua oke, commit

        JOptionPane.showMessageDialog(this, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
        loadTableData();

    } catch (SQLException ex) {
        // kalau ada error, otomatis rollback karena try-with-resources + autoCommit(false)
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
    
}

private void updateIstirahat() {
    int row = table.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Pilih baris absen terlebih dahulu!",
            "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String idAbsen = tableModel.getValueAt(row, 0).toString();
    LocalTime now = LocalTime.now();

    try (Connection conn = Koneksi.Getkoneksi()) {
        conn.setAutoCommit(false);

        // 1) Ambil jam istirahat & jam kembali
        Time jamIst = null, jamKbl = null;
        try (PreparedStatement psSel = conn.prepareStatement(
                "SELECT jamistirahat, jamkembali, idkaryawan "
              + "FROM tabsensi WHERE idabsen = ?")) {
            psSel.setString(1, idAbsen);
            try (ResultSet rs = psSel.executeQuery()) {
                if (rs.next()) {
                    jamIst = rs.getTime("jamistirahat");
                    jamKbl = rs.getTime("jamkembali");
                } else {
                    throw new SQLException("Record absen tidak ditemukan!");
                }
            }
        }

        // 2) Logika simpan istirahat / kembali
        if (jamIst == null) {
            // a) Simpan jam istirahat pertama kali
            try (PreparedStatement psUp = conn.prepareStatement(
                    "UPDATE tabsensi SET jamistirahat = ? WHERE idabsen = ?")) {
                psUp.setTime(1, Time.valueOf(now));
                psUp.setString(2, idAbsen);
                psUp.executeUpdate();
            }
            JOptionPane.showMessageDialog(this,
                "Jam istirahat disimpan",
                "Informasi", JOptionPane.INFORMATION_MESSAGE);

        } else if (jamKbl == null) {
            // b) Kembali dari istirahat
            Duration dur = Duration.between(jamIst.toLocalTime(), now);
            boolean terlambatKembali = dur.toMinutes() > 60;

            // Update jamkembali ± id_keterangan
            String updTabsensi = terlambatKembali
                ? "UPDATE tabsensi SET jamkembali = ?, id_keterangan = 3 WHERE idabsen = ?"
                : "UPDATE tabsensi SET jamkembali = ? WHERE idabsen = ?";
            try (PreparedStatement psUp = conn.prepareStatement(updTabsensi)) {
                psUp.setTime(1, Time.valueOf(now));
                psUp.setString(2, idAbsen);
                psUp.executeUpdate();
            }

            // Jika terlambat kembali, tambah counter di tkaryawan
            if (terlambatKembali) {
                // ambil idkaryawan dari tabsensi
                String idKar;
                try (PreparedStatement psK = conn.prepareStatement(
                        "SELECT idkaryawan FROM tabsensi WHERE idabsen = ?")) {
                    psK.setString(1, idAbsen);
                    try (ResultSet rsK = psK.executeQuery()) {
                        rsK.next();
                        idKar = rsK.getString("idkaryawan");
                    }
                }
                // update tkaryawan.terlambat_kembali
                try (PreparedStatement psUpdKar = conn.prepareStatement(
                        "UPDATE tkaryawan SET terlambat_kembali = terlambat_kembali + 1 "
                      + "WHERE idkaryawan = ?")) {
                    psUpdKar.setString(1, idKar);
                    psUpdKar.executeUpdate();
                }
                JOptionPane.showMessageDialog(this,
                    "Jam kembali disimpan (terlambat kembali)",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Jam kembali disimpan",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            }

        } else {
            // c) Sudah lengkap
            JOptionPane.showMessageDialog(this,
                "Istirahat dan kembali sudah diisi",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
        }

        conn.commit();
        loadTableData();

    } catch (SQLException ex) {
        // Rollback otomatis oleh try-with-resources jika terjadi exception
        JOptionPane.showMessageDialog(this,
            "Error: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void updatePulang() {
    int row = table.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this,
            "Pilih baris absen terlebih dahulu!",
            "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String idAbsen = tableModel.getValueAt(row, 0).toString();
    LocalTime now = LocalTime.now();

    try (Connection conn = Koneksi.Getkoneksi()) {
        conn.setAutoCommit(false);

        // 1) Ambil jam masuk, istirahat, kembali, dan idkaryawan
        LocalTime jamMasuk, jamIstirahat = null, jamKembali = null;
        String idKar;
        String selSql = "SELECT idkaryawan, jammasuk, jamistirahat, jamkembali "
                      + "FROM tabsensi WHERE idabsen = ?";
        try (PreparedStatement psSel = conn.prepareStatement(selSql)) {
            psSel.setString(1, idAbsen);
            try (ResultSet rs = psSel.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Data absen tidak ditemukan!");
                }
                idKar      = rs.getString("idkaryawan");
                jamMasuk   = rs.getTime("jammasuk").toLocalTime();
                Time tIst  = rs.getTime("jamistirahat");
                Time tKbl  = rs.getTime("jamkembali");
                if (tIst != null) jamIstirahat = tIst.toLocalTime();
                if (tKbl != null) jamKembali   = tKbl.toLocalTime();
            }
        }

        // 2) Tentukan id_keterangan baru
        int idKeterangan;
        LocalTime batasMasuk = LocalTime.of(8, 0);
        if (jamMasuk.isAfter(batasMasuk)) {
            idKeterangan = 2;               // Terlambat masuk
        } else if (jamIstirahat != null
                && jamKembali != null
                && Duration.between(jamIstirahat, jamKembali).toMinutes() > 60) {
            idKeterangan = 3;               // Terlambat kembali
        } else if (Duration.between(jamMasuk, now).toHours() > 8) {
            idKeterangan = 4;               // Lembur
        } else {
            idKeterangan = 1;               // Hadir tepat waktu
        }

        // 3) Update jampulang + id_keterangan di tabsensi
        String upTabs = "UPDATE tabsensi "
                      + "SET jampulang = ?, id_keterangan = ? "
                      + "WHERE idabsen = ?";
        try (PreparedStatement psUp = conn.prepareStatement(upTabs)) {
            psUp.setTime(1, Time.valueOf(now));
            psUp.setInt(2, idKeterangan);
            psUp.setString(3, idAbsen);
            psUp.executeUpdate();
        }

        // 4) Update counter di tkaryawan sesuai id_keterangan
        String kolom;
        switch (idKeterangan) {
            case 2: kolom = "terlambat";          break;
            case 3: kolom = "terlambat_kembali";  break;
            case 4: kolom = "lembur";             break;
            default: kolom = "hadir";             break;
        }
        String upKar  = "UPDATE tkaryawan "
                      + "SET " + kolom + " = " + kolom + " + 1 "
                      + "WHERE idkaryawan = ?";
        try (PreparedStatement psUpdKar = conn.prepareStatement(upKar)) {
            psUpdKar.setString(1, idKar);
            psUpdKar.executeUpdate();
        }

        // 5) Commit & feedback
        conn.commit();
        JOptionPane.showMessageDialog(this,
            "Jam pulang & keterangan disimpan (" + kolom + ")",
            "Informasi", JOptionPane.INFORMATION_MESSAGE);

        loadTableData();

    } catch (SQLException ex) {
        // Jika ada error, rollback otomatis oleh try-with-resources
        JOptionPane.showMessageDialog(this,
            "Error: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void loadTableData() {
    tableModel.setRowCount(0);
    String sql = ""
        + "SELECT a.idabsen, a.idkaryawan, k.namakaryawan, s.namashift, p.prodi, "
        + "       a.tanggal, a.jammasuk, a.jamistirahat, a.jamkembali, a.jampulang, "
        + "       t.keterangan AS keterangan "
        + "FROM tabsensi a "
        + "  JOIN tkaryawan k   ON a.idkaryawan     = k.idkaryawan "
        + "  JOIN tshift s       ON a.idshift        = s.idshift "
        + "  JOIN tprodi p       ON k.idprodi        = p.idprodi "
        + "  JOIN tketerangan t  ON a.id_keterangan  = t.id_keterangan "
        + "ORDER BY a.tanggal DESC, a.jammasuk DESC";  // <— urut terbaru dulu

    try (Connection conn = Koneksi.Getkoneksi();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                rs.getInt("idabsen"),
                rs.getString("idkaryawan"),
                rs.getString("namakaryawan"),
                rs.getString("namashift"),
                rs.getString("prodi"),
                rs.getDate("tanggal"),
                rs.getTime("jammasuk"),
                rs.getTime("jamistirahat"),
                rs.getTime("jamkembali"),
                rs.getTime("jampulang"),
                rs.getString("keterangan")
            });
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error saat load data: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

 

    public static void main(String[] args) {
           SwingUtilities.invokeLater(() -> new absen().setVisible(true));
     }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        jScrollPane1.setViewportView(jEditorPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 527, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 711, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
