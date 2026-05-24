package model.layanan;

/**
 * Abstract class Kereta sebagai base class untuk semua jenis kereta.
 * Menerapkan pilar OOP: Abstraction dan Encapsulation.
 */
public abstract class Kereta {
    private String namaKereta;
    private double hargaDasar;

    public Kereta(String namaKereta, double hargaDasar) {
        this.namaKereta = namaKereta;
        this.hargaDasar = hargaDasar;
    }

    public String getNamaKereta() { return namaKereta; }
    public double getHargaDasar() { return hargaDasar; }

    /**
     * Setiap jenis kereta wajib mengimplementasikan cara menghitung total tarif.
     * Ini adalah penerapan Polymorphism.
     */
    public abstract double hitungTotalTarif();
}
