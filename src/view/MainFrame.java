package view;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main entry point aplikasi Sistem Tiket Kereta Api.
 */
public class MainFrame {
    public static void main(String[] args) {
        // Gunakan Cross-Platform L&F (Metal) agar warna komponen konsisten di semua OS
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Jalankan di Event Dispatch Thread untuk keamanan Swing
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}