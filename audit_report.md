# 📋 Laporan Audit Proyek Akhir PBO — Sistem Tiket Kereta Api

> **Auditor:** QA Engineer & Dosen Penguji PBO  
> **Proyek:** `TiketKeretaApi` (Java Swing + MVC)  
> **Tanggal Audit:** 2 Juni 2026  
> **Total File Diperiksa:** 22 file Java

---

## 1. Ringkasan Arsitektur

### Peta Struktur Package

```
src/
├── tiketkeretaapi/     → Main entry point
├── controller/         → BookingController
├── model/
│   ├── Connector.java  → Konfigurasi DB
│   ├── entitas/        → Entitas data (User, Penumpang, Admin, Jadwal, Tiket) + DAO
│   └── layanan/        → Kereta (abstract), Subkelas, TransaksiOperasi (interface), DAO operasional
├── view/               → LoginFrame, AdminFrame, PenumpangFrame, MainFrame
└── exception/          → Custom Exceptions
```

### Evaluasi Keseluruhan Arsitektur

Secara umum, struktur MVC **sudah terbentuk dengan baik** dan memiliki pemisahan tanggung jawab yang jelas. Hirarki inheritance (`User → Admin/Penumpang`, `Kereta → KeretaEkonomi/Bisnis/Eksekutif`) diimplementasikan dengan benar. Konsep custom exception, transaksi ACID, dan penggunaan `PreparedStatement` secara konsisten menunjukkan pemahaman yang matang.

Namun, terdapat beberapa **pelanggaran arsitektur serius** dan **kelemahan signifikan** yang harus diperbaiki sebelum responsi.

---

## 2. Temuan Kritis (Bug / Pelanggaran Kriteria)

### 🔴 KRITIS-01 — Pelanggaran MVC: Logika Bisnis di dalam View
**File:** `PenumpangFrame.java`, Baris 408–413  
**Kategori:** Arsitektur MVC

```java
// ❌ PELANGGARAN: Method hitungHarga() ada di dalam kelas View
private double hitungHarga(double h, String kelas) {
    switch (kelas.toLowerCase()) {
        case "eksekutif": return h * 1.20;
        case "bisnis":    return h * 1.10;
        default:          return h;
    }
}
```

**Masalah:** `PenumpangFrame` (View) memiliki logika perhitungan harga sendiri (`hitungHarga`) yang duplikat dengan logika di `BookingController`. Ini adalah pelanggaran prinsip MVC — View **tidak boleh** mengandung logika bisnis apapun. Jika persentase tarif berubah, developer harus ubah di **dua tempat** (Controller + View).

---

### 🔴 KRITIS-02 — Pelanggaran MVC: DAO Diinstansiasi Langsung di View
**File:** `LoginFrame.java` Baris 23, `PenumpangFrame.java` Baris 47–52, `AdminFrame.java` Baris 30–33  
**Kategori:** Arsitektur MVC

```java
// ❌ LoginFrame.java — View menginstansiasi dan memanggil DAO langsung
private final TiketKeretaApiDAO authDAO;
public LoginFrame() {
    authDAO = new TiketKeretaApiDAO(); // ← DAO ada di View, bukan Controller
    ...
}
private void prosesLogin(ActionEvent e) {
    User user = authDAO.login(username, password); // ← Query DB dari View!
}

// ❌ PenumpangFrame.java — View memegang 5 DAO sekaligus
private final TiketKeretaApiDAO tiketDAO;
private final JadwalDAO jadwalDAO;
private final RiwayatDAO riwayatDAO;
private final UserDAO userDAO;
private final TopupDAO topupDAO;
```

**Masalah:** View seharusnya **tidak pernah** berbicara langsung ke DAO/Model. Semua akses ke database harus melalui Controller. `LoginFrame` tidak memiliki Controller sama sekali — logika login diletakkan di View.

---

### 🔴 KRITIS-03 — Singleton Connector Tidak Thread-Safe (Race Condition)
**File:** `model/Connector.java`, Baris 16–19  
**Kategori:** Multithreading / Resource Management

