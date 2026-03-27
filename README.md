# demo-dao-jdbc

Projeto de demonstração do padrão **DAO (Data Access Object)** com **JDBC** em Java.  
Desenvolvido como material de estudo para entender como separar a lógica de acesso a dados das regras de negócio, utilizando uma estrutura simples e funcional.

## Objetivo

Este projeto implementa um sistema de cadastro de **vendedores** e **departamentos**, permitindo operações básicas de CRUD (Create, Read, Update, Delete) por meio de um menu interativo no console.  
Ele ilustra na prática:

- Conexão com banco de dados relacional (MySQL)
- Padrão DAO com interfaces e implementações JDBC
- Uso de `PreparedStatement`, `ResultSet` e tratamento de exceções
- Factory para obtenção das implementações de DAO
- Organização do código em camadas: entidades, DAOs, banco de dados e aplicação
- Validação de unicidade de e‑mail e tratamento amigável de erros

## Tecnologias

- **Java 17** (ou superior)
- **JDBC**
- **MySQL** (ou qualquer SGBD relacional com adaptações)
- **Git** para versionamento

> O projeto não usa frameworks como Spring; é JDBC puro.

## Estrutura do Projeto

```
demo-dao-jdbc/
├── src/
│   ├── application/          # Classe principal (Program.java) e menus
│   ├── db/                   # Conexão com o banco, exceções personalizadas
│   ├── model/
│   │   ├── dao/              # Interfaces DAO (SellerDao, DepartmentDao)
│   │   │   └── impl/         # Implementações JDBC das interfaces
│   │   └── entities/         # Classes de domínio (Seller, Department)
│   └── resources/            # Arquivos de configuração e SQL (opcional)
│       ├── db.properties     # Credenciais do banco (a partir do exemplo)
│       └── db/
│           └── schema.sql    # Script de criação do banco
├── .gitignore
└── README.md
```

> **Nota sobre a pasta `resources/`**:  
> Para que o `ClassLoader` encontre o arquivo `db.properties`, a pasta `resources/` deve estar no **classpath**.  
> - **No IntelliJ**: marque `src` como *Sources Root* e depois `src/resources` como *Resources Root*.  
> - **No Eclipse**: marque `src/resources` como *Source Folder*.  
> - **No terminal**: adicione a pasta ao classpath com `-cp "out;src/resources"` (Windows) ou `-cp "out:src/resources"` (Linux/Mac).  
> Se preferir simplificar, mova `db.properties` diretamente para `src/` (sem a pasta `resources`).

## Padrão DAO

O padrão **DAO** isola a lógica de acesso a dados em classes específicas.  
As interfaces (`SellerDao`, `DepartmentDao`) definem as operações, enquanto as implementações (`SellerDaoJDBC`, `DepartmentDaoJDBC`) contêm o código SQL e manipulação do `ResultSet`.  
A fábrica `DaoFactory` fornece as implementações concretas, centralizando a criação e facilitando futuras trocas (ex.: para JPA, Hibernate).

## Configuração e Execução

### 1. Pré‑requisitos

- JDK 17+ instalada
- MySQL Server em execução

### 2. Banco de Dados

Crie um banco de dados (ex.: `demo_dao`) e execute o script `src/resources/db/schema.sql` (ou o conteúdo abaixo):

```sql
CREATE DATABASE IF NOT EXISTS demo_dao;
USE demo_dao;

CREATE TABLE department (
    Id INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(60) NOT NULL
);

CREATE TABLE seller (
    Id INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(60) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    BirthDate DATE NOT NULL,
    BaseSalary DECIMAL(10,2) NOT NULL,
    DepartmentId INT NOT NULL,
    FOREIGN KEY (DepartmentId) REFERENCES department (Id)
);
```

### 3. Configurar credenciais

Na pasta `src/resources/` (ou `src/` se preferir), crie um arquivo `db.properties` a partir do exemplo `db.properties.example`:

```properties
# db.properties
user=SEU_USUARIO
password=SUA_SENHA
dburl=jdbc:mysql://localhost:3306/NOME_DO_BANCO?allowPublicKeyRetrieval=true&useSSL=false
```

Substitua os valores pelas suas credenciais e nome do banco.

### 4. Executar

Compile e execute a classe `Program` localizada em `src/application/Program.java`.  
O menu principal será exibido no terminal.

### 5. Usando o menu

Navegue pelas opções digitando o número correspondente e pressionando **Enter**.  
- **Seller operations**: CRUD completo para vendedores, inclusive busca por departamento.  
- **Department operations**: CRUD para departamentos.  
- **Exit**: encerra o programa.

## Funcionalidades

### Seller
- `findById` – buscar vendedor pelo ID
- `findAll` – listar todos os vendedores
- `findByDepartment` – listar vendedores de um departamento
- `insert` – cadastrar novo vendedor (com validação de e‑mail único)
- `update` – atualizar dados de um vendedor (campos opcionais)
- `deleteById` – remover vendedor (com verificação de integridade)

### Department
- `findById` – buscar departamento pelo ID
- `findAll` – listar todos os departamentos
- `insert` – cadastrar novo departamento
- `update` – atualizar nome do departamento
- `deleteById` – remover departamento (falha se existirem vendedores vinculados)

## Exceções e Tratamento

O projeto define duas exceções personalizadas:
- `DbException` – erros gerais de banco (conexão, SQL, etc.)
- `DbIntegrityException` – violação de integridade referencial (ex.: tentar excluir um departamento que possui vendedores) **e violação de unicidade** (e‑mail duplicado).

Exemplo:
```
Error: Email already exists. Please use another email.
```

## Dependências

- **MySQL Connector/J** – driver JDBC para MySQL.  
  Baixe o JAR em [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/) e adicione ao classpath.
