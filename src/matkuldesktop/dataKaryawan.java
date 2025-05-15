package matkuldesktop;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;
    import java.awt.event.KeyEvent;
    import static java.lang.System.err;
    import java.sql.Connection; 
    import java.sql.DriverManager; 
    import java.sql.Statement;
    import java.sql.ResultSet; 
    import java.sql.SQLException; 
    import javax.swing.JOptionPane; 
    import javax.swing.table.DefaultTableModel;
    import javax.swing.table.TableColumn;
    import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author Dzikr
 */
public class dataKaryawan extends javax.swing.JFrame {
      private boolean isEdit = false;
    private String editId   = "";

    // model utk JTable
    private DefaultTableModel tabelmodel;
    // di atas, bersama field lain:
    private List<Integer> jabatanIds = new ArrayList<>();
    private List<Integer> prodiIds   = new ArrayList<>();

    // koneksi
    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;

    private Statement stat;           // ← tambahkan ini

    public dataKaryawan() {
        super("Data Karyawan");
        initComponents();
        initCustom();
    }
    
    private void koneksi(){//begin
        try {
               Class.forName("com.mysql.jdbc.Driver");
               con=DriverManager.getConnection("jdbc:mysql://localhost/db_absensi", "root", "");
               stat=con.createStatement();
             } catch (ClassNotFoundException | SQLException e) {
               JOptionPane.showMessageDialog(null, e);
             }
      }//end begin

