package view;

import model.entitas.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminFrame extends JFrame {

    private final User admin;
    private final UserDAO userDAO;
    private final JadwalDAO jadwalDAO;
    private final RiwayatDAO riwayatDAO;
    private final TopupDAO topupDAO;

    private DefaultTableModel modelUser, modelJadwal, modelRiwayat, modelTopup;
    private JTable tabelUser, tabelJadwal, tabelTopup;
    private JTextField txtUUsername,txtUPassword,txtUSaldo,txtJNama,txtJKet,txtJAsal,txtJTujuan,txtJHarga,txtJKursi;
    private JComboBox<String> comboURole, comboJKelas;

    private static final NumberFormat FMT = NumberFormat.getCurrencyInstance(new Locale("id","ID"));

    public AdminFrame(User admin) {
        this.admin      = admin;
        this.userDAO    = new UserDAO();
        this.jadwalDAO  = new JadwalDAO();
        this.riwayatDAO = new RiwayatDAO();
        this.topupDAO   = new TopupDAO();
        setupUI();
        refreshUser(); refreshJadwal(); refreshRiwayat(); refreshTopup();
    }

    private void setupUI() {
        setTitle("Admin Dashboard - " + admin.getUsername());
        setSize(950, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30,80,160));
        header.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        JLabel lblT = new JLabel("Admin Panel - Sistem Tiket Kereta Api");
        lblT.setForeground(Color.WHITE); lblT.setFont(new Font("Arial",Font.BOLD,17));
        JLabel lblA = new JLabel("Login sebagai: "+admin.getUsername()+" (Admin)   ");
        lblA.setForeground(new Color(180,210,255)); lblA.setFont(new Font("Arial",Font.PLAIN,12));
        JButton btnOut = btn("Logout", new Color(200,60,60));
        btnOut.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0)); right.setOpaque(false);
        right.add(lblA); right.add(btnOut);
        header.add(lblT,BorderLayout.WEST); header.add(right,BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial",Font.BOLD,13));
        tabs.addTab("  Manajemen User  ",     buildTabUser());
        tabs.addTab("  Manajemen Jadwal  ",   buildTabJadwal());
        tabs.addTab("  Riwayat Transaksi  ",  buildTabRiwayat());
        tabs.addTab("  Permintaan Top Up  ",  buildTabTopup());

        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i==2) refreshRiwayat();
            if (i==3) refreshTopup();
        });

        add(header,BorderLayout.NORTH);
        add(tabs,BorderLayout.CENTER);
    }

    // ── TAB 1: USER ──────────────────────────────────────────────────────────
    private JPanel buildTabUser() {
        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        p.setBackground(Color.WHITE);

        String[] cols = {"ID","Username","Saldo","Role","Dibuat"};
        modelUser = new DefaultTableModel(cols,0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        tabelUser = buildTable(modelUser);
        tabelUser.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabelUser.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabelUser.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabelUser.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabelUser.getColumnModel().getColumn(4).setPreferredWidth(180);
        tabelUser.getSelectionModel().addListSelectionListener(e -> { if(!e.getValueIsAdjusting()) isiFormUser(); });

        JPanel form = new JPanel(new BorderLayout(0,6));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30,80,160)),
            BorderFactory.createEmptyBorder(10,14,8,14)));

        JLabel lf = new JLabel("Form User"); lf.setFont(new Font("Arial",Font.BOLD,13)); lf.setForeground(new Color(30,80,160));

        JPanel fields = new JPanel(new GridLayout(2,4,12,8)); fields.setBackground(Color.WHITE);
        txtUUsername = field(); txtUPassword = field(); txtUSaldo = field(); txtUSaldo.setText("0");
        comboURole = new JComboBox<>(new String[]{"PENUMPANG","ADMIN"});
        fields.add(bold("Username:")); fields.add(txtUUsername);
        fields.add(bold("Password:")); fields.add(txtUPassword);
        fields.add(bold("Saldo:"));    fields.add(txtUSaldo);
        fields.add(bold("Role:"));     fields.add(comboURole);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,12,4)); btns.setBackground(Color.WHITE);
        JButton bT=btn("  Tambah  ",new Color(34,140,60)),bU=btn("  Update  ",new Color(200,130,0)),
                bH=btn("  Hapus  ",new Color(200,50,50)),bB=btn("  Bersihkan  ",new Color(100,100,100));
        bT.addActionListener(e->tambahUser()); bU.addActionListener(e->updateUser());
        bH.addActionListener(e->hapusUser());  bB.addActionListener(e->bersihUser());
        btns.add(bT); btns.add(bU); btns.add(bH); btns.add(bB);

        form.add(lf,BorderLayout.NORTH); form.add(fields,BorderLayout.CENTER); form.add(btns,BorderLayout.SOUTH);
        p.add(new JScrollPane(tabelUser),BorderLayout.CENTER); p.add(form,BorderLayout.SOUTH);
        return p;
    }

    private void isiFormUser() {
        int r=tabelUser.getSelectedRow(); if(r<0) return;
        txtUUsername.setText((String)modelUser.getValueAt(r,1)); txtUPassword.setText("");
        txtUSaldo.setText(modelUser.getValueAt(r,2).toString());
        comboURole.setSelectedItem(modelUser.getValueAt(r,3));
    }
    private void tambahUser() {
        try {
            String un=txtUUsername.getText().trim(),pw=txtUPassword.getText().trim();
            if(un.isEmpty()||pw.isEmpty()){warn("Username & Password wajib diisi!");return;}
            userDAO.tambahUser(un,pw,Double.parseDouble(txtUSaldo.getText().trim()),(String)comboURole.getSelectedItem());
            info("User ditambahkan!"); refreshUser(); bersihUser();
        } catch(NumberFormatException x){warn("Saldo harus angka!");}
        catch(SQLException x){dbErr("tambah user",x);}
    }
    private void updateUser() {
        int r=tabelUser.getSelectedRow(); if(r<0){warn("Pilih user!");return;}
        try {
            userDAO.updateUser((int)modelUser.getValueAt(r,0),txtUUsername.getText().trim(),
                txtUPassword.getText().trim(),Double.parseDouble(txtUSaldo.getText().trim()),
                (String)comboURole.getSelectedItem());
            info("User diperbarui!"); refreshUser(); bersihUser();
        } catch(NumberFormatException x){warn("Saldo harus angka!");}
        catch(SQLException x){dbErr("update user",x);}
    }
    private void hapusUser() {
        int r=tabelUser.getSelectedRow(); if(r<0){warn("Pilih user!");return;}
        if(JOptionPane.showConfirmDialog(this,"Hapus user '"+modelUser.getValueAt(r,1)+"'?","Konfirmasi",
            JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
        try { userDAO.hapusUser((int)modelUser.getValueAt(r,0)); info("User dihapus!"); refreshUser(); bersihUser();
        } catch(SQLException x){dbErr("hapus user",x);}
    }
    private void refreshUser() {
        try { modelUser.setRowCount(0); for(Object[] r:userDAO.getAllUsers()) modelUser.addRow(r);
        } catch(SQLException x){dbErr("muat user",x);}
    }
    private void bersihUser() {
        txtUUsername.setText(""); txtUPassword.setText(""); txtUSaldo.setText("0");
        comboURole.setSelectedIndex(0); tabelUser.clearSelection();
    }

    // ── TAB 2: JADWAL ────────────────────────────────────────────────────────
    private JPanel buildTabJadwal() {
        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); p.setBackground(Color.WHITE);

        String[] cols = {"ID","Nama Kereta","Kelas","Asal","Tujuan","Harga","Kursi","Keterangan"};
        modelJadwal = new DefaultTableModel(cols,0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        tabelJadwal = buildTable(modelJadwal);
        int[] ws = {40,160,90,130,130,110,60,150};
        for(int i=0;i<ws.length;i++) tabelJadwal.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
        tabelJadwal.getSelectionModel().addListSelectionListener(e -> { if(!e.getValueIsAdjusting()) isiFormJadwal(); });

        JPanel form = new JPanel(new BorderLayout(0,6)); form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30,80,160)),
            BorderFactory.createEmptyBorder(10,14,8,14)));

        JLabel lf=new JLabel("Form Jadwal"); lf.setFont(new Font("Arial",Font.BOLD,13)); lf.setForeground(new Color(30,80,160));

        txtJNama=field(); txtJKet=field(); txtJAsal=field(); txtJTujuan=field();
        txtJHarga=field(); txtJHarga.setText("0"); txtJKursi=field(); txtJKursi.setText("100");
        comboJKelas=new JComboBox<>(new String[]{"Ekonomi","Bisnis","Eksekutif"});

        JPanel fields=new JPanel(new GridLayout(3,4,12,8)); fields.setBackground(Color.WHITE);
        fields.add(bold("Nama Kereta:"));    fields.add(txtJNama);
        fields.add(bold("Keterangan:"));     fields.add(txtJKet);
        fields.add(bold("Kelas:"));          fields.add(comboJKelas);
        fields.add(bold("Stasiun Asal:"));   fields.add(txtJAsal);
        fields.add(bold("Stasiun Tujuan:")); fields.add(txtJTujuan);
        fields.add(bold("Harga Dasar:"));    fields.add(txtJHarga);
        fields.add(bold("Kursi Tersedia:")); fields.add(txtJKursi);

        JPanel btns=new JPanel(new FlowLayout(FlowLayout.CENTER,12,4)); btns.setBackground(Color.WHITE);
        JButton bT=btn("  Tambah  ",new Color(34,140,60)),bU=btn("  Update  ",new Color(200,130,0)),
                bH=btn("  Hapus  ",new Color(200,50,50)),bB=btn("  Bersihkan  ",new Color(100,100,100));
        bT.addActionListener(e->tambahJadwal()); bU.addActionListener(e->updateJadwal());
        bH.addActionListener(e->hapusJadwal());  bB.addActionListener(e->bersihJadwal());
        btns.add(bT); btns.add(bU); btns.add(bH); btns.add(bB);

        form.add(lf,BorderLayout.NORTH); form.add(fields,BorderLayout.CENTER); form.add(btns,BorderLayout.SOUTH);
        p.add(new JScrollPane(tabelJadwal),BorderLayout.CENTER); p.add(form,BorderLayout.SOUTH);
        return p;
    }

    private void isiFormJadwal() {
        int r=tabelJadwal.getSelectedRow(); if(r<0) return;
        txtJNama.setText((String)modelJadwal.getValueAt(r,1));
        comboJKelas.setSelectedItem(modelJadwal.getValueAt(r,2));
        txtJAsal.setText((String)modelJadwal.getValueAt(r,3));
        txtJTujuan.setText((String)modelJadwal.getValueAt(r,4));
        txtJHarga.setText(modelJadwal.getValueAt(r,5).toString());
        txtJKursi.setText(modelJadwal.getValueAt(r,6).toString());
        Object k=modelJadwal.getValueAt(r,7); txtJKet.setText(k!=null?k.toString():"");
    }
    private void tambahJadwal() {
        try {
            if(txtJNama.getText().trim().isEmpty()||txtJAsal.getText().trim().isEmpty()||txtJTujuan.getText().trim().isEmpty()){warn("Nama, asal & tujuan wajib diisi!");return;}
            jadwalDAO.tambahJadwal(txtJNama.getText().trim(),txtJKet.getText().trim(),(String)comboJKelas.getSelectedItem(),
                txtJAsal.getText().trim(),txtJTujuan.getText().trim(),
                Double.parseDouble(txtJHarga.getText().trim()),Integer.parseInt(txtJKursi.getText().trim()));
            info("Jadwal ditambahkan!"); refreshJadwal(); bersihJadwal();
        } catch(NumberFormatException x){warn("Harga & kursi harus angka!");}
        catch(SQLException x){dbErr("tambah jadwal",x);}
    }
    private void updateJadwal() {
        int r=tabelJadwal.getSelectedRow(); if(r<0){warn("Pilih jadwal!");return;}
        try {
            jadwalDAO.updateJadwal((int)modelJadwal.getValueAt(r,0),txtJNama.getText().trim(),
                txtJKet.getText().trim(),(String)comboJKelas.getSelectedItem(),
                txtJAsal.getText().trim(),txtJTujuan.getText().trim(),
                Double.parseDouble(txtJHarga.getText().trim()),Integer.parseInt(txtJKursi.getText().trim()));
            info("Jadwal diperbarui!"); refreshJadwal(); bersihJadwal();
        } catch(NumberFormatException x){warn("Harga & kursi harus angka!");}
        catch(SQLException x){dbErr("update jadwal",x);}
    }
    private void hapusJadwal() {
        int r=tabelJadwal.getSelectedRow(); if(r<0){warn("Pilih jadwal!");return;}
        if(JOptionPane.showConfirmDialog(this,"Hapus jadwal '"+modelJadwal.getValueAt(r,1)+"'?","Konfirmasi",
            JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
        try { jadwalDAO.hapusJadwal((int)modelJadwal.getValueAt(r,0)); info("Jadwal dihapus!"); refreshJadwal(); bersihJadwal();
        } catch(SQLException x){dbErr("hapus jadwal",x);}
    }
    private void refreshJadwal() {
        try { modelJadwal.setRowCount(0);
            for(Jadwal j:jadwalDAO.getAllJadwal()) modelJadwal.addRow(new Object[]{
                j.getId(),j.getNamaKereta(),j.getKelas(),j.getStasiunAsal(),j.getStasiunTujuan(),j.getHargaDasar(),j.getKursiTersedia(),j.getKeterangan()});
        } catch(SQLException x){dbErr("muat jadwal",x);}
    }
    private void bersihJadwal() {
        txtJNama.setText("");txtJKet.setText("");txtJAsal.setText("");txtJTujuan.setText("");
        txtJHarga.setText("0");txtJKursi.setText("100");comboJKelas.setSelectedIndex(0);tabelJadwal.clearSelection();
    }

    // ── TAB 3: RIWAYAT ───────────────────────────────────────────────────────
    private JPanel buildTabRiwayat() {
        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); p.setBackground(Color.WHITE);
        JLabel lbl=new JLabel("Riwayat Semua Transaksi");
        lbl.setFont(new Font("Arial",Font.BOLD,14)); lbl.setForeground(new Color(30,80,160));

        String[] cols={"#","Username","Kode Booking","Nama Kereta","Kelas","Asal","Tujuan","Total Bayar","Waktu"};
        modelRiwayat=new DefaultTableModel(cols,0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl=buildTable(modelRiwayat);
        int[] ws={35,100,110,150,80,110,110,130,160};
        for(int i=0;i<ws.length;i++) tbl.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        JButton btnR=btn("  ↺ Refresh  ",new Color(30,80,160)); btnR.addActionListener(e->refreshRiwayat());
        JPanel bot=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,4)); bot.setBackground(Color.WHITE); bot.add(btnR);

        p.add(lbl,BorderLayout.NORTH); p.add(new JScrollPane(tbl),BorderLayout.CENTER); p.add(bot,BorderLayout.SOUTH);
        return p;
    }
    private void refreshRiwayat() {
        try { modelRiwayat.setRowCount(0); int n=1;
            for(Object[] r:riwayatDAO.getAllRiwayat())
                modelRiwayat.addRow(new Object[]{n++,r[1],r[2],r[3],r[4],r[5],r[6],FMT.format(r[7]),r[8]});
        } catch(SQLException x){dbErr("muat riwayat",x);}
    }

    // ── TAB 4: PERMINTAAN TOP UP ─────────────────────────────────────────────
    private JPanel buildTabTopup() {
        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); p.setBackground(Color.WHITE);

        JLabel lbl=new JLabel("Permintaan Top Up Saldo");
        lbl.setFont(new Font("Arial",Font.BOLD,14)); lbl.setForeground(new Color(30,80,160));

        String[] cols={"ID","Username","Jumlah","Status","Waktu Pengajuan","Waktu Diproses"};
        modelTopup=new DefaultTableModel(cols,0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl=buildTable(modelTopup);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(40);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(120);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(130);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(180);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(180);

        // Warnai kolom status
        tbl.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int row,int col){
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,val,sel,foc,row,col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if(!sel){
                    String s=val!=null?val.toString():"";
                    l.setForeground("DITERIMA".equals(s)?new Color(0,130,0):"DITOLAK".equals(s)?new Color(200,0,0):new Color(180,130,0));
                    l.setBackground(row%2==0?Color.WHITE:new Color(240,245,255));
                    l.setFont(new Font("Arial",Font.BOLD,12));
                }
                return l;
            }
        });
        tabelTopup = tbl;

        // Tombol aksi
        JPanel bot=new JPanel(new FlowLayout(FlowLayout.CENTER,16,6)); bot.setBackground(Color.WHITE);
        JButton btnTerima=btn("  ✔ Terima  ",new Color(34,140,60));
        JButton btnTolak =btn("  ✘ Tolak  ",new Color(200,50,50));
        JButton btnR     =btn("  ↺ Refresh  ",new Color(30,80,160));

        btnTerima.addActionListener(e -> {
            int r=tabelTopup.getSelectedRow(); if(r<0){warn("Pilih permintaan terlebih dahulu!");return;}
            String status=(String)modelTopup.getValueAt(r,3);
            if(!"PENDING".equals(status)){warn("Hanya permintaan PENDING yang bisa diproses!");return;}
            int id=(int)modelTopup.getValueAt(r,0);
            String user=(String)modelTopup.getValueAt(r,1);
            String jumlah=modelTopup.getValueAt(r,2).toString();
            if(JOptionPane.showConfirmDialog(this,
                "Terima top up "+jumlah+" untuk user '"+user+"'?\nSaldo akan langsung ditambahkan.",
                "Konfirmasi",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)!=JOptionPane.YES_OPTION) return;
            try {
                if(topupDAO.approveRequest(id)) { info("Top up diterima! Saldo user telah diperbarui."); refreshTopup(); refreshUser(); }
                else warn("Gagal menerima, mungkin sudah diproses.");
            } catch(SQLException x){dbErr("terima topup",x);}
        });

        btnTolak.addActionListener(e -> {
            int r=tabelTopup.getSelectedRow(); if(r<0){warn("Pilih permintaan terlebih dahulu!");return;}
            String status=(String)modelTopup.getValueAt(r,3);
            if(!"PENDING".equals(status)){warn("Hanya permintaan PENDING yang bisa diproses!");return;}
            int id=(int)modelTopup.getValueAt(r,0);
            if(JOptionPane.showConfirmDialog(this,
                "Tolak permintaan top up dari '"+modelTopup.getValueAt(r,1)+"'?",
                "Konfirmasi",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
            try {
                if(topupDAO.rejectRequest(id)) { info("Permintaan top up ditolak."); refreshTopup(); }
                else warn("Gagal menolak, mungkin sudah diproses.");
            } catch(SQLException x){dbErr("tolak topup",x);}
        });

        btnR.addActionListener(e -> refreshTopup());
        bot.add(btnTerima); bot.add(btnTolak); bot.add(btnR);

        p.add(lbl,BorderLayout.NORTH);
        p.add(new JScrollPane(tbl),BorderLayout.CENTER);
        p.add(bot,BorderLayout.SOUTH);
        return p;
    }

    private void refreshTopup() {
        try { modelTopup.setRowCount(0);
            for(Object[] r:topupDAO.getAllRequests())
                modelTopup.addRow(new Object[]{r[0],r[1],FMT.format(r[2]),r[3],r[4],r[5]!=null?r[5]:"-"});
        } catch(SQLException x){dbErr("muat topup",x);}
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private JTable buildTable(DefaultTableModel model) {
        JTable t=new JTable(model);
        t.setFont(new Font("Arial",Font.PLAIN,12)); t.setRowHeight(26);
        t.setSelectionBackground(new Color(190,215,255)); t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(220,225,235)); t.setShowGrid(true);
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int row,int col){
                JLabel l=(JLabel)super.getTableCellRendererComponent(tbl,val,sel,foc,row,col);
                l.setBackground(new Color(30,80,160)); l.setForeground(Color.WHITE);
                l.setFont(new Font("Arial",Font.BOLD,12)); l.setOpaque(true);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0,0,2,1,new Color(60,110,200)));
                return l;
            }
        });
        t.getTableHeader().setPreferredSize(new Dimension(0,30));
        t.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int row,int col){
                JLabel l=(JLabel)super.getTableCellRendererComponent(tbl,val,sel,foc,row,col);
                if(!sel){l.setBackground(row%2==0?Color.WHITE:new Color(240,245,255));l.setForeground(Color.DARK_GRAY);}
                l.setBorder(BorderFactory.createEmptyBorder(0,6,0,6)); return l;
            }
        });
        return t;
    }

    private JButton btn(String t,Color bg){
        JButton b=new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial",Font.BOLD,12)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false); b.setOpaque(true); b.setBorder(BorderFactory.createEmptyBorder(7,18,7,18));
        return b;
    }
    private JLabel bold(String t){JLabel l=new JLabel(t);l.setFont(new Font("Arial",Font.BOLD,12));l.setForeground(new Color(60,60,60));return l;}
    private JTextField field(){JTextField f=new JTextField();f.setFont(new Font("Arial",Font.PLAIN,12));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(180,190,210)),BorderFactory.createEmptyBorder(3,6,3,6)));return f;}
    private void warn(String m){JOptionPane.showMessageDialog(this,m,"Peringatan",JOptionPane.WARNING_MESSAGE);}
    private void info(String m){JOptionPane.showMessageDialog(this,m,"Sukses",JOptionPane.INFORMATION_MESSAGE);}
    private void dbErr(String a,SQLException x){JOptionPane.showMessageDialog(this,"Gagal "+a+":\n"+x.getMessage(),"Error DB",JOptionPane.ERROR_MESSAGE);x.printStackTrace();}
}
