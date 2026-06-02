package model;

import java.sql.SQLException;
import java.util.List;

public interface InterfaceCrudDAO<T> {
    List<T> getAll() throws SQLException;
    boolean tambah(T entity) throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean hapus(int id) throws SQLException;
}
