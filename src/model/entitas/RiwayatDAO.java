package model.entitas;

import model.Connector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RiwayatDAO menangani pengambilan riwayat transaksi tiket.
 * Query JOIN antara tabel tiket, users, dan jadwal_kereta.
 */
public class RiwayatDAO {

    /**
     * Ambil riwayat pemesanan untuk satu penumpang berdasarkan ID user.
     * Kolom: id, kode_booking, nama_kereta, kelas, stasiun_asal, stasiun_tujuan, total_bayar, waktu_pesan
     */
    public List<Object[]> getRiwayatByUser(int idUser) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT t.id, t.kode_booking, j.nama_kereta, j.kelas, " +
                     "j.stasiun_asal, j.stasiun_tujuan, t.total_bayar, t.waktu_pesan " +
                     "FROM tiket t " +
                     "JOIN jadwal_kereta j ON t.id_jadwal = j.id " +
                     "WHERE t.id_user = ? " +
                     "ORDER BY t.waktu_pesan DESC";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("kode_booking"),
                        rs.getString("nama_kereta"),
                        rs.getString("kelas"),
                        rs.getString("stasiun_asal"),
                        rs.getString("stasiun_tujuan"),
                        rs.getDouble("total_bayar"),
                        rs.getString("waktu_pesan")
                    });
                }
            }
        }
        return list;
    }

    /**
     * Ambil semua riwayat transaksi untuk Admin (semua user).
     * Kolom: id, username, kode_booking, nama_kereta, kelas, stasiun_asal, stasiun_tujuan, total_bayar, waktu_pesan
     */
    public List<Object[]> getAllRiwayat() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT t.id, u.username, t.kode_booking, j.nama_kereta, j.kelas, " +
                     "j.stasiun_asal, j.stasiun_tujuan, t.total_bayar, t.waktu_pesan " +
                     "FROM tiket t " +
                     "JOIN users u ON t.id_user = u.id " +
                     "JOIN jadwal_kereta j ON t.id_jadwal = j.id " +
                     "ORDER BY t.waktu_pesan DESC";
        try (Connection conn = Connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("kode_booking"),
                    rs.getString("nama_kereta"),
                    rs.getString("kelas"),
                    rs.getString("stasiun_asal"),
                    rs.getString("stasiun_tujuan"),
                    rs.getDouble("total_bayar"),
                    rs.getString("waktu_pesan")
                });
            }
        }
        return list;
    }
}
