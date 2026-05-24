package model.entitas;

public class Jadwal {
    private int id;
    private String namaKereta;
    private String kelas;
    private String stasiunAsal;
    private String stasiunTujuan;
    private double hargaDasar;
    private int kursiTersedia;
    private String keterangan;

    // Constructor, Getter, dan Setter
    public Jadwal(int id, String namaKereta, String kelas, String stasiunAsal, String stasiunTujuan, double hargaDasar, int kursiTersedia) {
        this(id, namaKereta, kelas, stasiunAsal, stasiunTujuan, hargaDasar, kursiTersedia, "");
    }

    public Jadwal(int id, String namaKereta, String kelas, String stasiunAsal, String stasiunTujuan, double hargaDasar, int kursiTersedia, String keterangan) {
        this.id = id;
        this.namaKereta = namaKereta;
        this.kelas = kelas;
        this.stasiunAsal = stasiunAsal;
        this.stasiunTujuan = stasiunTujuan;
        this.hargaDasar = hargaDasar;
        this.kursiTersedia = kursiTersedia;
        this.keterangan = keterangan != null ? keterangan : "";
    }

    public int getId() { return id; }
    public String getNamaKereta() { return namaKereta; }
    public String getKelas() { return kelas; }
    public String getStasiunAsal() { return stasiunAsal; }
    public String getStasiunTujuan() { return stasiunTujuan; }
    public double getHargaDasar() { return hargaDasar; }
    public int getKursiTersedia() { return kursiTersedia; }
    public String getKeterangan() { return keterangan; }
}