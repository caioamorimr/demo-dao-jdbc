package model.dao.impl;

import db.DB;
import db.DbException;
import db.DbIntegrityException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerDaoJDBC implements SellerDao {

    private final Connection conn;

    public SellerDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Seller seller) {
        if (seller == null) {
            throw new DbException("Seller must not be null");
        }
        if (seller.getDepartment() == null || seller.getDepartment().getId() == null) {
            throw new DbException("Seller's department and department ID must not be null");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(
                    "INSERT INTO seller " +
                            "(Name, Email, BirthDate, BaseSalary, DepartmentId) " +
                            "VALUES " +
                            "(?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            pstmt.setString(1, seller.getName());
            pstmt.setString(2, seller.getEmail());
            pstmt.setDate(3, Date.valueOf(seller.getBirthDate()));
            pstmt.setDouble(4, seller.getBaseSalary());
            pstmt.setInt(5, seller.getDepartment().getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    seller.setId(rs.getInt(1));
                }
                DB.closeResultSet(rs);
            } else {
                throw new DbException("Error while inserting seller");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DbIntegrityException("Email already exists. Please use another email.", e);
        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeStatement(pstmt);
        }
    }

    @Override
    public void update(Seller seller) {
        if (seller == null || seller.getId() == null) {
            throw new DbException("Seller and Seller ID must not be null");
        }
        if (seller.getDepartment() == null || seller.getDepartment().getId() == null) {
            throw new DbException("Seller's department and department ID must not be null");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(
                    "UPDATE seller " +
                            "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? " +
                            "WHERE Id = ?"
            );

            pstmt.setString(1, seller.getName());
            pstmt.setString(2, seller.getEmail());
            pstmt.setDate(3, Date.valueOf(seller.getBirthDate()));
            pstmt.setDouble(4, seller.getBaseSalary());
            pstmt.setInt(5, seller.getDepartment().getId());
            pstmt.setInt(6, seller.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DbException("Id not found: " + seller.getId());
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DbIntegrityException("Email already exists. Please use another email.", e);
        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeStatement(pstmt);
        }
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) {
            throw new DbException("Seller ID must not be null");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(
                    "DELETE FROM seller " +
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
    public Seller findById(Integer id) {
        if (id == null) {
            throw new DbException("Seller ID must not be null");
        }

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(
                    "SELECT seller.*, department.Name as DepName " +
                            "FROM seller INNER JOIN department " +
                            "ON seller.DepartmentId = department.Id " +
                            "WHERE seller.Id = ?"
            );

            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return instantiateSeller(rs, instantiateDepartment(rs));
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
    public List<Seller> findAll() {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(
                    "SELECT seller.*, department.Name as DepName " +
                            "FROM seller INNER JOIN department " +
                            "ON seller.DepartmentId = department.Id " +
                            "ORDER BY Name"
            );

            rs = pstmt.executeQuery();

            List<Seller> sellers = new ArrayList<>();
            Map<Integer, Department> departments = new HashMap<>();

            while (rs.next()) {
                final ResultSet currentRs = rs;
                Department dept = departments.computeIfAbsent(
                        rs.getInt("DepartmentId"),
                        k -> {
                            try {
                                return instantiateDepartment(currentRs);
                            } catch (SQLException e) {
                                throw new DbException(e.getMessage(), e);
                            }
                        }
                );
                sellers.add(instantiateSeller(rs, dept));
            }

            return sellers;

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(pstmt);
        }
    }

    @Override
    public List<Seller> findByDepartment(Department department) {
        if (department == null || department.getId() == null) {
            throw new DbException("Department and Department ID must not be null");
        }

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(
                    "SELECT seller.*, department.Name as DepName " +
                            "FROM seller INNER JOIN department " +
                            "ON seller.DepartmentId = department.Id " +
                            "WHERE DepartmentId = ? " +
                            "ORDER BY Name"
            );

            pstmt.setInt(1, department.getId());
            rs = pstmt.executeQuery();

            List<Seller> sellers = new ArrayList<>();
            Map<Integer, Department> departments = new HashMap<>();

            while (rs.next()) {
                final ResultSet currentRs = rs;
                Department dept = departments.computeIfAbsent(
                        rs.getInt("DepartmentId"),
                        k -> {
                            try {
                                return instantiateDepartment(currentRs);
                            } catch (SQLException e) {
                                throw new DbException(e.getMessage(), e);
                            }
                        }
                );
                sellers.add(instantiateSeller(rs, dept));
            }

            return sellers;

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(pstmt);
        }
    }

    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("DepartmentId"), rs.getString("DepName"));
    }

    private Seller instantiateSeller(ResultSet rs, Department department) throws SQLException {
        return new Seller(
                rs.getInt("Id"),
                rs.getString("Name"),
                rs.getString("Email"),
                rs.getDate("BirthDate").toLocalDate(),
                rs.getDouble("BaseSalary"),
                department
        );
    }
}