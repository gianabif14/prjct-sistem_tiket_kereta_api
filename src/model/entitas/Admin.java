package model.entitas;

public class Admin extends User {
    public Admin(int id, String username) {
        super(id, username);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}

