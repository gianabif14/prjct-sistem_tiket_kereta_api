package model.layanan;

/**
 * KeretaBisnis mewarisi Kereta dan menambah biaya layanan 10%.
 * Menerapkan pilar OOP: Inheritance dan Polymorphism.
 */
public class KeretaBisnis extends Kereta {

    private static final double BIAYA_LAYANAN_PERSEN = 0.10;

    public KeretaBisnis(String namaKereta, double hargaDasar) {
        super(namaKereta, hargaDasar);
    }

    /**
     * Tarif Bisnis = harga dasar + 10% biaya layanan.
     */
    @Override
    public double hitungTotalTarif() {
        return getHargaDasar() * (1 + BIAYA_LAYANAN_PERSEN);
    }
}
