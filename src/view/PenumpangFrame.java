package view;

import controller.BookingController;
import model.entitas.*;
import model.layanan.TiketKeretaApiDAO;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PenumpangFrame extends JFrame {

    private final Penumpang penumpang;
    private final TiketKeretaApiDAO tiketDAO;
    private final JadwalDAO jadwalDAO;
    private final BookingController controller;
    private final RiwayatDAO riwayatDAO;
    private final UserDAO userDAO;
    private final TopupDAO topupDAO;

    // Header saldo label (global agar bisa direfresh)
    private JLabel lblSaldo;

    // Tab Pesan
    private JComboBox<String> comboAsal, comboTujuan;
    private JPanel panelCari, panelHasil;
    private JList<String> listKereta;
    private DefaultListModel<String> modelKereta;
    private List<Jadwal> hasilCari;

    // Tab Riwayat
    private DefaultTableModel modelRiwayat;

    // Tab Topup
    private JLabel lblJumlahTopup;
    private double jumlahTopup = 10000;
    private DefaultTableModel modelTopup;

    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public PenumpangFrame(Penumpang penumpang) {
        this.penumpang  = penumpang;
        this.tiketDAO   = new TiketKeretaApiDAO();
        this.jadwalDAO  = new JadwalDAO();
        this.controller = new BookingController();
        this.riwayatDAO = new RiwayatDAO();
        this.userDAO    = new UserDAO();
        this.topupDAO   = new TopupDAO();
        setupUI();
        loadStasiunAsal();
        refreshRiwayat();
        refreshTopupList();
    }

    private void setupUI() {
        setTitle("Dashboard Penumpang - " + penumpang.getUsername());
        setSize(720, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("  Pesan Tiket  ",     buildTabPesan());
        tabs.addTab("  Riwayat  ",         buildTabRiwayat());
        tabs.addTab("  Top Up Saldo  ",    buildTabTopup());

        tabs.addChangeListener(e -> {
            refreshSaldo();
            if (tabs.getSelectedIndex() == 1) refreshRiwayat();
            if (tabs.getSelectedIndex() == 2) refreshTopupList();
        });
        add(tabs, BorderLayout.CENTER);
    }

    //  HEADER 
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(30, 80, 160));
        h.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblTitle = new JLabel("Pemesanan Tiket Kereta Api");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 17));

        lblSaldo = new JLabel("Saldo: " + FMT.format(penumpang.getSaldoWallet()));
        lblSaldo.setForeground(new Color(180, 230, 180));
        lblSaldo.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel lblUser = new JLabel("Halo, " + penumpang.getUsername() + "   ");
        lblUser.setForeground(new Color(200, 220, 255));
        lblUser.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton btnLogout = btn("Logout", new Color(200, 60, 60));
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        right.setOpaque(false);
        right.add(lblUser); right.add(lblSaldo);
        right.add(Box.createHorizontalStrut(10)); right.add(btnLogout);

        h.add(lblTitle, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    /** Ambil saldo terbaru dari DB dan perbarui label + objek penumpang */
    private void refreshSaldo() {
        try {
            double saldoBaru = userDAO.getSaldoById(penumpang.getId());
            penumpang.setSaldoWallet(saldoBaru);
            lblSaldo.setText("Saldo: " + FMT.format(saldoBaru));
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat saldo: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  TAB 1: PESAN TIKET 
    private JPanel buildTabPesan() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        panelCari  = buildPanelCari();
        panelHasil = buildPanelHasil();
        panelHasil.setVisible(false);
        p.add(panelCari,  BorderLayout.NORTH);
        p.add(panelHasil, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildPanelCari() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(18, 20, 10, 20),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 80, 160)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16))));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8); g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.weightx=0.15; p.add(bold("Stasiun Asal :"), g);
        g.gridx=1; g.weightx=0.35; comboAsal = new JComboBox<>(); comboAsal.setFont(new Font("Arial",Font.PLAIN,13)); p.add(comboAsal, g);
        g.gridx=2; g.weightx=0.04; JLabel arr=new JLabel("→",SwingConstants.CENTER); arr.setFont(new Font("Arial",Font.BOLD,16)); arr.setForeground(new Color(30,80,160)); p.add(arr,g);
        g.gridx=3; g.weightx=0.15; p.add(bold("Stasiun Tujuan :"), g);
        g.gridx=4; g.weightx=0.35; comboTujuan = new JComboBox<>(); comboTujuan.setFont(new Font("Arial",Font.PLAIN,13)); p.add(comboTujuan, g);

        g.gridx=0; g.gridy=1; g.gridwidth=5; g.fill=GridBagConstraints.NONE; g.anchor=GridBagConstraints.CENTER;
        JButton btnCari = btn("  Cari Kereta  ", new Color(30, 80, 160));
        p.add(btnCari, g);

        comboAsal.addActionListener(e -> loadStasiunTujuan());
        btnCari.addActionListener(e -> cariKereta());
        return p;
    }

    private JPanel buildPanelHasil() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(6, 20, 16, 20),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 80, 160)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10))));

        JLabel lbl = new JLabel("Pilih Kereta :"); lbl.setFont(new Font("Arial",Font.BOLD,13)); lbl.setForeground(new Color(30,80,160));
        modelKereta = new DefaultListModel<>();
        listKereta  = new JList<>(modelKereta);
        listKereta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        listKereta.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listKereta.setFixedCellHeight(44);
        listKereta.setCellRenderer(new StripeCellRenderer());

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        aksi.setBackground(Color.WHITE);
        JButton btnBatal = btn("  ← Kembali  ", new Color(120,120,120));
        JButton btnOk    = btn("  Konfirmasi & Bayar  ", new Color(34,140,60));
        btnBatal.addActionListener(e -> kembaliKeCari());
        btnOk.addActionListener(e   -> konfirmasi());
        aksi.add(btnBatal); aksi.add(btnOk);

        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(listKereta), BorderLayout.CENTER);
        p.add(aksi, BorderLayout.SOUTH);
        return p;
    }

    //  TAB 2: RIWAYAT 
    private JPanel buildTabRiwayat() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.setBackground(Color.WHITE);

        JLabel lbl = new JLabel("Riwayat Pemesanan Tiket Anda");
        lbl.setFont(new Font("Arial", Font.BOLD, 14)); lbl.setForeground(new Color(30,80,160));

        String[] cols = {"#","Kode Booking","Nama Kereta","Kelas","Asal","Tujuan","Total Bayar","Waktu Pesan"};
        modelRiwayat = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl = buildTable(modelRiwayat);

        JButton btnRefresh = btn("  Refresh  ", new Color(30,80,160));
        btnRefresh.addActionListener(e -> refreshRiwayat());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,4)); bot.setBackground(Color.WHITE); bot.add(btnRefresh);

        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    private void refreshRiwayat() {
        try {
            modelRiwayat.setRowCount(0);
            int n = 1;
            for (Object[] r : riwayatDAO.getRiwayatByUser(penumpang.getId())) {
                modelRiwayat.addRow(new Object[]{n++, r[1], r[2], r[3], r[4], r[5], FMT.format(r[6]), r[7]});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  TAB 3: TOP UP 
    private JPanel buildTabTopup() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        p.setBackground(Color.WHITE);

        // -- Form top up --
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30,80,160)),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,8,8,8);

        g.gridx=0; g.gridy=0; g.gridwidth=3; g.anchor=GridBagConstraints.CENTER;
        JLabel lblInfo = new JLabel("Ajukan Permintaan Top Up Saldo");
        lblInfo.setFont(new Font("Arial",Font.BOLD,14)); lblInfo.setForeground(new Color(30,80,160));
        form.add(lblInfo, g);

        g.gridy=1; g.gridwidth=1;
        JButton btnMinus = btn("  −  ", new Color(200,80,80));
        btnMinus.setFont(new Font("Arial", Font.BOLD, 16));
        form.add(btnMinus, g);

        g.gridx=1;
        lblJumlahTopup = new JLabel(FMT.format(jumlahTopup), SwingConstants.CENTER);
        lblJumlahTopup.setFont(new Font("Arial", Font.BOLD, 18));
        lblJumlahTopup.setForeground(new Color(30,80,160));
        lblJumlahTopup.setPreferredSize(new Dimension(200, 36));
        lblJumlahTopup.setBorder(BorderFactory.createLineBorder(new Color(30,80,160)));
        form.add(lblJumlahTopup, g);

        g.gridx=2;
        JButton btnPlus = btn("  +  ", new Color(34,140,60));
        btnPlus.setFont(new Font("Arial", Font.BOLD, 16));
        form.add(btnPlus, g);

        g.gridy=2; g.gridx=0; g.gridwidth=3;
        JLabel lblNote = new JLabel("* Kelipatan Rp 10.000  |  Minimum Rp 10.000", SwingConstants.CENTER);
        lblNote.setFont(new Font("Arial", Font.ITALIC, 11)); lblNote.setForeground(Color.GRAY);
        form.add(lblNote, g);

        g.gridy=3;
        JButton btnSubmit = btn("  Ajukan Permintaan  ", new Color(30,80,160));
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 13));
        form.add(btnSubmit, g);

        btnMinus.addActionListener(e -> {
            if (jumlahTopup > 10000) { jumlahTopup -= 10000; lblJumlahTopup.setText(FMT.format(jumlahTopup)); }
        });
        btnPlus.addActionListener(e -> { jumlahTopup += 10000; lblJumlahTopup.setText(FMT.format(jumlahTopup)); });
        btnSubmit.addActionListener(e -> submitTopup());

        // -- Tabel riwayat topup --
        JLabel lblRiwayat = new JLabel("Riwayat Permintaan Top Up");
        lblRiwayat.setFont(new Font("Arial", Font.BOLD, 13)); lblRiwayat.setForeground(new Color(30,80,160));

        String[] cols = {"#","Jumlah","Status","Waktu Pengajuan","Waktu Diproses"};
        modelTopup = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl = buildTable(modelTopup);
        // Warnai status
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int row,int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,sel,foc,row,col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    String s = val != null ? val.toString() : "";
                    lbl.setForeground("DITERIMA".equals(s) ? new Color(0,140,0) : "DITOLAK".equals(s) ? new Color(200,0,0) : new Color(180,130,0));
                    lbl.setBackground(row%2==0 ? Color.WHITE : new Color(240,245,255));
                    lbl.setFont(new Font("Arial", Font.BOLD, 12));
                }
                return lbl;
            }
        });

        JPanel botPanel = new JPanel(new BorderLayout(0,6));
        botPanel.setBackground(Color.WHITE);
        botPanel.add(lblRiwayat, BorderLayout.NORTH);
        botPanel.add(new JScrollPane(tbl), BorderLayout.CENTER);

        p.add(form, BorderLayout.NORTH);
        p.add(botPanel, BorderLayout.CENTER);
        return p;
    }

    private void submitTopup() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ajukan permintaan top up sebesar " + FMT.format(jumlahTopup) + "?\n" +
            "Saldo akan ditambahkan setelah Admin menyetujui.",
            "Konfirmasi Top Up", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            topupDAO.submitTopup(penumpang.getId(), jumlahTopup);
            JOptionPane.showMessageDialog(this,
                "Permintaan top up berhasil diajukan!\nTunggu persetujuan Admin.",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            jumlahTopup = 10000;
            lblJumlahTopup.setText(FMT.format(jumlahTopup));
            refreshTopupList();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengajukan:\n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTopupList() {
        try {
            modelTopup.setRowCount(0);
            int n = 1;
            for (Object[] r : topupDAO.getRequestsByUser(penumpang.getId())) {
                modelTopup.addRow(new Object[]{n++, FMT.format(r[1]), r[2], r[3], r[4] != null ? r[4] : "-"});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat top up: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  LOGIKA BOOKING 
    private void loadStasiunAsal() {
        try {
            comboAsal.removeAllItems(); comboAsal.addItem("-- Pilih Stasiun Asal --");
            for (String s : tiketDAO.getAllStasiunAsal()) comboAsal.addItem(s);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat stasiun asal: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStasiunTujuan() {
        String asal = (String) comboAsal.getSelectedItem();
        comboTujuan.removeAllItems();
        if (asal == null || asal.startsWith("--")) return;
        try {
            comboTujuan.addItem("-- Pilih Stasiun Tujuan --");
            for (String s : tiketDAO.getStasiunTujuanByAsal(asal)) comboTujuan.addItem(s);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat stasiun tujuan: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cariKereta() {
        String asal = (String) comboAsal.getSelectedItem();
        String tujuan = (String) comboTujuan.getSelectedItem();
        if (asal==null||asal.startsWith("--")||tujuan==null||tujuan.startsWith("--")) {
            JOptionPane.showMessageDialog(this,"Harap pilih stasiun asal dan tujuan!","Peringatan",JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            hasilCari = jadwalDAO.cariJadwal(asal, tujuan);
            modelKereta.clear();
            if (hasilCari.isEmpty()) {
                JOptionPane.showMessageDialog(this,"Tidak ada jadwal untuk rute "+asal+" → "+tujuan,"Info",JOptionPane.INFORMATION_MESSAGE); return;
            }
            for (Jadwal j : hasilCari) {
                String kursi = j.getKursiTersedia()>0 ? "Kursi: "+j.getKursiTersedia() : "HABIS";
                modelKereta.addElement(String.format("%-26s | %-10s | %-18s | %s",
                    j.getNamaKereta(), j.getKelas(), FMT.format(controller.getHargaPreview(j.getHargaDasar(),j.getKelas())), kursi));
            }
            panelCari.setVisible(false); panelHasil.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,"Gagal mencari: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kembaliKeCari() { panelHasil.setVisible(false); panelCari.setVisible(true); }

    private void konfirmasi() {
        int idx = listKereta.getSelectedIndex();
        if (idx < 0) { JOptionPane.showMessageDialog(this,"Pilih kereta terlebih dahulu!","Peringatan",JOptionPane.WARNING_MESSAGE); return; }
        Jadwal j = hasilCari.get(idx);
        if (j.getKursiTersedia()<=0) { JOptionPane.showMessageDialog(this,"Kursi habis!","Info",JOptionPane.WARNING_MESSAGE); return; }
        double harga = controller.getHargaPreview(j.getHargaDasar(), j.getKelas());
        int ok = JOptionPane.showConfirmDialog(this,
            "Kereta  : "+j.getNamaKereta()+"\nKelas   : "+j.getKelas()+
            "\nRute    : "+j.getStasiunAsal()+" → "+j.getStasiunTujuan()+
            "\nTotal   : "+FMT.format(harga)+"\n\nKonfirmasi pembayaran?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        String hasil = controller.prosesTiket(penumpang.getId(), j.getId(), j.getHargaDasar(), j.getKelas());
        String[] r = hasil.split("\\|", 2);
        if ("SUKSES".equals(r[0])) {
            refreshSaldo(); // ← Auto-refresh saldo setelah berhasil bayar
            JOptionPane.showMessageDialog(this, r[1], "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            kembaliKeCari();
        } else {
            JOptionPane.showMessageDialog(this, r[1], "Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }



    //  HELPERS UI 
    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false); b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
        return b;
    }

    private JLabel bold(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("Arial",Font.BOLD,13)); return l;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(new Font("Arial",Font.PLAIN,12)); t.setRowHeight(26);
        t.setSelectionBackground(new Color(190,215,255)); t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(220,225,235)); t.setShowGrid(true);
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int row,int col) {
                JLabel l=(JLabel)super.getTableCellRendererComponent(tbl,val,sel,foc,row,col);
                l.setBackground(new Color(30,80,160)); l.setForeground(Color.WHITE);
                l.setFont(new Font("Arial",Font.BOLD,12)); l.setOpaque(true);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0,0,2,1,new Color(60,110,200)));
                return l;
            }
        });
        t.getTableHeader().setPreferredSize(new Dimension(0,30));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int row,int col) {
                JLabel l=(JLabel)super.getTableCellRendererComponent(tbl,val,sel,foc,row,col);
                if (!sel) { l.setBackground(row%2==0?Color.WHITE:new Color(240,245,255)); l.setForeground(Color.DARK_GRAY); }
                l.setBorder(BorderFactory.createEmptyBorder(0,6,0,6)); return l;
            }
        });
        return t;
    }

    private static class StripeCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list,Object val,int idx,boolean sel,boolean foc) {
            JLabel l=(JLabel)super.getListCellRendererComponent(list,val,idx,sel,foc);
            l.setFont(new Font("Monospaced",Font.PLAIN,12));
            l.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
            if (!sel) { l.setBackground(idx%2==0?Color.WHITE:new Color(240,245,255)); l.setForeground(Color.DARK_GRAY); }
            return l;
        }
    }
}
