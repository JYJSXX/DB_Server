package DB_Server;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class db_server {
    static boolean Mode = true;
    final static String[] tables =
            {"Bank", "Customer", "Password", "Account", "Department", "Employee", "Loan", "Authentication", "Password", "ePassword"};
    final static String[] tables_reverse =
            {"ePassword", "Authentication", "Password", "Loan", "Employee", "Department", "Account", "Password", "Customer", "Bank"};
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
//            if (!check()) {
//                System.out.println("Database is not correct!" + '\n' +
//                        "Whether quit or force to create a new database? (q/f)");
//                String input = System.console().readLine();
//                if (input.equals("f")) {
                    drop_all();
                    create_all();
//                } else return;
//            }
            System.out.println("Database is correct!");
        }
//        InsertTable("Customer", new String[]{"1", "'Tom'", "'Shanghai'", "'123456789'", "'20223311@qq.com'"});
    }

    private static void drop_all(){
        for (String table : tables_reverse) {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate("DROP TABLE if exists " + table);
                statement.executeUpdate("DROP TRIGGER if exists UpdateBalance");
                statement.executeUpdate("DROP PROCEDURE if exists RepayLoan");
                statement.executeUpdate("DROP FUNCTION if exists CalculateInterestRate");
                statement.executeUpdate("DROP PROCEDURE if exists CancelLoan");
                statement.executeUpdate("DROP PROCEDURE if exists ConfirmLoan");
                statement.executeUpdate("DROP PROCEDURE if exists DenyLoan");
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
//                    "CustomerAddress VARCHAR(255)," +
                    "CustomerTel VARCHAR(255)," +
                    "CustomerMail VARCHAR(255))");




            statement.executeUpdate("CREATE TABLE Account (" +
                    "AccountID INT PRIMARY KEY AUTO_INCREMENT," +
                    "AccountName VARCHAR(255) UNIQUE NOT NULL ," +
                    "AccountAvatar LONGTEXT," +
                    "AccountType VARCHAR(255) NOT NULL ," +
                    "Balance DOUBLE DEFAULT 0," +
                    "BankID INT," +
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
                    "EmployeeAvatar LONGTEXT, " +
                    "EmployeeMail VARCHAR(255)," +
                    "EmployeeTel VARCHAR(255)," +
                    "Salary DOUBLE," +
                    "DepartmentID INT," +
                    "FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID))");

            statement.executeUpdate("CREATE TABLE ePassword (" +
                    "EmployeeID INT PRIMARY KEY AUTO_INCREMENT," +
                    "Password VARCHAR(255)," +
                    "FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID))");

            statement.executeUpdate("CREATE TABLE Loan (" +
                    "LoanID INT PRIMARY KEY AUTO_INCREMENT," +
                    "Amount DOUBLE NOT NULL ," +
                    "InterestRate DOUBLE," +
                    "LoanDate DATE," +
                    "RequestDate DATE," +
                    "Status INT DEFAULT -1," +
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

            //每有一个贷款(Loan.ID由0变为1时)，更新Account的Balance
            statement.executeUpdate("CREATE TRIGGER UpdateBalance AFTER UPDATE ON Loan " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "IF OLD.Status = -1 AND NEW.Status = 0 THEN " +
                    "UPDATE Account SET Balance = Balance + NEW.Amount WHERE AccountID = NEW.AccountID;" +
                    "END IF;" +
                    "END");

            //为每一个银行注册部门
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department11', 1)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department12', 1)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department13', 1)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department21', 2)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department22', 2)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department23', 2)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department31', 3)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department32', 3)");
            statement.executeUpdate("INSERT INTO Department (DepartmentName, BankID) " +
                    "VALUES ('department33', 3)");

            //注册root账号
            statement.executeUpdate("SET GLOBAL log_bin_trust_function_creators = true;");
            statement.executeUpdate(
                    "INSERT INTO Employee Values(1, 'root', '', 'yu12345@mail.ustc.edu.cn', '123456', 100000, 1); "
            );
            statement.executeUpdate(
                    "INSERT INTO ePassword Values(1, 'root'); " );
            statement.executeUpdate(
                    "INSERT INTO Customer VALUES (1, 'testCustomer', '12332111223','test@mail.test');");
            statement.executeUpdate(
                    "INSERT INTO Account Values(1, 'test', '', 'Checking Account', 100000000, 1); ");
            statement.executeUpdate("INSERT INTO Password VALUES (1, 'test')");
            statement.executeUpdate("INSERT INTO Authentication VALUES(1, 1)");
