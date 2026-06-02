package model.entitas;

import model.Connector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TopupDAO menangani permintaan top up saldo penumpang.
 * Alur: Penumpang submit request → Admin approve/tolak → Saldo terupdate (ACID).
 */
public class TopupDAO {

    /**
     * Penumpang mengajukan permintaan top up saldo.
     */
    public boolean submitTopup(int idUser, double jumlah) throws SQLException {
        String sql = "INSERT INTO topup_request (id_user, jumlah) VALUES (?, ?)";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setDouble(2, jumlah);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Admin: ambil semua permintaan top up (terbaru dulu).
     * Kolom: id, username, jumlah, status, waktu_request, waktu_proses
     */
    public List<Object[]> getAllRequests() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT r.id, u.username, r.jumlah, r.status, r.waktu_request, r.waktu_proses " +
                     "FROM topup_request r " +
                     "JOIN users u ON r.id_user = u.id " +
                     "ORDER BY r.waktu_request DESC";
        try (Connection conn = Connector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getDouble("jumlah"),
                    rs.getString("status"),
                    rs.getString("waktu_request"),
                    rs.getString("waktu_proses")
                });
            }
        }
        return list;
    }

    /**
     * Penumpang: ambil riwayat permintaan top up miliknya.
     * Kolom: id, jumlah, status, waktu_request, waktu_proses
     */
    public List<Object[]> getRequestsByUser(int idUser) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, jumlah, status, waktu_request, waktu_proses " +
                     "FROM topup_request WHERE id_user = ? ORDER BY waktu_request DESC";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("jumlah"),
                        rs.getString("status"),
                        rs.getString("waktu_request"),
                        rs.getString("waktu_proses")
                    });
                }
            }
        }
        return list;
    }

    /**
     * Admin menerima permintaan top up: update saldo user + ubah status (ACID).
     */
    public boolean approveRequest(int idRequest) throws SQLException {
        Connection conn = null;
        try {
            conn = Connector.getConnection();
            conn.setAutoCommit(false);

            // Ambil data request yang masih PENDING
            int idUser;
            double jumlah;
            String getReq = "SELECT id_user, jumlah FROM topup_request WHERE id = ? AND status = 'PENDING'";
            try (PreparedStatement ps = conn.prepareStatement(getReq)) {
                ps.setInt(1, idRequest);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // Sudah diproses atau tidak ditemukan
                    }
                    idUser = rs.getInt("id_user");
                    jumlah = rs.getDouble("jumlah");
                }
            }

            // Tambah saldo user
            String updateSaldo = "UPDATE users SET saldo = saldo + ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSaldo)) {
                ps.setDouble(1, jumlah);
                ps.setInt(2, idUser);
                ps.executeUpdate();
            }

            // Update status request ke DITERIMA
            String updateStatus = "UPDATE topup_request SET status = 'DITERIMA', waktu_proses = NOW() WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateStatus)) {
                ps.setInt(1, idRequest);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    /**
     * Admin menolak permintaan top up.
     */
    public boolean rejectRequest(int idRequest) throws SQLException {
        String sql = "UPDATE topup_request SET status = 'DITOLAK', waktu_proses = NOW() " +
                     "WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRequest);
            return ps.executeUpdate() > 0;
        }
    }
}