```java
// ❌ Singleton tanpa synchronization — Race Condition!
public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {   // ← Baris 17: Thread A & B bisa masuk bersamaan!
        connection = DriverManager.getConnection(URL, USER, PASS);
    }
    return connection;
}
```

**Masalah:** Pola Singleton ini **tidak thread-safe**. Jika dua thread memeriksa `connection == null` secara bersamaan (sebelum salah satu membuat koneksi), keduanya akan membuat `Connection` baru, menyebabkan koneksi lama bocor. Harus menggunakan `synchronized` atau pola double-checked locking.

---

### 🔴 KRITIS-04 — Kelas Tiket.java Kosong (Empty Class / Dead Code)
**File:** `model/entitas/Tiket.java`  
**Kategori:** Kualitas Kode

```java
// ❌ Kelas entitas tanpa atribut, konstruktor, getter, atau setter apapun!
public class Tiket {
    // ... completely empty ...
}
```

**Masalah:** Kelas `Tiket` yang mewakili entitas tabel `tiket` di database **tidak memiliki satu pun atribut**. Ini menunjukkan kelas ini dibuat tapi tidak pernah diselesaikan. Kelas entitas yang kosong adalah pelanggaran serius terhadap prinsip **Encapsulation** dan tidak berguna dalam arsitektur MVC.

---

### 🔴 KRITIS-05 — Dua Entry Point Aplikasi yang Bertentangan
**File:** `tiketkeretaapi/TiketKeretaApi.java` dan `view/MainFrame.java`  
**Kategori:** Arsitektur MVC

```java
// ❌ TiketKeretaApi.java → Menggunakan SystemLookAndFeel
UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));

// ❌ MainFrame.java → Menggunakan CrossPlatformLookAndFeel (Metal!)
UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
```

**Masalah:** Ada **dua kelas `main()`** yang keduanya menjalankan aplikasi dengan konfigurasi L&F berbeda. `MainFrame` juga salah nama — isinya bukan frame tapi entry point kedua. Ini membingungkan dan berpotensi dijalankan yang salah saat responsi, menghasilkan tampilan berbeda dari yang diharapkan.

---

### 🟠 SEDANG-01 — Tidak Ada Generic DAO / Interface Repository
**File:** Semua kelas DAO  
**Kategori:** Abstraction (Interface)

Semua kelas DAO (`UserDAO`, `JadwalDAO`, `RiwayatDAO`, `TopupDAO`) diimplementasikan secara **terpisah tanpa kontrak interface atau abstract class bersama**. Padahal sudah ada `TransaksiOperasi` sebagai contoh yang baik. Standar profesional dan kritera kuliah mengharuskan adanya interface DAO generik (misal `ICrudDAO<T>`) agar kode lebih abstrak dan mudah diuji.

---

### 🟠 SEDANG-03 — Resource Leak di `TopupDAO.approveRequest()`
**File:** `model/entitas/TopupDAO.java`, Baris 95–101  
**Kategori:** Resource Management

```java
// ❌ ResultSet tidak ditutup dengan try-with-resources
try (PreparedStatement ps = conn.prepareStatement(getReq)) {
    ps.setInt(1, idRequest);
    ResultSet rs = ps.executeQuery();   // ← rs dibuat di luar try-with-resources!
    if (!rs.next()) {
        conn.rollback();
        return false;
    }
    idUser = rs.getInt("id_user");
    jumlah = rs.getDouble("jumlah");
}
// rs tidak pernah ditutup secara eksplisit!
```

**Masalah:** `ResultSet rs` tidak pernah ditutup karena tidak dibungkus dalam `try-with-resources`. Jika terjadi exception sebelum `rs` dibaca habis, koneksi akan kebocoran resource. Ini inkonsisten dengan DAO-DAO lain yang sudah benar menggunakan try-with-resources.

---

### 🟠 SEDANG-04 — Silent Exception di Beberapa Metode View
**File:** `PenumpangFrame.java`, Baris 119, 222, 338, 346  
**Kategori:** Exception Handling

