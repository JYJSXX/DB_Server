package DB_Server;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class db_server {
    static boolean Mode = true;
    final static String[] tables =
            {"Bank", "Customer", "Password", "Account", "Department", "Employee", "Loan", "Authentication", "Password"};
    final static String[] tables_reverse =
            {"Authentication", "Password", "Loan", "Employee", "Department", "Account", "Password", "Customer", "Bank"};
    static Connection connection = null;
    public static void db_server() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:mysql://127.0.0.1:3306/lab2";
        String username = "root";
        String password = "zHbXw8966.jyjs";

//        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, username, password);
            if (connection != null) {
                System.out.println("Connection to database successful!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            if (!check()) {
                System.out.println("Database is not correct!" + '\n' +
                        "Whether quit or force to create a new database? (q/f)");
                String input = System.console().readLine();
                if (input.equals("f")) {
                    drop_all();
                    create_all();
                } else return;
            }
            System.out.println("Database is correct!");
        }
//        InsertTable("Customer", new String[]{"1", "'Tom'", "'Shanghai'", "'123456789'", "'20223311@qq.com'"});
    }

    private static void drop_all(){
        for (String table : tables_reverse) {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate("DROP TABLE if exists " + table);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    1. **银行信息 (Bank)**
    - `BankID` (主键)
    - `BName`
    - `BAddress`
    - `Bmail`
    - `BTel`
2. **客户信息 (Customer)**
    - `CustomerID` (主键)
    - `CustomerName`
    - `CAddress`
    - `CTel`
    - `Cmail`
3. **账户信息 (Account)**
    - `AccountID` (主键)
    - `AccountType`
    - `Balance`
    - `CustomerID` (外键，引用 Customer)
    - `BankID` (外键，引用 Bank)
   - `LoanID` (外键，引用Loan)
4. **贷款信息 (Loan)**
    - `LoanID` (主键)
    - `Amount`
    - `InterestRate`
    - `LoanDate`
    - `Request Date`
    - `Status`
    - `AccountID` (外键，引用 Customer)
    - `Employee ID` (外键，引用 Employee)
    - `BankID` (外键，引用 Bank)
5. **银行部门信息 (Department)**
    - `DepartmentID` (主键)
    - `DName`
    - `BankID` (外键，引用 Bank)
6. **员工信息 (Employee)**
    - `EmployeeID` (主键)
    - `EName`
    - `Email`
    - `Salary`
    - `DepartmentID` (外键，引用 Department)
     */

    private static void create_all(){
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE Bank (" +
                    "BankID INT PRIMARY KEY AUTO_INCREMENT," +
                    "BankName VARCHAR(255) NOT NULL," +
                    "BankAddress VARCHAR(255) NOT NULL ," +
                    "BankMail VARCHAR(255) NOT NULL ," +
                    "BankTel VARCHAR(255) NOT NULL )");



            statement.executeUpdate("CREATE TABLE Customer (" +
                    "CustomerID VARCHAR(32) PRIMARY KEY," +
                    "CustomerName VARCHAR(255) NOT NULL ," +
                    "CustomerAddress VARCHAR(255)," +
                    "CustomerTel VARCHAR(255)," +
                    "CustomerMail VARCHAR(255))");




            statement.executeUpdate("CREATE TABLE Account (" +
                    "AccountID INT PRIMARY KEY AUTO_INCREMENT," +
                    "AccountName VARCHAR(255) UNIQUE NOT NULL ," +
                    "AccountType VARCHAR(255) NOT NULL ," +
                    "Balance DOUBLE DEFAULT 0," +
//                    "CustomerID INT," +
                    "BankID INT," +
//                    "FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)," +
                    "FOREIGN KEY (BankID) REFERENCES Bank(BankID))");

            statement.executeUpdate("CREATE TABLE Authentication(" +
                    "AccountID INT PRIMARY KEY, " +
                    "CustomerID VARCHAR(32) REFERENCES Customer(CustomerID))");

            statement.executeUpdate("CREATE TABLE Password (" +
                    "AccountID INT PRIMARY KEY AUTO_INCREMENT," +
                    "Password VARCHAR(255)," +
                    "FOREIGN KEY (AccountID) REFERENCES Account(AccountID))");



            statement.executeUpdate("CREATE TABLE Department (" +
                    "DepartmentID INT PRIMARY KEY AUTO_INCREMENT," +
                    "DepartmentName VARCHAR(255)," +
                    "BankID INT," +
                    "FOREIGN KEY (BankID) REFERENCES Bank(BankID))");


            statement.executeUpdate("CREATE TABLE Employee (" +
                    "EmployeeID INT PRIMARY KEY AUTO_INCREMENT," +
                    "EmployeeName VARCHAR(255)," +
                    "EmployeeMail VARCHAR(255)," +
                    "Salary DOUBLE," +
                    "DepartmentID INT," +
                    "FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID))");

            statement.executeUpdate("CREATE TABLE Loan (" +
                    "LoanID INT PRIMARY KEY AUTO_INCREMENT," +
                    "Amount DOUBLE," +
                    "InterestRate DOUBLE," +
                    "LoanDate DATE," +
                    "RequestDate DATE," +
                    "Status INT," +
                    "AccountID INT," +
                    "EmployeeID INT," +
                    "BankID INT," +
                    "FOREIGN KEY (AccountID) REFERENCES Account(AccountID)," +
                    "FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID)," +
                    "FOREIGN KEY (BankID) REFERENCES Bank(BankID))");


            statement.executeUpdate("INSERT INTO Bank (BankName, BankAddress, Bankmail, BankTel)" +
                    " VALUES ('SmallBank', 'Shanghai', 'SmallBank@mail.bank.com', '123456789')");

            statement.executeUpdate("INSERT INTO Bank (BankName, BankAddress, Bankmail, BankTel)" +
                    " VALUES ('MediumBank', 'Beijing', 'MediumBank@mail.bank.com', '123456789')");

            //气笑了 怎么合肥报Typo
            statement.executeUpdate("INSERT INTO Bank (BankName, BankAddress, Bankmail, BankTel)" +
                    " VALUES ('BigBank', 'Hefei', 'BigBank@mail.bank.com', '123456789')");

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean check(){
        //check if Table Bank exists
        for (String table : tables) {
            try {
                Statement statement = connection.createStatement();
                statement.executeQuery("SELECT * FROM " + table);
            } catch (SQLException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean InsertTable(String table, String[] columns, String[] values) {
        try {
            Statement statement = connection.createStatement();
            var sql = "INSERT INTO " + table + "(" + String.join(", ", columns) + ")"
                    + " VALUES (" + String.join(", ", values) + ")";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.print(e.getErrorCode());
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Query the database SIMPLY
     *
     * @param json a map of the query
     * @table the table to query
     * @columns the columns to return
     * @conditions the conditions to query
     * @return
     */
    public static Map<String, Object> simpleQuery(Map<String, Object> json){
        var table = (String) json.get("table");
        var ret_columns = (String[]) json.get("columns");
        var conditions = (String[]) json.get("conditions");
        var res = new HashMap<String, Object>();
        try {
            Statement statement = connection.createStatement();
            var sql = "SELECT " + String.join(", ", ret_columns)
                    + " FROM " + table + " WHERE " + String.join(" AND ", conditions);
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                for (String column : ret_columns) {
                    res.put(column, resultSet.getObject(column));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }

//    private static List<String> Obj2String(Object obj){
//        var temp = (List<?>) obj;
//        List<String> res = List.of();
//        for (Object o : temp) {
//            if(o instanceof String){
//                res.add((String) o);
//            }
//            else{
//                res.add(o.toString());
//            }
//        }
//        return res;
//    }

    public static Map<String, Object> CheckAuthentication(int AccountID){
        Map<String,Object> res = new HashMap<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM Authentication WHERE AccountID = " + AccountID);
            if(resultSet.next()) {
                res.put("CustomerID", resultSet.getString("CustomerID"));
                res.put("AccountID", AccountID);
                res.put("isAuthenticated", true);
            }
            else{
                res.put("AccountID", AccountID);
                res.put("isAuthenticated", false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.put("AccountID", AccountID);
            res.put("isAuthenticated", false);
        }
        return res;
    }
}
