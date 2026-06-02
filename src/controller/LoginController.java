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
