package application;

import db.DbException;
import db.DbIntegrityException;
import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Program {

    private static final Scanner sc = new Scanner(System.in);
    private static final SellerDao sellerDao = DaoFactory.createSellerDao();
    private static final DepartmentDao departmentDao = DaoFactory.createDepartmentDao();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMainMenu();
            int option = readInt("Option: ");
            System.out.println();
            switch (option) {
                case 1 -> sellerMenu();
                case 2 -> departmentMenu();
                case 0 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
        sc.close();
        System.out.println("Goodbye!");
    }

    // -------------------------------------------------------------------------
    // MENUS
    // -------------------------------------------------------------------------

    private static void printMainMenu() {
        System.out.println("========================================");
        System.out.println("           DEMO DAO JDBC - MENU         ");
        System.out.println("========================================");
        System.out.println("  1. Seller operations");
        System.out.println("  2. Department operations");
        System.out.println("  0. Exit");
        System.out.println("========================================");
    }

    private static void sellerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- SELLER ---");
            System.out.println("  1. Find by ID");
            System.out.println("  2. Find all");
            System.out.println("  3. Find by Department");
            System.out.println("  4. Insert");
            System.out.println("  5. Update");
            System.out.println("  6. Delete by ID");
            System.out.println("  0. Back");
            int option = readInt("Option: ");
            System.out.println();
            switch (option) {
                case 1 -> sellerFindById();
                case 2 -> sellerFindAll();
                case 3 -> sellerFindByDepartment();
                case 4 -> sellerInsert();
                case 5 -> sellerUpdate();
                case 6 -> sellerDelete();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }

    private static void departmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- DEPARTMENT ---");
            System.out.println("  1. Find by ID");
            System.out.println("  2. Find all");
            System.out.println("  3. Insert");
            System.out.println("  4. Update");
            System.out.println("  5. Delete by ID");
            System.out.println("  0. Back");
            int option = readInt("Option: ");
            System.out.println();
            switch (option) {
                case 1 -> departmentFindById();
                case 2 -> departmentFindAll();
                case 3 -> departmentInsert();
                case 4 -> departmentUpdate();
                case 5 -> departmentDelete();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }

    // -------------------------------------------------------------------------
    // SELLER OPERATIONS
    // -------------------------------------------------------------------------

    private static void sellerFindById() {
        int id = readInt("Enter seller ID: ");
        try {
            Seller seller = sellerDao.findById(id);
            System.out.println("Seller found:");
            System.out.println(seller);
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sellerFindAll() {
        try {
            List<Seller> sellers = sellerDao.findAll();
            if (sellers.isEmpty()) {
                System.out.println("No sellers found.");
            } else {
                System.out.println("All sellers (" + sellers.size() + "):");
                sellers.forEach(System.out::println);
            }
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sellerFindByDepartment() {
        int deptId = readInt("Enter department ID: ");
        try {
            List<Seller> sellers = sellerDao.findByDepartment(new Department(deptId));
            if (sellers.isEmpty()) {
                System.out.println("No sellers found for department ID " + deptId + ".");
            } else {
                System.out.println("Sellers in department " + deptId + " (" + sellers.size() + "):");
                sellers.forEach(System.out::println);
            }
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sellerInsert() {
        System.out.println("Enter new seller data:");
        sc.nextLine();
        String name = readString("Name: ");
        String email = readString("Email: ");
        LocalDate birthDate = readDate("Birth date (dd/MM/yyyy): ");
        double salary = readDouble("Base salary: ");
        int deptId = readInt("Department ID: ");

        Seller seller = new Seller(name, email, birthDate, salary, new Department(deptId));
        try {
            sellerDao.insert(seller);
            System.out.println("Seller inserted successfully. Generated ID: " + seller.getId());
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sellerUpdate() {
        int id = readInt("Enter seller ID to update: ");
        try {
            Seller seller = sellerDao.findById(id);
            System.out.println("Current data: " + seller);
            System.out.println("Enter new data (press Enter to keep current value):");
            sc.nextLine();

            String name = readStringOrDefault("New name [" + seller.getName() + "]: ", seller.getName());
            String email = readStringOrDefault("New email [" + seller.getEmail() + "]: ", seller.getEmail());
            LocalDate birthDate = readDateOrDefault(
                    "New birth date [" + seller.getBirthDate().format(DATE_FMT) + "]: ",
                    seller.getBirthDate()
            );
            double salary = readDoubleOrDefault(
                    "New base salary [" + seller.getBaseSalary() + "]: ",
                    seller.getBaseSalary()
            );
            int deptId = readIntOrDefault(
                    "New department ID [" + seller.getDepartment().getId() + "]: ",
                    seller.getDepartment().getId()
            );

            seller.setName(name);
            seller.setEmail(email);
            seller.setBirthDate(birthDate);
            seller.setBaseSalary(salary);
            seller.setDepartment(new Department(deptId));

            sellerDao.update(seller);
            System.out.println("Seller updated successfully.");
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void sellerDelete() {
        int id = readInt("Enter seller ID to delete: ");
        try {
            sellerDao.deleteById(id);
            System.out.println("Seller deleted successfully.");
        } catch (DbIntegrityException e) {
            System.out.println("Integrity error: " + e.getMessage());
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // DEPARTMENT OPERATIONS
    // -------------------------------------------------------------------------

    private static void departmentFindById() {
        int id = readInt("Enter department ID: ");
        try {
            Department department = departmentDao.findById(id);
            System.out.println("Department found:");
            System.out.println(department);
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void departmentFindAll() {
        try {
            List<Department> departments = departmentDao.findAll();
            if (departments.isEmpty()) {
                System.out.println("No departments found.");
            } else {
                System.out.println("All departments (" + departments.size() + "):");
                departments.forEach(System.out::println);
            }
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void departmentInsert() {
        sc.nextLine();
        String name = readString("Department name: ");
        Department department = new Department(name);
        try {
            departmentDao.insert(department);
            System.out.println("Department inserted successfully. Generated ID: " + department.getId());
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void departmentUpdate() {
        int id = readInt("Enter department ID to update: ");
        try {
            Department department = departmentDao.findById(id);
            System.out.println("Current data: " + department);
            sc.nextLine();
            String name = readStringOrDefault("New name [" + department.getName() + "]: ", department.getName());
            department.setName(name);
            departmentDao.update(department);
            System.out.println("Department updated successfully.");
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void departmentDelete() {
        int id = readInt("Enter department ID to delete: ");
        try {
            departmentDao.deleteById(id);
            System.out.println("Department deleted successfully.");
        } catch (DbIntegrityException e) {
            System.out.println("Integrity error (department may have sellers): " + e.getMessage());
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // INPUT HELPERS
    // -------------------------------------------------------------------------

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    private static int readIntOrDefault(String prompt, int defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid value. Keeping current.");
            return defaultValue;
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static double readDoubleOrDefault(String prompt, double defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid value. Keeping current.");
            return defaultValue;
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static String readStringOrDefault(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(sc.nextLine().trim(), DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use dd/MM/yyyy.");
            }
        }
    }

    private static LocalDate readDateOrDefault(String prompt, LocalDate defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return defaultValue;
        try {
            return LocalDate.parse(input, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid format. Keeping current.");
            return defaultValue;
        }
    }
}