package model.entitas;

// Pilar OOP: Inheritance (mewarisi sifat dari class User)
public class Penumpang extends User {
    
    // Pilar OOP: Encapsulation (atribut khusus untuk Penumpang)
    private double saldoWallet;

    // Constructor
    public Penumpang(int id, String username, double saldoWallet) {
        // Memanggil constructor dari parent class (User)
        super(id, username);
        
        // Memvalidasi saldo saat inisialisasi awal
        setSaldoWallet(saldoWallet);
    }

    // Getter untuk saldo E-Wallet
    public double getSaldoWallet() {
        return saldoWallet;
    }

    // Setter untuk saldo dengan logika validasi (Encapsulation)
    public void setSaldoWallet(double saldoWallet) {
        if (saldoWallet < 0) {
            throw new IllegalArgumentException("Saldo E-Wallet tidak boleh kurang dari 0!");
        }
        this.saldoWallet = saldoWallet;
    }

    // Pilar OOP: Polymorphism (Method Overriding dari abstract class User)
    @Override
    public String getRole() {
        return "PENUMPANG";
    }
}