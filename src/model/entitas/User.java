package model.entitas;

// Pilar OOP: Abstraction
public abstract class User {
    
    // Pilar OOP: Encapsulation (menggunakan private modifiers)
    private int id;
    private String username;

    // Constructor
    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getter untuk ID
    public int getId() {
        return id;
    }

    // Getter untuk Username
    public String getUsername() {
        return username;
    }

    // Setter untuk Username (opsional, jika fitur edit profil dibutuhkan)
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Abstract method ini memaksa kelas turunannya (Admin / Penumpang)
     * untuk mendefinisikan role-nya masing-masing.
     * Ini adalah penerapan pilar Polymorphism (Method Overriding).
     */
    public abstract String getRole();
}