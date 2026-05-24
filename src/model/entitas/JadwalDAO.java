package model.entitas;

import model.Connector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JadwalDAO menangani operasi CRUD untuk tabel jadwal_kereta.
 */
public class JadwalDAO {

    /**
     * READ: Mengambil semua data jadwal kereta dari database.
     */
    public List<Jadwal> getAllJadwal() throws SQLException {
        List<Jadwal> list = new ArrayList<>();
        String sql = "SELECT * FROM jadwal_kereta ORDER BY nama_kereta";
        try (Connection conn = Connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * READ: Mengambil jadwal berdasarkan stasiun asal dan tujuan.
     */
    public List<Jadwal> cariJadwal(String stasiunAsal, String stasiunTujuan) throws SQLException {
        List<Jadwal> list = new ArrayList<>();
        String sql = "SELECT * FROM jadwal_kereta WHERE stasiun_asal = ? AND stasiun_tujuan = ? ORDER BY kelas, harga_dasar";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stasiunAsal);
            ps.setString(2, stasiunTujuan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * CREATE: Menambahkan data jadwal baru ke dalam database.
     */
    public boolean tambahJadwal(String namaKereta, String keterangan, String kelas,
                                 String stasiunAsal, String stasiunTujuan,
                                 double hargaDasar, int kursiTersedia) throws SQLException {
        String sql = "INSERT INTO jadwal_kereta (nama_kereta, keterangan, kelas, stasiun_asal, stasiun_tujuan, harga_dasar, kursi_tersedia) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaKereta);
            ps.setString(2, keterangan);
            ps.setString(3, kelas);
            ps.setString(4, stasiunAsal);
            ps.setString(5, stasiunTujuan);
            ps.setDouble(6, hargaDasar);
            ps.setInt(7, kursiTersedia);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * UPDATE: Memperbarui data jadwal di database berdasarkan ID.
     */
    public boolean updateJadwal(int id, String namaKereta, String keterangan, String kelas,
                                 String stasiunAsal, String stasiunTujuan,
                                 double hargaDasar, int kursiTersedia) throws SQLException {
        String sql = "UPDATE jadwal_kereta SET nama_kereta=?, keterangan=?, kelas=?, stasiun_asal=?, stasiun_tujuan=?, harga_dasar=?, kursi_tersedia=? WHERE id=?";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaKereta);
            ps.setString(2, keterangan);
            ps.setString(3, kelas);
            ps.setString(4, stasiunAsal);
            ps.setString(5, stasiunTujuan);
            ps.setDouble(6, hargaDasar);
            ps.setInt(7, kursiTersedia);
            ps.setInt(8, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * DELETE: Menghapus data jadwal dari database berdasarkan ID.
     */
    public boolean hapusJadwal(int id) throws SQLException {
        String sql = "DELETE FROM jadwal_kereta WHERE id = ?";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Jadwal mapRow(ResultSet rs) throws SQLException {
        return new Jadwal(
            rs.getInt("id"),
            rs.getString("nama_kereta"),
            rs.getString("kelas"),
            rs.getString("stasiun_asal"),
            rs.getString("stasiun_tujuan"),
            rs.getDouble("harga_dasar"),
            rs.getInt("kursi_tersedia"),
            rs.getString("keterangan")
        );
    }
}