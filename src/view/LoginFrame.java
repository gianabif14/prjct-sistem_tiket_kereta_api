package view;

import model.entitas.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import model.entitas.UserDAO;

public class LoginFrame extends JFrame {
    
    // Deklarasi komponen UI
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    
    // Deklarasi DAO untuk akses data login
    private final UserDAO authDAO;

    public LoginFrame() {
        authDAO = new UserDAO();
        setupUI();
    }

    private void setupUI() {
        // Konfigurasi dasar Frame
        setTitle("Login - Sistem Enterprise KAI");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Agar window muncul tepat di tengah layar
        setLayout(new BorderLayout());

        // --- PANEL HEADER ---
        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(new Color(41, 128, 185)); // Warna biru khas korporat
        JLabel lblJudul = new JLabel("Silakan Login");
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 18));
        panelHeader.add(lblJudul);

        // --- PANEL FORM ---
        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 15));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panelForm.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panelForm.add(txtUsername);

        panelForm.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panelForm.add(txtPassword);

        // --- PANEL TOMBOL ---
        JPanel panelButton = new JPanel();
        panelButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        btnLogin = new JButton("Login Masuk");
        btnLogin.setBackground(new Color(39, 174, 96)); // Warna hijau untuk tombol aksi
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelButton.add(btnLogin);

        // --- EVENT LISTENER ---
        // Menangkap aksi klik pada tombol login
        btnLogin.addActionListener(this::prosesLogin);

        // --- PENYUSUNAN KE FRAME ---
        add(panelHeader, BorderLayout.NORTH);
        add(panelForm, BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);
        
        // Memungkinkan pengguna menekan tombol "Enter" di keyboard untuk login
        getRootPane().setDefaultButton(btnLogin);
    }

    /**
     * Logika utama untuk memproses login.
     * Mengambil data dari form, memanggil DAO, dan menerapkan Polimorfisme Frame.
     */
    private void prosesLogin(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        // Validasi input kosong (Mencegah NullPointerException atau error query)
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username dan Password tidak boleh kosong!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Memanggil AuthDAO untuk mencocokkan data dengan database
            User user = authDAO.login(username, password);

            if (user != null) {
                JOptionPane.showMessageDialog(this, 
                    "Selamat datang, " + user.getUsername() + "!\nLogin sebagai: " + user.getRole(), 
                    "Login Berhasil", JOptionPane.INFORMATION_MESSAGE);
                
                // Menutup (destroy) window Login
                this.dispose(); 

                // Menerapkan konsep OOP: Arahkan ke Dashboard yang sesuai berdasarkan Role
                if ("ADMIN".equals(user.getRole())) {
                    new AdminFrame().setVisible(true);
                } else {
                    // Jika penumpang, kirimkan ID-nya ke MainFrame untuk pencatatan transaksi
                    new MainFrame(user.getId()).setVisible(true); 
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Username atau Password salah!", 
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Terjadi kesalahan koneksi database:\n" + ex.getMessage(), 
                "Error Sistem", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Cetak log error ke console untuk debugging
        }
    }
}