//            statement.executeUpdate(
//                    "INSERT INTO Customer Values('1', 'root', 'root', 'root', 'root'); ");
            var sql = "CREATE FUNCTION CalculateInterestRate (AccountID INT, Amount DOUBLE) RETURNS DOUBLE " +
                    "BEGIN " +
                    "SELECT SUM(Loan.Amount) INTO @local FROM Loan " +
                        "WHERE Loan.AccountID = AccountID AND (Loan.Status = 0 or Loan.Status = -1);" +
                    "IF @local IS NULL THEN SET @local = 0;" +
                    "END IF;" +
                    "SET @total = @local + Amount;" +
                    "IF @total < 100000 THEN SET @res = 0.005;" +
                    "ELSEIF @total < 1000000 THEN SET @res = 0.01;" +
                    "ELSEIF @total < 10000000 THEN SET @res = 0.015;" +
                    "ELSE SET @res=0.02;" +
                    "END IF;" +
                    "RETURN @res;" +
                    "END ";
            System.out.println(sql);
            statement.executeUpdate(sql);
            //用户还款
            statement.executeUpdate(
                    """
CREATE PROCEDURE RepayLoan (
    IN loan_id INT
)
BEGIN
    DECLARE loan_amount DOUBLE;
    DECLARE loan_status int;
    DECLARE account_id INT;
    DECLARE _balance INT;
    DECLARE interest_rate DOUBLE;
    DECLARE pay DOUBLE;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 获取贷款金额和账户ID
    SELECT Amount, AccountID, Status, InterestRate INTO loan_amount, account_id, loan_status, interest_rate
    FROM Loan
    WHERE LoanID = loan_id
    FOR UPDATE;

    -- 检查贷款是否存在
    IF loan_amount IS NULL THEN
        ROLLBACK ;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loan not found';
    END IF;
    
    -- 检查贷款状态
    IF loan_status <> 0 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Loan can\\'t repay';
    END IF;
    
    -- 获取账户余额
    SELECT Balance INTO _balance
    FROM Account
    WHERE AccountID = account_id
    FOR UPDATE;

    
    SET pay = loan_amount * (1+interest_rate);
    
    IF pay > _balance THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45002' SET MESSAGE_TEXT = 'Balance not enough';
    END IF;
    
    -- 更新贷款状态
    UPDATE Loan
    SET Status = 1
    WHERE LoanID = loan_id;
    
    -- 更新账户余额
    UPDATE Account
    SET Balance = _balance - pay
    WHERE AccountID = account_id;
    
    -- 提交事务
    COMMIT;
END
            """);

            statement.executeUpdate("""
CREATE PROCEDURE CancelLoan (
    IN loan_id INT
)
BEGIN
    DECLARE loan_status INT;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 获取贷款状态
    SELECT Status INTO loan_status
    FROM Loan
    WHERE LoanID = loan_id
    FOR UPDATE;

    -- 检查贷款是否存在
    IF loan_status IS NULL THEN
        ROLLBACK ;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loan not found';
    END IF;
    
    -- 检查贷款状态
    IF loan_status <> -1 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Loan can\\'t cancel';
    END IF;

    -- 删除此条贷款
    DELETE FROM Loan
    WHERE LoanID = loan_id;
    
    -- 提交事务
    COMMIT;
END
            """);

            statement.executeUpdate("""
CREATE PROCEDURE ConfirmLoan (
    IN loan_id INT,
    IN employee_id INT
)
BEGIN
    DECLARE loan_status INT;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 获取贷款状态
    SELECT Status INTO loan_status
    FROM Loan
    WHERE LoanID = loan_id
    FOR UPDATE;

    -- 检查贷款是否存在
    IF loan_status IS NULL THEN
        ROLLBACK ;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loan not found';
    END IF;
    
    -- 检查贷款状态
    IF loan_status <> -1 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Loan can\\'t confirm';
    END IF;

    -- 更新贷款状态
    UPDATE Loan
    SET Status = 0, EmployeeID = employee_id
    WHERE LoanID = loan_id;
    
    -- 提交事务
    COMMIT;
END
            
            """);

            statement.executeUpdate("""
CREATE PROCEDURE DenyLoan (
    IN loan_id INT,
    IN employee_id INT
)
BEGIN
    DECLARE loan_status INT;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 获取贷款状态
    SELECT Status INTO loan_status
    FROM Loan
    WHERE LoanID = loan_id
    FOR UPDATE;

    -- 检查贷款是否存在
    IF loan_status IS NULL THEN
        ROLLBACK ;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Loan not found';
    END IF;
    
    -- 检查贷款状态
    IF loan_status <> -1 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Loan can\\'t confirm';
    END IF;

    -- 更新贷款状态
    UPDATE Loan
    SET Status = -2, EmployeeID = employee_id
    WHERE LoanID = loan_id;
    
    -- 提交事务
    COMMIT;
END
            """);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void RepayLoan(int LoanID){
        try {
            var sql = "CALL RepayLoan(" + LoanID + ")";
            var statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            if(e.getErrorCode() == 45002)
                throw new DatabaseException(DatabaseExceptionType.BALANCE_NOT_ENOUGH, e.getMessage() + " IN RepayLoan");
            else if (e.getErrorCode() == 45001) {
                throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_VALID, e.getMessage() + " IN RepayLoan");
            } else
                throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, e.getMessage() + " IN RepayLoan");
        }
    }

    public static void CancelLoan(int LoanID){
        try {
            var sql = "CALL CancelLoan(" + LoanID + ")";
            var statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            if(e.getErrorCode() == 45001)
                throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_VALID, e.getMessage() + " IN CancelLoan");
            else if(e.getErrorCode() == 45000)
                throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_FOUND, e.getMessage() + " IN CancelLoan");
            else
                throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, e.getMessage() + " IN CancelLoan");
        }
    }

    public static void ConfirmLoan(int LoanID, int EmployeeID){
        try {
            var sql = "CALL ConfirmLoan(" + LoanID + ", " + EmployeeID + ")";
            var statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            if(e.getErrorCode() == 45001)
                throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_VALID, e.getMessage() + " IN ConfirmLoan");
            else if(e.getErrorCode() == 45000)
                throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_FOUND, e.getMessage() + " IN ConfirmLoan");
            else
                throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, e.getMessage() + " IN ConfirmLoan");
        }
    }

    public static void DenyLoan(int LoanID, int EmployeeID){
        try {
            var sql = "CALL DenyLoan(" + LoanID + ", " + EmployeeID + ")";
            var statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            if(e.getErrorCode() == 45001)
                throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_VALID, e.getMessage() + " IN DenyLoan");
            else if(e.getErrorCode() == 45000)
                throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_FOUND, e.getMessage() + " IN DenyLoan");
            else
                throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, e.getMessage() + " IN DenyLoan");
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

    public static void InsertTable(String table, String[] columns, String[] values) {
        try {
            Statement statement = connection.createStatement();
            var sql = "INSERT INTO " + table + "(" + String.join(", ", columns) + ")"
                    + " VALUES (" + String.join(", ", values) + ")";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.print(e.getErrorCode());
            throw new DatabaseException(DatabaseExceptionType.INSERT_ERROR, e.getMessage());
//            e.printStackTrace();
//            return false;
        }
//        return true;
    }

    public static int InsertTable(String table, String[] columns, String[] values, int num) {
        try {
            var sql = "INSERT INTO " + table + "(" + String.join(", ", columns) + ")"
                    + " VALUES (" + String.join(", ", values) + ")";
            var preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            var res = preparedStatement.executeUpdate();
            ResultSet resultSet;
            if(res > 0){
                resultSet = preparedStatement.getGeneratedKeys();
                if(resultSet.next()) return resultSet.getInt(1);
                else throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, " In InsertTable ");
            }
            else throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, " In InsertTable ");
        } catch (SQLException e) {
            System.out.print(e.getErrorCode());
            throw new DatabaseException(DatabaseExceptionType.INSERT_ERROR, e.getMessage());
//            e.printStackTrace();
//            return false;
        }
