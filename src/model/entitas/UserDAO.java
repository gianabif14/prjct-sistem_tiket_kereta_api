package model.entitas;

import model.Connector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO menangani operasi CRUD untuk tabel users.
 */
public class UserDAO {

    /**
     * Ambil saldo terbaru dari DB berdasarkan ID user.
     */
    public double getSaldoById(int idUser) throws SQLException {
        String sql = "SELECT saldo FROM users WHERE id = ?";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("saldo");
            }
        }
        return 0.0;
    }

    /**
     * READ: Ambil semua data penumpang.
     */
    public List<User> getAllPenumpang() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'PENUMPANG' ORDER BY username";
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

    /**
     * READ: Ambil semua data user (admin dan penumpang).
     */
    public List<Object[]> getAllUsers() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, username, saldo, role, created_at FROM users ORDER BY username";
        try (Connection conn = Connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getDouble("saldo"),
                    rs.getString("role"),
                    rs.getString("created_at")
                });
            }
        }
        return list;
    }

    /**
     * CREATE: Tambah User Baru.
     */
    public boolean tambahUser(String username, String password, double saldo, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, saldo, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setDouble(3, saldo);
            ps.setString(4, role);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * UPDATE: Perbarui data user.
     */
    public boolean updateUser(int id, String username, String password, double saldo, String role) throws SQLException {
        String sql;
        if (password == null || password.isEmpty()) {
            sql = "UPDATE users SET username=?, saldo=?, role=? WHERE id=?";
            try (Connection conn = Connector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setDouble(2, saldo);
                ps.setString(3, role);
                ps.setInt(4, id);
                return ps.executeUpdate() > 0;
            }
        } else {
            sql = "UPDATE users SET username=?, password=?, saldo=?, role=? WHERE id=?";
            try (Connection conn = Connector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setDouble(3, saldo);
                ps.setString(4, role);
                ps.setInt(5, id);
                return ps.executeUpdate() > 0;
            }
        }
    }

    /**
     * DELETE: Hapus User berdasarkan ID.
     */
    public boolean hapusUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}