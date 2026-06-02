package tiketkeretaapi;

import view.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class TiketKeretaApi {
    
    public static void main(String[] args) {
        //untuk mencegah terjadinya lag atau freeze pada antarmuka aplikasi.
        SwingUtilities.invokeLater(() -> {
            
            // Instansiasi dan menampilkan layar Login sebagai pintu masuk sistem
            LoginFrame frameLogin = new LoginFrame();
            frameLogin.setVisible(true);
            
        });
    }
}