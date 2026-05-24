package tiketkeretaapi;

import view.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class TiketKeretaApi {
    
    public static void main(String[] args) {
        
        // Mengubah "Look and Feel" GUI agar mengikuti tema bawaan Sistem Operasi
        // sehingga desain tombol dan form terlihat lebih modern (tidak terlihat jadul).
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            System.out.println("Gagal memuat tema sistem. Menggunakan tema default Java.");
        }

        /* 
         * Menjalankan GUI di dalam Event Dispatch Thread (EDT).
         * Ini adalah *best practice* (standar profesional) dalam Java Swing 
         * untuk mencegah terjadinya lag atau freeze pada antarmuka aplikasi.
         */
        SwingUtilities.invokeLater(() -> {
            
            // Instansiasi dan menampilkan layar Login sebagai pintu masuk sistem
            LoginFrame frameLogin = new LoginFrame();
            frameLogin.setVisible(true);
            
        });
    }
}