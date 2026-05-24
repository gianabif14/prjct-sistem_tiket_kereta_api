package controller;

import model.layanan.PemesananDAO;
import model.layanan.TransaksiOperasi;
import exception.KursiPenuhException;
import exception.SaldoTidakCukupException;
import model.layanan.KeretaEksekutif;
import model.layanan.KeretaEkonomi;
import model.layanan.KeretaBisnis;

import java.sql.SQLException;

/**
 * BookingController memisahkan logika bisnis dari tampilan (View).
 * Mengimplementasikan interface TransaksiOperasi (Polymorphism melalui interface).
 */
public class BookingController implements TransaksiOperasi {

    private final PemesananDAO pemesananDAO;

    public BookingController() {
        this.pemesananDAO = new PemesananDAO();
    }

    /**
     * Memproses logika pemesanan tiket dari input View.
     * Mengembalikan nilai String berformat "STATUS|PESAN" untuk dibaca oleh View.
     */
    @Override
    public String prosesTiket(int idUser, int idJadwal, double hargaDasar, String kelas) {
        try {
            double hargaFinal;

            // --- Logika Bisnis: Polymorphism berdasarkan kelas kereta ---
            switch (kelas.toLowerCase()) {
                case "eksekutif":
                    hargaFinal = new KeretaEksekutif("temp", hargaDasar).hitungTotalTarif();
                    break;
                case "bisnis":
                    hargaFinal = new KeretaBisnis("temp", hargaDasar).hitungTotalTarif();
                    break;
                default: // ekonomi
                    hargaFinal = new KeretaEkonomi("temp", hargaDasar).hitungTotalTarif();
                    break;
            }

            // --- Eksekusi Transaksi ACID ke DAO ---
            String kodeBooking = pemesananDAO.prosesPemesanan(idUser, idJadwal, hargaFinal);

            return "SUKSES|Pemesanan Berhasil!\nKode Booking: " + kodeBooking
                    + "\nTotal Bayar: Rp " + String.format("%,.2f", hargaFinal);

        } catch (SaldoTidakCukupException | KursiPenuhException e) {
            return "GAGAL|" + e.getMessage();
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR|Kesalahan Sistem Database: " + e.getMessage();
        }
    }
}