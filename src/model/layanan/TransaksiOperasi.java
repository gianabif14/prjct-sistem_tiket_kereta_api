package model.layanan;

/**
 * Interface TransaksiOperasi mendefinisikan kontrak operasi transaksi tiket.
 * Menerapkan pilar OOP: Abstraction melalui interface.
 */
public interface TransaksiOperasi {
    /**
     * Memproses pemesanan tiket.
     * @return String berformat "STATUS|PESAN"
     */
    String prosesTiket(int idUser, int idJadwal, double hargaDasar, String kelas);
}