//        return true;
    }


    /**
     * Query the database SIMPLY
     *
     * @param json a map of the query
     * @table the table to query
     * @columns the columns to return
     * @conditions the conditions to query
     * @return a map of the query result and count as its length
     * @exception DatabaseException if the query failed
     */
    public static Map<String, Object> simpleQuery(Map<String, Object> json){
        var table = (String) json.get("table");
        var ret_columns = (String[]) json.get("columns");
        var conditions = (String[]) json.get("conditions");
        var res = new HashMap<String, Object>();
        try {
            Statement statement = connection.createStatement();
            var sql = "SELECT " + String.join(", ", ret_columns)
                    + " FROM " + table;
            if(conditions != null){
                sql += " WHERE " + String.join(" AND ", conditions);
            }
            ResultSet resultSet = statement.executeQuery(sql);

            int count = 0;
            while (resultSet.next()) {
                count++;
                if (ret_columns.length == 1 && ret_columns[0].equals("*")) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    ret_columns = new String[metaData.getColumnCount()];
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        ret_columns[i] = metaData.getColumnName(i + 1);
                    }
                }
                for (String column : ret_columns) {
                    var temp = (Object[]) res.get(column);
                    temp = temp == null ? new Object[0] : temp;
                    temp = Arrays.copyOf(temp, temp.length + 1);
                    temp[temp.length - 1] = resultSet.getObject(column);
                    res.put(column, temp);
                }
            }
            res.put("count", count);
            if(count == 0){
                if(ret_columns.length == 1 && ret_columns[0].equals("*")){
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    ret_columns = new String[metaData.getColumnCount()];
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        ret_columns[i] = metaData.getColumnName(i + 1);
                    }
                }
                for(String column : ret_columns){
                    res.put(column, new Object[0]);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
//            return null;
            throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, "In Simple Query"+e.getMessage());
        }
        return res;
    }

    public static void UpdateTable(Map<String, Object> json){
        var table = (String) json.get("table");
        var columns = (String[]) json.get("columns");
        var values = (String[]) json.get("values");
        var conditions = (String[]) json.get("conditions");
        try {
            Statement statement = connection.createStatement();
            var sql = "UPDATE " + table + " SET " + String.join(", ", columns) + " = " + String.join(", ", values);
            if(conditions != null){
                sql += " WHERE " + String.join(" AND ", conditions);
            }
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, "In Update Table"+e.getMessage());
        }
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
    public double GetInterestRate(int AccountID, double Amount){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT CalculateInterestRate(" + AccountID + ", " + Amount + ")"
            );
            var res = resultSet.next();
            if(res) return resultSet.getDouble(1);
            else return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void SetLoanDate(int LoanID, java.util.Date Due){
        java.sql.Date LoanDate = new java.sql.Date(new java.util.Date().getTime());
        //一年之后
        java.sql.Date RequestDate = new java.sql.Date(LoanDate.getTime() + 1000L * 60 * 60 * 24 * 365);
        String sql = "UPDATE Loan SET LoanDate = ?, RequestDate = ? WHERE LoanID = " + LoanID;
        try {
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDate(1, LoanDate);
            preparedStatement.setDate(2, RequestDate);
            preparedStatement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException(DatabaseExceptionType.UPDATE_LOAN_DATE_ILLEGALLY, e.getMessage()
                    + " IN SetLoanDate");
        }
    }
}
