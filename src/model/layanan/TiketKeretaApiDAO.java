package model.layanan;

import model.Connector;
import model.entitas.Admin;
import model.entitas.Penumpang;
import model.entitas.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TiketKeretaApiDAO menangani operasi login dan pengambilan daftar stasiun.
 */
public class TiketKeretaApiDAO {

    /**
     * Melakukan login dengan username dan password.
     * @return objek User (Admin/Penumpang) atau null jika gagal.
     */
    public User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    int id = rs.getInt("id");
                    String uname = rs.getString("username");

                    if ("ADMIN".equals(role)) {
                        return new Admin(id, uname);
                    } else {
                        double saldo = rs.getDouble("saldo");
                        return new Penumpang(id, uname, saldo);
                    }
                }
            }
        }
        return null; // Login gagal
    }

    /**
     * Mengambil daftar stasiun asal yang unik dari database.
     */
    public List<String> getAllStasiunAsal() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT stasiun_asal FROM jadwal_kereta ORDER BY stasiun_asal";
        try (Connection conn = Connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("stasiun_asal"));
            }
        }
        return list;
    }

    /**
     * Mengambil daftar stasiun tujuan yang unik berdasarkan stasiun asal.
     */
    public List<String> getStasiunTujuanByAsal(String stasiunAsal) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT stasiun_tujuan FROM jadwal_kereta WHERE stasiun_asal = ? ORDER BY stasiun_tujuan";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stasiunAsal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("stasiun_tujuan"));
                }
            }
        }
        return list;
    }
}