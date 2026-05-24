package model.layanan;

import model.Connector; // Sesuaikan dengan class koneksi Anda
import model.entitas.Admin;
import model.entitas.Penumpang;
import model.entitas.User;
import java.sql.*;

public class TiketKeretaApiDAO {
    public User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        // Gunakan Connector.java yang sudah Anda buat
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
}