```java
// ❌ Exception ditangkap tapi hanya di-print, tidak ada notifikasi ke user
private void refreshSaldo() {
    try {
        ...
    } catch (SQLException ex) { ex.printStackTrace(); } // ← User tidak tahu ada error!
}
private void refreshRiwayat() {
    try { ... } catch (SQLException ex) { ex.printStackTrace(); }  // ← Idem
}
private void loadStasiunAsal() {
    try { ... } catch (SQLException ex) { ex.printStackTrace(); }  // ← Idem
}
private void loadStasiunTujuan() {
    try { ... } catch (SQLException ex) { ex.printStackTrace(); }  // ← Idem
}
```

**Masalah:** Empat method di `PenumpangFrame` menangkap `SQLException` tanpa memberikan notifikasi kepada user via `JOptionPane`. Dari sisi UI, aplikasi akan terlihat "berjalan normal" tapi data tidak tampil — ini adalah pengalaman pengguna yang sangat buruk dan menunjukkan penanganan error yang setengah-setengah.

---

### 🟡 MINOR-01 — Tidak Ada `toString()` pada Kelas Entitas
**File:** `model/entitas/User.java`, `model/entitas/Jadwal.java`, `model/entitas/Penumpang.java`  
**Kategori:** OOP (Polymorphism / Method Overriding)

Tidak ada satupun kelas entitas yang meng-override method `toString()`. Meskipun tidak wajib, override `toString()` adalah salah satu demonstrasi **Method Overriding (Polymorphism)** yang paling umum diuji.

---

### 🟡 MINOR-02 — Tidak Ada Multithreading Eksplisit
**File:** Seluruh proyek  
**Kategori:** Multithreading

Tidak ada implementasi `Runnable`/`Thread`/`SwingWorker` selain `SwingUtilities.invokeLater()` di `main()`. Untuk proyek kereta api, fitur seperti **live clock**, **auto-refresh otomatis**, atau **loading indicator** saat query berjalan (menggunakan `SwingWorker`) tidak diimplementasikan. Penggunaan hanya `invokeLater` sudah benar tapi kurang mendemonstrasikan penguasaan multithreading.

---

### 🟡 MINOR-03 — `comboJKelas` Menggunakan Kapitalisasi Berbeda dari Nilai DB
**File:** `AdminFrame.java`, Baris 181 vs `JadwalDAO.java`  
**Kategori:** Bug Potensial

```java
// AdminFrame: item di ComboBox ditulis dengan huruf kapital awal
comboJKelas = new JComboBox<>(new String[]{"Ekonomi", "Bisnis", "Eksekutif"});

// Namun di BookingController, perbandingan kelas menggunakan .toLowerCase()
case "eksekutif": ...
case "bisnis": ...
```

Jika di database nilai kolom `kelas` disimpan dengan format tertentu (misal `EKONOMI`), pemilihan dari `comboJKelas` bisa menghasilkan mismatch. Perlu dipastikan konsistensi nilai antara form, query, dan database.

---

## 3. Rekomendasi Perbaikan

### Perbaikan KRITIS-01 & KRITIS-02: Pisahkan Logika ke Controller

Buat `LoginController` terpisah, dan pindahkan `hitungHarga` keluar dari View.

**Langkah 1 — Buat `LoginController.java`:**
```java
// src/controller/LoginController.java
package controller;

import model.entitas.User;
import model.layanan.TiketKeretaApiDAO;
import java.sql.SQLException;

public class LoginController {
    private final TiketKeretaApiDAO authDAO = new TiketKeretaApiDAO();

    public User login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username dan Password tidak boleh kosong!");
        }
        return authDAO.login(username, password);
    }
}
```

**Langkah 2 — Refactor `LoginFrame.java`:**
```java
// View hanya memegang referensi Controller
private final LoginController loginController = new LoginController();

private void prosesLogin(ActionEvent e) {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());
    try {
        User user = loginController.login(username, password); // ← Lewat Controller
        if (user != null) {
            this.dispose();
            if ("ADMIN".equals(user.getRole())) {
                new AdminFrame(user).setVisible(true);
            } else {
                new PenumpangFrame((Penumpang) user).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Username atau Password salah!",
                "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    } catch (IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
            "Peringatan", JOptionPane.WARNING_MESSAGE);
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Kesalahan koneksi: " + ex.getMessage(),
            "Error Sistem", JOptionPane.ERROR_MESSAGE);
    }
}
```

