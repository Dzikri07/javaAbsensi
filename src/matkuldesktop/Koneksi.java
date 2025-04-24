package matkuldesktop;

 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import javax.swing.JOptionPane;


public class Koneksi { 
    public static Connection con; 
    public static Statement stm; 
    
    public static Connection Getkoneksi()
     {
          try{
            String url ="jdbc:mysql://localhost/db_absensi";
            String user="root";
            String pass="";
            Class.forName("com.mysql.jdbc.Driver");
            con =DriverManager.getConnection(url,user,pass);
            stm = con.createStatement();
            System.out.println("koneksi berhasil;");
        }//batas try
        catch (ClassNotFoundException | SQLException e) 
        {
          System.err.println("koneksi gagal" +e.getMessage());
        }  
          return con;
     }
    
    public static void main(String[] args) {
    // TODO code application logic here
 Getkoneksi();
 }

} 