    private void initCustom() {
      koneksi();

        // 2. setup table
        tabelmodel = new DefaultTableModel(new String[]{"ID", "Nama", "Jabatan", "Prodi"}, 0);
        tkaryawan.setModel(tabelmodel);

        // 3. load combo & table
        loadComboJabatan();
        loadComboProdi();
        loadTable();

        // 4. action tombol
        simpan.addActionListener(e -> simpan( ));
        hapus.addActionListener(e -> hapus());
        refresh.addActionListener(e -> clearForm());
        
        // klik row utk isi form
        tkaryawan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = tkaryawan.getSelectedRow();
                if (r >= 0) {
                    idKarywan.setText(tabelmodel.getValueAt(r,0).toString());
                    namaKaryawan.setText(tabelmodel.getValueAt(r,1).toString());
                }
            }
        });
    }
    
    
 private void loadComboJabatan() {
    combojabatan.removeAllItems();
    jabatanIds.clear();
    try {
        pst = con.prepareStatement("SELECT idjabatan, jabatan FROM tjabatan");
        rs  = pst.executeQuery();
        while (rs.next()) {
            int id    = rs.getInt("idjabatan");
            String nm = rs.getString("jabatan");
            combojabatan.addItem(nm);
            jabatanIds.add(id);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Load jabatan gagal: " + ex.getMessage());
    }
}

private void loadComboProdi() {
    comboprodi.removeAllItems();
    prodiIds.clear();
    try {
        pst = con.prepareStatement("SELECT idprodi, prodi FROM tprodi");
        rs  = pst.executeQuery();
        while (rs.next()) {
            int id    = rs.getInt("idprodi");
            String nm = rs.getString("prodi");
            comboprodi.addItem(nm);
            prodiIds.add(id);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Load prodi gagal: " + ex.getMessage());
    }
}

    private void loadTable() {
        tabelmodel.setRowCount(0);
        String sql = ""
          + "SELECT k.idkaryawan, k.namakaryawan, j.jabatan, p.prodi "
          + "FROM tkaryawan k "
          + " LEFT JOIN tjabatan j ON k.idjabatan = j.idjabatan "
          + " LEFT JOIN tprodi   p ON k.idprodi   = p.idprodi";
        try {
            pst = con.prepareStatement(sql);
            rs  = pst.executeQuery();
            while (rs.next()) {
                tabelmodel.addRow(new Object[]{
                    rs.getString("idkaryawan"),
                    rs.getString("namakaryawan"),
                    rs.getString("jabatan"),
                    rs.getString("prodi")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Load table gagal: "+ex.getMessage());
        }
    }
private void clearForm() {
    idKarywan.setText("");
    namaKaryawan.setText("");
    combojabatan.setSelectedIndex(-1);
    comboprodi.setSelectedIndex(-1);
    isEdit = false;
    editId  = "";
    simpan.setText("Simpan");
}

  private void simpan() {
   String id   = idKarywan.getText().trim();
    String nama = namaKaryawan.getText().trim();
    int idxJ    = combojabatan.getSelectedIndex();
    int idxP    = comboprodi.getSelectedIndex();

    if (id.isEmpty() || nama.isEmpty() || idxJ < 0 || idxP < 0) {
        JOptionPane.showMessageDialog(this, "ID, Nama, Jabatan & Prodi wajib diisi");
        return;
    }

    int idjabatan = jabatanIds.get(idxJ);
    int idprodi   = prodiIds.get(idxP);

    // 1. Build SQL string
    String sql;
    if (isEdit) {
        sql = "UPDATE tkaryawan SET namakaryawan=?, idjabatan=?, idprodi=? WHERE idkaryawan=?";
    } else {
        sql = "INSERT INTO tkaryawan(idkaryawan, namakaryawan, idjabatan, idprodi) VALUES(?,?,?,?)";
    }

    // 2. Debug di luar SQL
    System.out.printf("DEBUG: isEdit=%b, editId=%s, SQL=%s%n", isEdit, editId, sql);

    try {
        // 3. Prepare statement & set params
        pst = con.prepareStatement(sql);
        if (isEdit) {
            pst.setString(1, nama);
            pst.setInt(2, idjabatan);
            pst.setInt(3, idprodi);
            pst.setString(4, editId);
        } else {
            pst.setString(1, id);
            pst.setString(2, nama);
            pst.setInt(3, idjabatan);
            pst.setInt(4, idprodi);
        }

        // 4. Execute and debug row count
        int affected = pst.executeUpdate();
        System.out.println("DEBUG: rows affected=" + affected);

        // 5. Show alert & refresh UI
        String msg = isEdit
            ? "Update sukses untuk ID=" + editId
            : "Insert sukses";
        JOptionPane.showMessageDialog(this, msg);
        loadTable();
        clearForm();

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            (isEdit ? "Error update: " : "Error simpan: ") + ex.getMessage());
    }
}
  

    private void hapus() {
        String id = idKarywan.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Yakin hapus ID="+id+"?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (ok==JOptionPane.YES_OPTION) {
            try {
                pst = con.prepareStatement("DELETE FROM tkaryawan WHERE idkaryawan=?");
                pst.setString(1, id);
                pst.executeUpdate();
                loadTable();
                clearForm();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error hapus: "+ex.getMessage());
            }
        }
    }

    // helper utk memilih combo berdasarkan nama
    private void selectComboItem(JComboBox<Item> combo, String name) {
        for (int i=0; i<combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getName().equals(name)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
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
        tkaryawan = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        idKarywan = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        comboprodi = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        namaKaryawan = new javax.swing.JTextField();
        simpan = new javax.swing.JButton();
        refresh = new javax.swing.JButton();
        hapus = new javax.swing.JButton();
        combojabatan = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tkaryawan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tkaryawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tkaryawanMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tkaryawan);

        jLabel1.setText("Nama Kaywan");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setText("DATA KARYAWAN");

        jLabel3.setText("Prodi");

        idKarywan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idKarywanActionPerformed(evt);
            }
        });

        jLabel4.setText("Jabatan");

        comboprodi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "teknik", "faster", "hukum", "febi" }));
        comboprodi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboprodiActionPerformed(evt);
            }
        });

        jLabel5.setText("Id Karywan");

        simpan.setText("simpan");

        refresh.setText("refresh");
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });

        hapus.setText("hapus");
        hapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hapusActionPerformed(evt);
            }
        });

        combojabatan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(98, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(namaKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addComponent(jLabel3))
                                    .addGap(67, 67, 67)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(comboprodi, 0, 92, Short.MAX_VALUE)
                                        .addComponent(combojabatan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(idKarywan, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(simpan)
                                .addGap(18, 18, 18)
                                .addComponent(hapus)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(refresh)))
                        .addGap(76, 76, 76))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(220, 220, 220))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(idKarywan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(namaKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(combojabatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboprodi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simpan)
                    .addComponent(refresh)
                    .addComponent(hapus))
                .addGap(45, 45, 45)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshActionPerformed

    private void hapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hapusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hapusActionPerformed

    private void idKarywanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idKarywanActionPerformed
            // TODO add your handling code here:
    }//GEN-LAST:event_idKarywanActionPerformed

    private void comboprodiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboprodiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboprodiActionPerformed

    private void tkaryawanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tkaryawanMouseClicked
       int row = tkaryawan.getSelectedRow();
    if (row < 0) return;

    String id        = tabelmodel.getValueAt(row, 0).toString();
    String nama      = tabelmodel.getValueAt(row, 1).toString();
    String namaJab   = tabelmodel.getValueAt(row, 2).toString();
    String namaProdi = tabelmodel.getValueAt(row, 3).toString();

    idKarywan.setText(id);
    namaKaryawan.setText(nama);
    combojabatan.setSelectedItem(namaJab);
    comboprodi.setSelectedItem(namaProdi);

    isEdit = true;
    editId = id;
    simpan.setText("Update");
    }//GEN-LAST:event_tkaryawanMouseClicked
// inner class utk combo
    
    
    private static class Item {
        private final int id;
        private final String name;
        public Item(int id, String name) {
            this.id   = id;
            this.name = name;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
          SwingUtilities.invokeLater(() -> new dataKaryawan().setVisible(true));
      
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> combojabatan;
    private javax.swing.JComboBox<String> comboprodi;
    private javax.swing.JButton hapus;
    private javax.swing.JTextField idKarywan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField namaKaryawan;
    private javax.swing.JButton refresh;
    private javax.swing.JButton simpan;
    private javax.swing.JTable tkaryawan;
    // End of variables declaration//GEN-END:variables
}