**Langkah 3 — Hapus `hitungHarga()` dari `PenumpangFrame`:**
```java
// View memanggil Controller untuk preview harga, bukan menghitung sendiri
// Tambahkan method publik di BookingController:
public double getHargaPreview(double hargaDasar, String kelas) {
    switch (kelas.toLowerCase()) {
        case "eksekutif": return new KeretaEksekutif("", hargaDasar).hitungTotalTarif();
        case "bisnis":    return new KeretaBisnis("", hargaDasar).hitungTotalTarif();
        default:          return new KeretaEkonomi("", hargaDasar).hitungTotalTarif();
    }
}

// Di PenumpangFrame, ganti pemanggilan hitungHarga() dengan:
double harga = controller.getHargaPreview(j.getHargaDasar(), j.getKelas());
```

---

### Perbaikan KRITIS-03: Thread-Safe Singleton

```java
// src/model/Connector.java — Gunakan synchronized untuk keamanan thread
public class Connector {
    private static volatile Connection connection; // ← tambahkan volatile
    // ...

    public static synchronized Connection getConnection() throws SQLException { // ← synchronized
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }
}
```

---

### Perbaikan KRITIS-04: Lengkapi Kelas `Tiket.java`

```java
// src/model/entitas/Tiket.java — Implementasikan sebagai entitas lengkap
package model.entitas;

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
```

---

### Perbaikan KRITIS-05: Hapus Entry Point Ganda

Hapus method `main()` dari `MainFrame.java` atau ubah fungsinya. Jadikan `TiketKeretaApi.java` satu-satunya entry point.

```java
// src/view/MainFrame.java — Jadikan abstract base frame, bukan entry point
// Hapus method main() sepenuhnya dari file ini.
// MainFrame bisa diubah menjadi abstract class yang di-extend AdminFrame & PenumpangFrame
// untuk mengurangi duplikasi kode buildTable(), btn(), dll.
```

---

### Perbaikan SEDANG-03: Tutup ResultSet di `TopupDAO`

```java
// src/model/entitas/TopupDAO.java, method approveRequest()
// ❌ Sebelum (resource leak):
ResultSet rs = ps.executeQuery();
if (!rs.next()) { ... }

// ✅ Sesudah (try-with-resources):
try (PreparedStatement ps = conn.prepareStatement(getReq);
     ResultSet rs = ps.executeQuery()) {          // ← rs dimasukkan ke TWR
    ps.setInt(1, idRequest);
    if (!rs.next()) {
        conn.rollback();
        return false;
    }
    idUser = rs.getInt("id_user");
    jumlah = rs.getDouble("jumlah");
}
```

> [!WARNING]
> Urutan yang benar: `ps.setInt()` harus dipanggil **sebelum** `ps.executeQuery()`. Dalam versi yang ada, urutan sudah salah — `setInt` dipanggil setelah `executeQuery` ada di dalam try. Perbaikan di atas juga mengkoreksi urutan ini.

---

### Perbaikan SEDANG-04: Tambah Notifikasi User untuk Silent Exceptions

```java
// Ganti semua pola berikut:
catch (SQLException ex) { ex.printStackTrace(); }

// Dengan pola yang informatif untuk user:
catch (SQLException ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(this,
        "Gagal memuat data: " + ex.getMessage(),
        "Koneksi Bermasalah", JOptionPane.ERROR_MESSAGE);
}
```

---

### Bonus — Tambahkan Interface Generic DAO (Nilai Plus)

```java
// src/model/ICrudDAO.java — Interface DAO generik (Generic + Abstraction)
package model;

import java.sql.SQLException;
import java.util.List;

public interface ICrudDAO<T> {
    List<T> getAll() throws SQLException;
    boolean tambah(T entity) throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean hapus(int id) throws SQLException;
}

// Kemudian JadwalDAO mengimplementasikannya:
public class JadwalDAO implements ICrudDAO<Jadwal> { ... }
```

