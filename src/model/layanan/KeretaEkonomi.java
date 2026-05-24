package model.layanan;

/**
 * KeretaEkonomi mewarisi Kereta tanpa biaya tambahan.
 * Menerapkan pilar OOP: Inheritance dan Polymorphism.
 */
public class KeretaEkonomi extends Kereta {

    public KeretaEkonomi(String namaKereta, double hargaDasar) {
        super(namaKereta, hargaDasar);
    }

    /**
     * Tarif Ekonomi = harga dasar (tanpa biaya tambahan).
     */
    @Override
    public double hitungTotalTarif() {
        return getHargaDasar();
    }
}
