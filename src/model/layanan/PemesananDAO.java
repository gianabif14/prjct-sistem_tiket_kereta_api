package model.layanan;

import model.Connector;
import exception.KursiPenuhException;
import exception.SaldoTidakCukupException;
import java.sql.*;
import java.util.UUID;

public class PemesananDAO {

    public String prosesPemesanan(int idUser, int idJadwal, double totalHarga) 
            throws SQLException, SaldoTidakCukupException, KursiPenuhException {
        
        Connection conn = null;
        String kodeBooking = "BOOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try {
            conn = Connector.getConnection();
            conn.setAutoCommit(false); // ACID: Mulai Transaksi

            // 1. Cek & Potong Saldo
            String updateSaldo = "UPDATE users SET saldo = saldo - ? WHERE id = ? AND saldo >= ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSaldo)) {
                ps.setDouble(1, totalHarga);
                ps.setInt(2, idUser);
                ps.setDouble(3, totalHarga);
                if (ps.executeUpdate() == 0) {
                    throw new SaldoTidakCukupException("Saldo E-Wallet Anda tidak mencukupi!");
                }
            }

            // 2. Kurangi Kursi
            String updateKursi = "UPDATE jadwal_kereta SET kursi_tersedia = kursi_tersedia - 1 WHERE id = ? AND kursi_tersedia > 0";
            try (PreparedStatement ps = conn.prepareStatement(updateKursi)) {
                ps.setInt(1, idJadwal);
                if (ps.executeUpdate() == 0) {
                    throw new KursiPenuhException("Mohon maaf, kursi telah habis terjual!");
                }
            }

            // 3. Terbitkan Tiket
            String insertTiket = "INSERT INTO tiket (id_user, id_jadwal, kode_booking, total_bayar) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertTiket)) {
                ps.setInt(1, idUser);
                ps.setInt(2, idJadwal);
                ps.setString(3, kodeBooking);
                ps.setDouble(4, totalHarga);
                ps.executeUpdate();
            }

            conn.commit(); // ACID: Commit jika semua sukses
            return kodeBooking;

        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // ACID: Rollback jika ada error/trigger menolak
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }
}