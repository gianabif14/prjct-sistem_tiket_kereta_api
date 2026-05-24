package model.entitas;

import model.Connector; // Pastikan class Connector.java sudah ada
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JadwalDAO {
    
    /**
     * READ: Mengambil semua data jadwal kereta dari database.
     * Digunakan oleh AdminFrame untuk mengisi tabel dan MainFrame untuk mengisi ComboBox.
     */
    public List<Jadwal> getAllJadwal() throws SQLException {
        List<Jadwal> list = new ArrayList<>();
        String sql = "SELECT * FROM jadwal_kereta";
        
        // Menggunakan try-with-resources agar koneksi otomatis tertutup (mencegah memory leak)
        try (Connection conn = Connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                list.add(new Jadwal(
                    rs.getInt("id"), 
                    rs.getString("nama_kereta"), 
                    rs.getString("kelas"),
                    rs.getString("stasiun_asal"), 
                    rs.getString("stasiun_tujuan"),
                    rs.getDouble("harga_dasar"), 
                    rs.getInt("kursi_tersedia")
                ));
            }
        }
        return list;
    }

    /**
     * CREATE: Menambahkan data jadwal baru ke dalam database.
     * Digunakan oleh AdminFrame.
     */
    public boolean tambahJadwal(Jadwal j) throws SQLException {
        // Kolom keterangan diset default '-' untuk penyederhanaan
        String sql = "INSERT INTO jadwal_kereta (nama_kereta, kelas, stasiun_asal, stasiun_tujuan, harga_dasar, kursi_tersedia, keterangan) VALUES (?, ?, ?, ?, ?, ?, '-')";
        
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, j.getNamaKereta());
            ps.setString(2, j.getKelas());
            ps.setString(3, j.getStasiunAsal());
            ps.setString(4, j.getStasiunTujuan());
            ps.setDouble(5, j.getHargaDasar());
            ps.setInt(6, j.getKursiTersedia());
            
            // Mengembalikan true jika ada baris yang terpengaruh (berhasil diinsert)
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * DELETE: Menghapus data jadwal dari database berdasarkan ID.
     * Digunakan oleh AdminFrame.
     */
    public boolean hapusJadwal(int id) throws SQLException {
        String sql = "DELETE FROM jadwal_kereta WHERE id = ?";
        
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}