package model.entitas;

import model.Connector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // READ: Ambil semua data penumpang
    public List<User> getAllPenumpang() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'PENUMPANG'";
        try (Connection conn = Connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Penumpang(
                    rs.getInt("id"), 
                    rs.getString("username"), 
                    rs.getDouble("saldo")
                ));
            }
        }
        return list;
    }

    // CREATE: Tambah Penumpang Baru
    public boolean tambahPenumpang(String username, String password, double saldo) throws SQLException {
        String sql = "INSERT INTO users (username, password, saldo, role) VALUES (?, ?, ?, 'PENUMPANG')";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setDouble(3, saldo);
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: Hapus Penumpang
    public boolean hapusUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}