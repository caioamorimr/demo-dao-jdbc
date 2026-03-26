package model.dao.impl;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.sql.*;
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
                throw new DbException("Unexpected error. No rows affected");
            }

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeStatement(pstmt);
        }

    }

    @Override
    public void update(Seller seller) {

    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public Seller findById(Integer id) {
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
                Department department = instantiateDepartment(rs);
                return instantiateSeller(rs, department);
            }

            throw new DbException("Id not found: " + id);

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(pstmt);
        }
    }

    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setId(rs.getInt("DepartmentId"));
        department.setName(rs.getString("DepName"));
        return department;
    }

    private Seller instantiateSeller(ResultSet rs, Department department) throws SQLException {
        Seller seller = new Seller();
        seller.setId(rs.getInt("Id"));
        seller.setName(rs.getString("Name"));
        seller.setEmail(rs.getString("Email"));
        seller.setBaseSalary(rs.getDouble("BaseSalary"));
        seller.setBirthDate(rs.getDate("BirthDate").toLocalDate());
        seller.setDepartment(department);
        return seller;
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
                Department dept = departments.get(rs.getInt("DepartmentId"));
                if (dept == null) {
                    dept = instantiateDepartment(rs);
                    departments.put(rs.getInt("DepartmentId"), dept);
                }

                Seller seller = instantiateSeller(rs, dept);
                sellers.add(seller);
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
                Department dept = departments.get(rs.getInt("DepartmentId"));

                if (dept == null) {
                    dept = instantiateDepartment(rs);
                    departments.put(rs.getInt("DepartmentId"), dept);
                }

                Seller seller = instantiateSeller(rs, dept);
                sellers.add(seller);
            }

            return sellers;

        } catch (SQLException e) {
            throw new DbException(e.getMessage(), e);
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(pstmt);
        }
    }
}