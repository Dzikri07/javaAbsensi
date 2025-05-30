package matkuldesktop;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class absen3 extends javax.swing.JFrame {
    private JTextField tfIdKaryawan;
    private JTextField tfNama;
    private JLabel lblWaktuTanggal;
    private JTable tableAbsensi;
    private DefaultTableModel tableModel;
    private LocalDateTime istirahatStart;
    private int currentShift;
    private int currentKeterangan;
    
    public absen3() {
        initComponents();
        Koneksi.Getkoneksi();

        setTitle("Pt Yihong ");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        buildUI();
        initEventHandlers();
        startClock();
        refreshTable();
    }
     private void buildUI() {
        tfIdKaryawan = new JTextField(15);
        tfNama = new JTextField(30);
        tfNama.setEditable(false);
        lblWaktuTanggal = new JLabel("", SwingConstants.CENTER);
        lblWaktuTanggal.setFont(new Font("Segoe UI", Font.BOLD, 16));


        tfIdKaryawan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfNama.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        tableModel = new DefaultTableModel(
            new String[]{"ID","Nama","Tanggal","Masuk","Istirahat","Kembali","Pulang","Shift","Ket"}, 0
        );
        tableAbsensi = new JTable(tableModel);
        tableAbsensi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableAbsensi.setRowHeight(24);
        tableAbsensi.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableAbsensi.getTableHeader().setBackground(Color.LIGHT_GRAY);
        JScrollPane scroll = new JScrollPane(tableAbsensi);

        // Warna baris selang-seling
        tableAbsensi.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 248, 255));
                }
                return c;
            }
        });

        JPanel panelTop = new JPanel(new GridBagLayout());
        panelTop.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Form Absensi",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            Color.DARK_GRAY
        ));
        panelTop.setBackground(new Color(250, 250, 250));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        panelTop.add(new JLabel("Masukkan ID Karyawan:"), c);
        c.gridx = 1;
        panelTop.add(tfIdKaryawan, c);

        c.gridx = 0; c.gridy = 1;
        panelTop.add(new JLabel("Nama Karyawan:"), c);
        c.gridx = 1;
        panelTop.add(tfNama, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        panelTop.add(lblWaktuTanggal, c);

        setLayout(new BorderLayout(10, 10));
        add(panelTop, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void initEventHandlers() {
        tfIdKaryawan.addActionListener(e -> handleAutoAbsen());
    }

    private void handleAutoAbsen() {
        String id = tfIdKaryawan.getText().trim();
        if (id.isEmpty()) return;
        if (!lookupKaryawan(id)) return;

        try {
            int state = getCurrentAbsenState(id);
            switch (state) {
                case 0: doMasuk(id); break;
                case 1: doIstirahat(id); break;
                case 2: doIstirahat(id); break;
                case 3: doPulang(id); break;
                default:
                    JOptionPane.showMessageDialog(this, "Absensi hari ini sudah selesai.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        tfIdKaryawan.setText("");
        refreshTable();
    }

    private boolean lookupKaryawan(String id) {
        try {
            String sql = "SELECT namakaryawan FROM tkaryawan WHERE idkaryawan = ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfNama.setText(rs.getString("namakaryawan"));
            } else {
                tfNama.setText("ID tidak ditemukan");
                return false;
            }
            LocalTime now = LocalTime.now();
            currentShift = now.isBefore(LocalTime.of(10, 0)) ? 1
                         : now.isBefore(LocalTime.of(16, 0)) ? 2 : 3;
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private int getCurrentAbsenState(String id) throws SQLException {
        String sql = "SELECT jamistirahat, jamkembali, jampulang FROM tabsensi " +
                     "WHERE idkaryawan = ? AND tanggal = CURDATE()";
        PreparedStatement ps = Koneksi.con.prepareStatement(sql);
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            return 0;
        }
        Time jamIst = rs.getTime("jamistirahat");
        Time jamKmb = rs.getTime("jamkembali");
        Time jamPlg = rs.getTime("jampulang");
        if (jamIst == null) return 1;
        if (jamKmb == null) return 2;
        if (jamPlg == null) return 3;
        return 4;
    }

    private void doMasuk(String id) {
        LocalTime now = LocalTime.now();
        int ket = 1;
        LocalTime shiftStart;
        switch (currentShift) {
            case 1: shiftStart = LocalTime.of(8, 0); break;
            case 2: shiftStart = LocalTime.of(12, 0); break;
            default: shiftStart = LocalTime.of(17, 0); break;
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
            ps.setTime(2, Time.valueOf(now));
            ps.setInt(3, currentShift);
            ps.setInt(4, currentKeterangan);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Absen masuk tercatat: " + now);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void doIstirahat(String id) {
        LocalDateTime now = LocalDateTime.now();
        try {
            if (istirahatStart == null) {
                istirahatStart = now;
                String sql = "UPDATE tabsensi SET jamistirahat = ? WHERE idkaryawan = ? AND tanggal = CURDATE() AND jamistirahat IS NULL";
                PreparedStatement ps = Koneksi.con.prepareStatement(sql);
                ps.setTime(1, Time.valueOf(now.toLocalTime()));
                ps.setString(2, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Mulai istirahat: " + now.toLocalTime());
            } else {
                long menit = Duration.between(istirahatStart, now).toMinutes();
                int newKet = currentKeterangan;
                if (menit > 60 && currentKeterangan == 1) newKet = 3;
                String sql = "UPDATE tabsensi SET jamkembali = ?, id_keterangan = ? WHERE idkaryawan = ? AND tanggal = CURDATE() AND jamkembali IS NULL";
                PreparedStatement ps = Koneksi.con.prepareStatement(sql);
                ps.setTime(1, Time.valueOf(now.toLocalTime()));
                ps.setInt(2, newKet);
                ps.setString(3, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Selesai istirahat (" + menit + " menit)");
                istirahatStart = null;
                currentKeterangan = newKet;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error database: " + ex.getMessage());
        }
    }

    private void doPulang(String id) {
        try {
            String q = "SELECT jammasuk, id_keterangan FROM tabsensi WHERE idkaryawan = ? AND tanggal = CURDATE() AND jampulang IS NULL";
            PreparedStatement psSel = Koneksi.con.prepareStatement(q);
            psSel.setString(1, id);
            ResultSet rs = psSel.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Belum absen masuk hari ini atau sudah pulang.");
                return;
            }
            LocalDateTime masukDT = LocalDateTime.of(LocalDate.now(), rs.getTime("jammasuk").toLocalTime());
            int ket = rs.getInt("id_keterangan");
            LocalDateTime now = LocalDateTime.now();
            long jamKerja = Duration.between(masukDT, now).toHours();
            if (jamKerja > 8 && ket == 1) {
                ket = 4;
                JOptionPane.showMessageDialog(this, "Anda lembur! Total jam kerja: " + jamKerja + " jam.");
            }
            String upd = "UPDATE tabsensi SET jampulang = ?, id_keterangan = ? WHERE idkaryawan = ? AND tanggal = CURDATE() AND jampulang IS NULL";
            PreparedStatement ps = Koneksi.con.prepareStatement(upd);
            ps.setTime(1, Time.valueOf(now.toLocalTime()));
            ps.setInt(2, ket);
            ps.setString(3, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Absensi pulang tercatat: " + now.toLocalTime());
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Kesalahan database: " + ex.getMessage());
        }
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblWaktuTanggal.setText(now.format(DateTimeFormatter.ofPattern("HH.mm:ss | EEEE, dd MMMM yyyy", new Locale("id"))));
        });
        timer.start();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new absen3().setVisible(true));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
