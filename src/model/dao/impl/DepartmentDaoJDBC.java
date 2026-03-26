package model.dao.impl;

import db.DB;
import db.DbException;
import db.DbIntegrityException;
import model.dao.DepartmentDao;
import model.entities.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDaoJDBC implements DepartmentDao {

    private final Connection conn;

    public DepartmentDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Department department) {
        if (department == null) {
            throw new DbException("Department must not be null");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(
                    "INSERT INTO department " +
                            "(Name) " +
                            "VALUES " +
                            "(?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            pstmt.setString(1, department.getName());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    department.setId(rs.getInt(1));
                }
                DB.closeResultSet(rs);
            } else {
                throw new DbException("Error while inserting department");
            }

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeStatement(pstmt);
        }
    }

    @Override
    public void update(Department department) {
        if (department == null || department.getId() == null) {
            throw new DbException("Department and Department ID must not be null");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(
                    "UPDATE department " +
                            "SET Name = ? " +
                            "WHERE Id = ?"
            );

            pstmt.setString(1, department.getName());
            pstmt.setInt(2, department.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DbException("Id not found: " + department.getId());
            }

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeStatement(pstmt);
        }
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) {
            throw new DbException("Department ID must not be null");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(
                    "DELETE FROM department " +
                            "WHERE Id = ?"
            );

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DbException("Id not found: " + id);
            }

        } catch (SQLException e) {
            throw new DbIntegrityException(e.getMessage(), e);
        } finally {
            DB.closeStatement(pstmt);
        }
    }

    @Override
    public Department findById(Integer id) {
        if (id == null) {
            throw new DbException("Department ID must not be null");
        }

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(
                    "SELECT Id, Name " +
                            "FROM department " +
                            "WHERE Id = ?"
            );

            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return instantiateDepartment(rs);
            }

            throw new DbException("Id not found: " + id);

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(pstmt);
        }
    }

    @Override
    public List<Department> findAll() {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(
                    "SELECT Id, Name " +
                            "FROM department " +
                            "ORDER BY Name"
            );

            rs = pstmt.executeQuery();

            List<Department> departments = new ArrayList<>();

            while (rs.next()) {
                departments.add(instantiateDepartment(rs));
            }

            return departments;

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(pstmt);
        }
    }

    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("Id"), rs.getString("Name"));
    }
}