package view;

import controller.BookingController;
import model.entitas.Jadwal;
import model.entitas.JadwalDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MainFrame extends JFrame {
    private JComboBox<String> comboJadwal;
    private JButton btnBayar;
    private List<Jadwal> listJadwal;
    
    private final BookingController controller;
    private final JadwalDAO jadwalDAO;
    private final int idUserAktif; 

    // Constructor Menerima ID dari LoginFrame
    public MainFrame(int idUserAktif) {
        this.idUserAktif = idUserAktif;
        this.controller = new BookingController();
        this.jadwalDAO = new JadwalDAO();
        
        setupUI();
        loadJadwalKeComboBox();
    }

    private void setupUI() {
        setTitle("Dashboard Penumpang - Pemesanan Tiket");
        setSize(550, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel lblHeader = new JLabel("Pemesanan Tiket Kereta Api");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(lblHeader);

        JPanel formPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Pilih Rute & Jadwal Keberangkatan:"));
        
        comboJadwal = new JComboBox<>();
        formPanel.add(comboJadwal);

        btnBayar = new JButton("Konfirmasi & Bayar via E-Wallet");
        btnBayar.setBackground(new Color(39, 174, 96));
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(btnBayar);

        btnBayar.addActionListener(e -> prosesPembayaran());

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void loadJadwalKeComboBox() {
        try {
            // Tarik data dinamis dari database menggunakan JadwalDAO
            listJadwal = jadwalDAO.getAllJadwal();
            for (Jadwal j : listJadwal) {
                // Tampilkan format ke ComboBox (ex: KA Taksaka - Eksekutif | Rp 450000)
                String item = j.getNamaKereta() + " - " + j.getKelas() + " | Rp " + j.getHargaDasar();
                comboJadwal.addItem(item);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat jadwal dari database!");
        }
    }

    private void prosesPembayaran() {
        int selectedIndex = comboJadwal.getSelectedIndex();
        if (selectedIndex < 0) return;

        // Dapatkan objek Jadwal berdasarkan index dropdown yang dipilih
        Jadwal jadwalTerpilih = listJadwal.get(selectedIndex);

        int idJadwal = jadwalTerpilih.getId();
        String kelas = jadwalTerpilih.getKelas();
        double hargaDasar = jadwalTerpilih.getHargaDasar();

        // Lempar proses bayar ke Controller (Sudah menerapkan ACID DB & Polymorphism)
        String hasil = controller.prosesTiket(idUserAktif, idJadwal, hargaDasar, kelas);
        
        String[] respon = hasil.split("\\|");
        if ("SUKSES".equals(respon[0])) {
            JOptionPane.showMessageDialog(this, respon[1], "Transaksi Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, respon[1], "Transaksi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }
}