---

## 4. Kesiapan Responsi

### Tabel Penilaian Per Kriteria

| No | Kriteria | Bobot | Nilai | Catatan |
|----|----------|-------|-------|---------|
| 1 | **Struktur Proyek & Arsitektur MVC** | 20% | 13/20 | Package sudah benar, namun View mengandung logika bisnis dan akses DAO langsung |
| 2 | **5 Pilar OOP** | 25% | 20/25 | Inheritance, Polymorphism, Abstraction, Encapsulation sudah didemonstrasikan. Tiket.java kosong mengurangi nilai Encapsulation |
| 3 | **Koneksi DB & JDBC** | 20% | 17/20 | PreparedStatement konsisten, try-with-resources hampir di semua tempat, ada 1 resource leak di TopupDAO |
| 4 | **Exception Handling** | 15% | 10/15 | Custom exception bagus, tapi ada 4 silent exceptions di PenumpangFrame yang tidak diinformasikan ke user |
| 5 | **Multithreading** | 10% | 5/10 | Hanya `invokeLater` di entry point, tidak ada SwingWorker/Thread eksplisit |
| 6 | **UI/UX & Event Handling** | 10% | 9/10 | Layout Manager baik (tidak null layout), event handling dengan lambda, validasi input ada |

### Skor Total

$$\textbf{Nilai Estimasi: 74 / 100}$$

> [!IMPORTANT]
> **Syarat Lulus Responsi:** Skor ≥ 75. Proyek ini berada tepat di batas. Perbaiki minimal **KRITIS-01 (logika bisnis di View)**, **KRITIS-04 (Tiket.java kosong)**, dan **SEDANG-04 (silent exceptions)** untuk melewati ambang batas dengan aman.

---

### Ringkasan Status

| Prioritas | Temuan | Status |
|-----------|--------|--------|
| 🔴 Kritis | KRITIS-01: Logika bisnis `hitungHarga()` di View | ❌ Harus diperbaiki |
| 🔴 Kritis | KRITIS-02: DAO diakses langsung dari View | ❌ Harus diperbaiki |
| 🔴 Kritis | KRITIS-03: Singleton tidak thread-safe | ❌ Harus diperbaiki |
| 🔴 Kritis | KRITIS-04: `Tiket.java` kelas kosong | ❌ Harus diperbaiki |
| 🔴 Kritis | KRITIS-05: Dua entry point `main()` bertentangan | ❌ Harus diperbaiki |
| 🟠 Sedang | SEDANG-01: Tidak ada Generic DAO Interface | ⚠️ Disarankan |
| 🟠 Sedang | SEDANG-02: Password plaintext | ⚠️ Disarankan |
| 🟠 Sedang | SEDANG-03: Resource leak ResultSet di TopupDAO | ❌ Harus diperbaiki |
| 🟠 Sedang | SEDANG-04: Silent exceptions di PenumpangFrame | ❌ Harus diperbaiki |
| 🟡 Minor | MINOR-01: Tidak ada `toString()` di entitas | 💡 Nilai plus jika ditambah |
| 🟡 Minor | MINOR-02: Tidak ada implementasi Thread/SwingWorker | 💡 Nilai plus jika ditambah |
| 🟡 Minor | MINOR-03: Inkonsistensi kapitalisasi kelas kereta | ⚠️ Perlu dicek |

### Yang Sudah Sangat Baik ✅
- Seluruh query menggunakan `PreparedStatement` (tidak ada SQL Injection)
- Transaksi ACID di `PemesananDAO` dan `approveRequest` sangat baik
- Custom Exception (`KursiPenuhException`, `SaldoTidakCukupException`) diimplementasikan dengan tepat
- Hierarki `User → Admin/Penumpang` dan `Kereta → 3 subkelas` mendemonstrasikan OOP dengan benar
- `SwingUtilities.invokeLater()` digunakan di entry point
- Layout Manager dinamis (GridBagLayout, BorderLayout, GridLayout) — **tidak ada null layout**
- Validasi input ada di semua form kritis
