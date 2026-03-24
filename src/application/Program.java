package application;

import model.entities.Department;
import model.entities.Seller;

import java.time.LocalDate;

public class Program {
    public static void main(String[] args) {

        Department department = new Department(1, "Books");

        Seller seller = new Seller(21, "Bob", "bob@email.com", LocalDate.of(2000, 4, 23), 3000.0, department);

        System.out.println(seller);
    }
}
