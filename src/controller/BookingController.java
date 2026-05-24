package controller;

import model.layanan.PemesananDAO;
import exception.KursiPenuhException;
import exception.SaldoTidakCukupException;
import model.layanan.KeretaEksekutif;
import model.layanan.KeretaEkonomi;

import java.sql.SQLException;

public class BookingController {
    
    // Controller memanggil DAO untuk urusan interaksi database
    private final PemesananDAO pemesananDAO;

    public BookingController() {
        // Instansiasi DAO
        this.pemesananDAO = new PemesananDAO();
    }

    /**
     * Memproses logika pemesanan tiket dari input View.
     * Mengembalikan nilai String berformat "STATUS|PESAN" untuk dibaca oleh JOptionPane di View.
     */
    public String prosesTiket(int idUser, int idJadwal, double hargaDasar, String kelas) {
        try {
            double hargaFinal = hargaDasar;
            
            // --- Logika Bisnis: Polymorphism berdasarkan kelas kereta ---
            // Jika Anda memiliki class KeretaBisnis, bisa ditambahkan di sini
            if ("Eksekutif".equalsIgnoreCase(kelas)) {
                // Misal: Eksekutif dikenakan biaya tambahan layanan
                KeretaEksekutif keretaEx = new KeretaEksekutif("Temp", hargaDasar);
                hargaFinal = keretaEx.hitungTotalTarif();
            } else if ("Ekonomi".equalsIgnoreCase(kelas)) {
                KeretaEkonomi keretaEko = new KeretaEkonomi("Temp", hargaDasar);
                hargaFinal = keretaEko.hitungTotalTarif();
            }

            // --- Eksekusi Transaksi ACID ke DAO ---
            // pemesananDAO akan mengembalikan kode booking jika sukses
            String kodeBooking = pemesananDAO.prosesPemesanan(idUser, idJadwal, hargaFinal);
            
            // Mengembalikan string sukses dengan delimiter "|"
            return "SUKSES|Pemesanan Berhasil!\nKode Booking: " + kodeBooking + "\nTotal Bayar: Rp " + hargaFinal;

        } catch (SaldoTidakCukupException | KursiPenuhException e) {
            // Ditangkap dari Exception kustom jika saldo e-wallet kurang
            return "GAGAL|" + e.getMessage();
            
        }
        // Ditangkap jika kapasitas kursi di database sudah 0
         catch (SQLException e) {
            // Ditangkap jika ada error pada koneksi atau query MySQL
            e.printStackTrace(); // Tampilkan di console IDE untuk debugging
            return "ERROR|Kesalahan Sistem Database: " + e.getMessage();
        }
    }
}