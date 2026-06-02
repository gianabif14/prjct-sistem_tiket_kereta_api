package view;

import model.entitas.User;
import model.entitas.Penumpang;
import controller.LoginController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * LoginFrame adalah tampilan awal untuk autentikasi user.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private final LoginController loginController;

    public LoginFrame() {
        loginController = new LoginController();
        setupUI();
    }

    private void setupUI() {
        setTitle("Login - Sistem Tiket Kereta Api");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        // === HEADER ===
        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setBackground(new Color(30, 80, 160));
        headerWrap.setBorder(BorderFactory.createEmptyBorder(16, 15, 16, 15));

        JLabel lblJudul = new JLabel("Sistem Tiket Kereta Api", SwingConstants.CENTER);
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel lblSub = new JLabel("Silakan masuk untuk melanjutkan", SwingConstants.CENTER);
        lblSub.setForeground(new Color(180, 210, 255));
        lblSub.setFont(new Font("Arial", Font.PLAIN, 12));

        headerWrap.add(lblJudul, BorderLayout.CENTER);
        headerWrap.add(lblSub, BorderLayout.SOUTH);

        // === FORM ===
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createEmptyBorder(25, 40, 10, 40));
        panelForm.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 0, 7, 0);

        JLabel lblUname = new JLabel("Username :");
        lblUname.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelForm.add(lblUname, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(200, 32));
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelForm.add(txtUsername, gbc);

        JLabel lblPass = new JLabel("Password :");
        lblPass.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panelForm.add(lblPass, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(200, 32));
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelForm.add(txtPassword, gbc);

        // === TOMBOL ===
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        panelButton.setBackground(Color.WHITE);

        btnLogin = new JButton("  LOGIN  ");
        btnLogin.setBackground(new Color(30, 80, 160));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setFocusPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        panelButton.add(btnLogin);

        // === EVENT ===
        btnLogin.addActionListener(this::prosesLogin);
        getRootPane().setDefaultButton(btnLogin);

        // === LAYOUT ===
        add(headerWrap, BorderLayout.NORTH);
        add(panelForm, BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);
    }

    private void prosesLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        try {
            User user = loginController.login(username, password);

            if (user != null) {
                this.dispose();
                if ("ADMIN".equals(user.getRole())) {
                    new AdminFrame(user).setVisible(true);
                } else {
                    new PenumpangFrame((Penumpang) user).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Username atau Password salah!",
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Peringatan", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Terjadi kesalahan koneksi database:\n" + ex.getMessage(),
                "Error Sistem", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}