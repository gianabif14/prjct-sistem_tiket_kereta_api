/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.entitas;

/**
 *
 * @author giana
 */
public class Tiket {
    private int id;
    private int idUser;
    private int idJadwal;
    private String kodeBooking;
    private double totalBayar;
    private String waktuPesan;

    public Tiket(int id, int idUser, int idJadwal,
                 String kodeBooking, double totalBayar, String waktuPesan) {
        this.id = id;
        this.idUser = idUser;
        this.idJadwal = idJadwal;
        this.kodeBooking = kodeBooking;
        this.totalBayar = totalBayar;
        this.waktuPesan = waktuPesan;
    }

    public int getId()             { return id; }
    public int getIdUser()         { return idUser; }
    public int getIdJadwal()       { return idJadwal; }
    public String getKodeBooking() { return kodeBooking; }
    public double getTotalBayar()  { return totalBayar; }
    public String getWaktuPesan()  { return waktuPesan; }

    @Override
    public String toString() {
        return "Tiket[" + kodeBooking + ", Rp" + totalBayar + "]";
    }
}
