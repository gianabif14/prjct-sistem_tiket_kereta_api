package model.layanan;

/**
 * KeretaEksekutif mewarisi Kereta dan menambah biaya layanan 20%.
 * Menerapkan pilar OOP: Inheritance dan Polymorphism.
 */
public class KeretaEksekutif extends Kereta {

    private static final double BIAYA_LAYANAN_PERSEN = 0.20;

    public KeretaEksekutif(String namaKereta, double hargaDasar) {
        super(namaKereta, hargaDasar);
    }

    /**
     * Tarif Eksekutif = harga dasar + 20% biaya layanan.
     */
    @Override
    public double hitungTotalTarif() {
        return getHargaDasar() * (1 + BIAYA_LAYANAN_PERSEN);
    }
}
