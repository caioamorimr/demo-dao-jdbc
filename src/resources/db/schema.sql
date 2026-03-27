CREATE DATABASE IF NOT EXISTS demo_dao;
USE demo_dao;

-- Tabela department
CREATE TABLE department
(
    Id   INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(60) NOT NULL
);

-- Tabela seller
CREATE TABLE seller
(
    Id           INT PRIMARY KEY AUTO_INCREMENT,
    Name         VARCHAR(60)    NOT NULL,
    Email        VARCHAR(100)   NOT NULL UNIQUE,
    BirthDate    DATE           NOT NULL,
    BaseSalary   DECIMAL(10, 2) NOT NULL,
    DepartmentId INT            NOT NULL,
    FOREIGN KEY (DepartmentId) REFERENCES department (Id)
);