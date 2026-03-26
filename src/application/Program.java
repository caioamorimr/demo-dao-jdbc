package application;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {
            SellerDao sellerDao = DaoFactory.createSellerDao();

            System.out.println("-----TEST 1: seller findById-----");
            Seller seller = sellerDao.findById(3);
            System.out.println(seller);
            System.out.println();

            System.out.println("-----TEST 2: seller findByDepartment-----");
            Department department = new Department(2, null);
            List<Seller> sellers = sellerDao.findByDepartment(department);
            for (Seller s : sellers) {
                System.out.println(s);
            }
            System.out.println();

            System.out.println("-----TEST 3: seller findAll-----");
            sellers = sellerDao.findAll();
            for (Seller s : sellers) {
                System.out.println(s);
            }
            System.out.println();

            System.out.println("-----TEST 4: seller insert-----");
            seller = new Seller(null, "Greg", "greg@email.com", LocalDate.of(2000, 12, 12), 4000.0, department);
            sellerDao.insert(seller);
            System.out.println("Inserted seller successfully. Id: " + seller.getId());
            System.out.println();

            System.out.println("-----TEST 5: seller update-----");
            seller = sellerDao.findById(1);
            seller.setName("Martha");
            sellerDao.update(seller);
            System.out.println("Updated seller successfully. Id: " + seller.getId());
            System.out.println();

            System.out.println("-----TEST 6: seller delete-----");
            System.out.print("Enter id for delete test: ");
            int id = sc.nextInt();
            sellerDao.deleteById(id);
            System.out.println("Deleted completed");

        }

    }
}