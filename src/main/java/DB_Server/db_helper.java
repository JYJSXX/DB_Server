package DB_Server;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class db_helper {
    private db_server db = null;
    final Map<String, String[]> Column_map = Map.of(
        "Account", new String[]{"AccountName", "AccountType", "BankID"},
        "Password", new String[]{"AccountID", "password"},
        "Authentication", new String[]{"AccountID", "CustomerID"}
    );
    public void init(){
        db = new db_server();
        db.db_server();
    }

    /**
     * Register a new account
     * @param AccountName the name of the account
     * @param password the password of the account
     * @param AccountType the type of the account
     * @param BankID the ID of the bank
     */
    public void Register(String AccountName, String password, String AccountType, int BankID) {
        if(!isValidPassword(password)) throw new DatabaseException(DatabaseExceptionType.INVALID_PASSWORD, password);
        var res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"AccountID"},
                "conditions", new String[]{"AccountName = '" + AccountName + "'"})).get("count");
        if((int)res > 0) throw new DatabaseException(DatabaseExceptionType.INVALID_ACCOUNT_NAME, AccountName);
        try {
            db.InsertTable("Account", Column_map.get("Account"),
                    new String[]{surround(AccountName), surround(AccountType), BankID + ""});
        } catch (Exception e) {
            throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, AccountName);
        }
        var res1 = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"AccountID"},
                "conditions", new String[]{"AccountName = '" + AccountName + "'"}));
        var count = res1.get("count");
        if(count.equals(0)) throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, AccountName);
        String AccountID = ((Object[]) res1.get("AccountID"))[0].toString();
        db.InsertTable("Password", Column_map.get("Password"), new String[]{AccountID,surround(password)});
    }

    /**
     * Register a new employee
     * @param EmployeeName the name of the employee
     * @param password the password of the employee
     * @param DepartmentID the ID of the department
     */
    public void Register(String EmployeeName, String password, int DepartmentID, double Salary){
        if(!isValidPassword(password)) throw new DatabaseException(DatabaseExceptionType.INVALID_PASSWORD, password);
        var res = db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"EmployeeID"},
                "conditions", new String[]{"EmployeeName = '" + EmployeeName + "'"})).get("count");
        if((int)res > 0) throw new DatabaseException(DatabaseExceptionType.INVALID_ACCOUNT_NAME, EmployeeName);
        try {
            db.InsertTable("Employee", new String[]{"EmployeeName", "DepartmentID", "Salary"},
                    new String[]{surround(EmployeeName), DepartmentID + "", Salary + ""});
        } catch (Exception e) {
            throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, EmployeeName);
        }
        var res1 = db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"EmployeeID"},
                "conditions", new String[]{"EmployeeName = '" + EmployeeName + "'"}));
        var count = res1.get("count");
        if(count.equals(0)) throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, EmployeeName);
        String EmployeeID = ((Object[]) res1.get("EmployeeID"))[0].toString();
        db.InsertTable("ePassword", new String[]{"EmployeeID", "password"}, new String[]{EmployeeID, surround(password)});
    }

    private static String surround(String src){
        return "'" + src + "'";
    }

    private static boolean isValidPassword(String password) {
        // 定义密码合法性的正则表达式
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[_.]).{8,16}$";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);

        // 匹配密码
        return pattern.matcher(password).matches();
    }

    /**
     * Check if the user is authenticated
     * @param AccountID
     * @return map
     * <ul>
     *      <li>CustomerID:int</li>
     *      <li>AccountID:int</li>
     *      <li>isAuthenticated:boolean</li>
     *
     * </ul>
     */
    public Map<String, Object> checkAuthenticated(int AccountID){
//        boolean isAuthenticated = true;
        if(AccountID < 0) {
            throw new DatabaseException(DatabaseExceptionType.INVALID_ACCOUNT_NAME, AccountID + "");
        }
        return db_server.CheckAuthentication(AccountID);
    }

    /**
     * Authenticate the user
     * @param AccountID the ID of the user
     * @param CustomerName the name of the customer
     * @param CustomerID the ID of the customer
     */
    public void Authenticate(int AccountID, String CustomerName, String CustomerID){
        if(CheckIDNumber(CustomerName, CustomerID)){
            Map<String, Object> json = new HashMap<>();
            json.put("table", "Customer");
            json.put("columns", new String[]{"CustomerID"});
            json.put("conditions", new String[]{"CustomerID = " + surround(CustomerID)});
            var res = db_server.simpleQuery(json);
            var count = res.get("count");
            assert res != null;
            if(count.equals(0)){
                db.InsertTable("Customer", new String[]{"CustomerName", "CustomerID"},
                        new String[]{surround(CustomerName), surround(CustomerID)});
            }
            res = checkAuthenticated(AccountID);
            if(res.get("isAuthenticated").equals(false)){
                db.InsertTable("Authentication", new String[]{"AccountID", "CustomerID"},
                        new String[]{String.valueOf(AccountID), surround(CustomerID)});
            } else if (!res.get("CustomerID").equals(CustomerID)) {
                throw new DatabaseException(DatabaseExceptionType.INVALID_Customer_WITH_ID, CustomerName + CustomerID);
            }
        }
        else throw new DatabaseException(DatabaseExceptionType.INVALID_Customer_WITH_ID, CustomerName + CustomerID);
    }

    private boolean CheckIDNumber(String CustomerName, String CustomerID_Number){
        //check if the ID number is valid
        return (CustomerID_Number.length() == 18) && (!CustomerName.isEmpty());
    }

    /**
     * Get the list of the bank
     * @return a map contains the list of the bank
     */
    public Map<String, Object> getBankList(){
        return db.simpleQuery(Map.of("table", "Bank", "columns", new String[]{"BankName"}));
//        assert res != null;
//        return O2S((Object[]) res.get("BankName"));
    }
    public Map<String, Object> getDepartmentList(int BankID){
        return db.simpleQuery(Map.of("table", "Department", "columns", new String[]{"*"},
                "conditions", new String[]{"BankID = " + BankID}));
    }

    public Map<String, Object> getDepartmentList(){
        return db.simpleQuery(Map.of("table", "Department", "columns", new String[]{"*"}));
    }

    /**
     * Get the detail of the Employee
     * @param DepartmentID the ID of the Employee
     * @return a map contains the Department&Bank of the Employee
     */
    public Map<String, Object> getEDB(int DepartmentID){
        return db.simpleQuery(Map.of("table", "Department", "columns", new String[]{"*"},
                "conditions", new String[]{"DepartmentID = " + DepartmentID}));
    }

    private String[] O2S(Object[] objs){
        String[] res = new String[objs.length];
        for(int i = 0; i < objs.length; i++){
            res[i] = (String) objs[i];
        }
        return res;
    }

    /**
     * Login
     * @param AccountName the name of the account
     * @param password the password of the account
     * @return a map contains the detail of the user
     */
    public Map<String, Object> Login(String AccountName, String password){
        var res = db_server.simpleQuery(Map.of("table", "Account", "columns", new String[]{"AccountID"},
                "conditions", new String[]{"AccountName = '" + AccountName + "'"}));
        var count = res.get("count");
        if(count.equals(0)) {
            var Employee_res = db_server.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"EmployeeID"},
                    "conditions", new String[]{"EmployeeName = '" + AccountName + "'"}));
            var Employee_count = Employee_res.get("count");
            if(Employee_count.equals(0))
                throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, AccountName);
            int EmployeeID = (int) ((Object[]) Employee_res.get("EmployeeID"))[0];
            Employee_res = db_server.simpleQuery(Map.of("table", "ePassword", "columns", new String[]{"password"},
                    "conditions", new String[]{"EmployeeID = " + EmployeeID}));
            count = Employee_res.get("count");
            if(count.equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, AccountName);
            String password_in_db = ((Object[]) Employee_res.get("password"))[0].toString();
            if(!password_in_db.equals(password))
                throw new DatabaseException(DatabaseExceptionType.ACCOUNT_PASSWORD_NOT_MATCH, AccountName);
            var ret = db_server.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"*"},
                    "conditions", new String[]{"EmployeeID = " + EmployeeID}));
            if(ret.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, AccountName);
            ret.put("isCustomer", false);
            return ret;
        }
        int AccountID = (int) ((Object[]) res.get("AccountID"))[0];
        res = db_server.simpleQuery(Map.of("table", "Password", "columns", new String[]{"password"},
                "conditions", new String[]{"AccountID = " + AccountID}));
        count = res.get("count");
        if(count.equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, AccountName);
        String password_in_db = ((Object[]) res.get("password"))[0].toString();
        if(!password_in_db.equals(password))
            throw new DatabaseException(DatabaseExceptionType.ACCOUNT_PASSWORD_NOT_MATCH, AccountName);
        var ret = db_server.simpleQuery(Map.of("table", "Account", "columns", new String[]{"*"},
                "conditions", new String[]{"AccountID = " + AccountID}));
        if(ret.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, AccountName);
        ret.put("isCustomer", true);
        return ret;
    }

    /**
     * Get the account name of the user
     * @param AccountID the ID of the user
     * @return the account name of the user
     */
    private String getAccountName(int AccountID){
        var res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"AccountName"},
                "conditions", new String[]{"AccountID = " + AccountID}));
        if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, AccountID + " !!in private!!");
        return ((Object[]) res.get("AccountName"))[0].toString();
    }

    /**
     * Get the interest rate of the loan
     * @param AccountID the ID of the user
     * @param Amount the amount of the loan
     * @return the interest rate of the loan
     */
    public double getInterestRate(int AccountID, double Amount){
        var AccountName = getAccountName(AccountID);
        if(checkAuthenticated(AccountID).get("isAuthenticated").equals(false))
            throw new DatabaseException(DatabaseExceptionType.NOT_AUTHENTICATED, "AccountName: " + AccountName);
        var res = db.GetInterestRate(AccountID, Amount);
        if(res == -1) throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, AccountName + "in getInterestRate");
        return res;
    }

    /**
     * Get the list of the loan
     * @param AccountID the ID of the user
     * @param isCustomer true if the user is a customer
     * @return a map contains the list of the loan of the Customer or all the loan if the user is an employee
     */
    public Map<String, Object> getLoanList(int AccountID, boolean isCustomer){
        Map<String, Object> res = null;
        if (isCustomer) {
            var AccountName = getAccountName(AccountID);
            if(checkAuthenticated(AccountID).get("isAuthenticated").equals(false))
                throw new DatabaseException(DatabaseExceptionType.NOT_AUTHENTICATED, "AccountName: " + AccountName);
            res = db.simpleQuery(Map.of("table", "Loan", "columns", new String[]{"*"},
                    "conditions", new String[]{"AccountID = " + AccountID}));
        }
        else {
            var EmployeeID = AccountID;
            var Employee_res = db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"EmployeeID"},
                    "conditions", new String[]{"EmployeeID = " + EmployeeID}));
            if(Employee_res.get("count").equals(0))
                throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, EmployeeID + " in getLoanList");
            res = db.simpleQuery(Map.of("table", "Loan", "columns", new String[]{"*"}));
        }
        return res;
    }

    /**
     * Cancel a loan
     * @param LoanID the ID of the loan
     */
    public void repayLoan(int LoanID){
        var res = db.simpleQuery(Map.of("table", "Loan", "columns", new String[]{"AccountID", "Amount"},
                "conditions", new String[]{"LoanID = " + LoanID}));
        if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_FOUND, LoanID + "");
        db.RepayLoan(LoanID);
    }

    /**
     * Cancel a loan
     * @param LoanID the ID of the loan
     */
    public void cancelLoan(int LoanID){
        var res = db.simpleQuery(Map.of("table", "Loan", "columns", new String[]{"AccountID", "Amount"},
                "conditions", new String[]{"LoanID = " + LoanID}));
        if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_FOUND, LoanID + "");
        db.CancelLoan(LoanID);
    }

    /**
     * Confirm a loan
     * @param LoanID the ID of the loan
     * @param EmployeeID the ID of the employee
     */
    public void confirmLoan(int LoanID, int EmployeeID){
        var res = db.simpleQuery(Map.of("table", "Loan", "columns", new String[]{"AccountID", "Amount"},
                "conditions", new String[]{"LoanID = " + LoanID}));
        if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_FOUND, LoanID + "");

        res = db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"EmployeeID"},
                "conditions", new String[]{"EmployeeID = " + EmployeeID}));
        if(res.get("count").equals(0))
            throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, EmployeeID + " in confirmLoan");
        db.ConfirmLoan(LoanID, EmployeeID);
    }

    /**
     * Deny a loan
     * @param LoanID the ID of the loan
     * @param EmployeeID the ID of the employee
     */
    public void denyLoan(int LoanID, int EmployeeID){
        var res = db.simpleQuery(Map.of("table", "Loan", "columns", new String[]{"AccountID", "Amount"},
                "conditions", new String[]{"LoanID = " + LoanID}));
        if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.LOAN_NOT_FOUND, LoanID + "");

        res = db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"EmployeeID"},
                "conditions", new String[]{"EmployeeID = " + EmployeeID}));
        if(res.get("count").equals(0))
            throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, EmployeeID + " in confirmLoan");
        db.DenyLoan(LoanID, EmployeeID);
    }

    /**
     * Apply for a loan
     * @param amount the amount of the loan
     * @param AccountID the ID of the user
     */
    public void applyLoan(int AccountID, double amount, int Duration){
        if(amount <= 0) throw new DatabaseException(DatabaseExceptionType.INVALID_LOAN_AMOUNT, amount + "");
        var accQ = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"AccountID"},
                "conditions", new String[]{"AccountID = " + AccountID}));
        if(accQ.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, AccountID + "");
        var EDB = getEDB(AccountID);
        int bankID = ((Object[]) EDB.get("BankID"))[0] == null ? -1 : (int) ((Object[]) EDB.get("BankID"))[0];
        if(bankID == -1) throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, AccountID + "");
        double interestRate = getInterestRate(AccountID, amount);
        int LoanID = db.InsertTable("Loan", new String[]{"AccountID", "Amount", "InterestRate, BankID"},
                new String[]{AccountID + "", amount + "", interestRate + "", bankID + ""},Duration);
        db.SetLoanDate(LoanID, new Date());
    }

    /**
     * Get the detail of the user
     * @param AccountID the ID of the user
     * @param isCustomer true if the user is a customer
     * @return a map contains the detail of the user
     */
    public Map<String, Object> getUserDetail(int AccountID, boolean isCustomer){
        if(isCustomer){
            var resA =  db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"*"},
                    "conditions", new String[]{"AccountID = " + AccountID}));
            var CustomerID = db.simpleQuery(Map.of("table", "Authentication", "columns", new String[]{"CustomerID"},
                    "conditions", new String[]{"AccountID = " + AccountID}));
            if(CustomerID.get("count").equals(0)) {
                resA.put("isAuthenticated", false);
                return resA;
            }
            else resA.put("isAuthenticated", true);
            var resC = db.simpleQuery(Map.of("table", "Customer", "columns", new String[]{"*"},
                    "conditions", new String[]{"CustomerID = " + surround(((Object[]) CustomerID.get("CustomerID"))[0].toString())}));
            resA.putAll(resC);
            return resA;
        } else {
            return db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"*"},
                    "conditions", new String[]{"EmployeeID = " + AccountID}));
        }
    }

    /**
     * Modify the mail of the user
     * @param isCustomer true if the user is a customer
     * @param ID the ID of the user (AccountID or EmployeeID)
     * @param Mail the new mail
     */
    public void modifyMail(boolean isCustomer, int ID, String Mail){
        if(isCustomer){
            var res = db.simpleQuery(Map.of("table", "Authentication", "columns", new String[]{"CustomerID"},
                    "conditions", new String[]{"AccountID = " + ID}));
            if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.NOT_AUTHENTICATED, ID + "");
            db.UpdateTable(Map.of("table", "Customer", "columns", new String[]{"CustomerMail"},
                    "values", new String[]{surround(Mail)}, "conditions", new String[]{"CustomerID = " + ID}));
        }
        else {
            db.UpdateTable(Map.of("table", "Employee", "columns", new String[]{"EmployeeMail"},
                    "values", new String[]{surround(Mail)}, "conditions", new String[]{"EmployeeID = " + ID}));
        }
    }

    /**
     * Modify the telephone number of the user
     * @param isCustomer: if the user is a customer
     * @param ID: the ID of the user (AccountID or EmployeeID)
     * @param Tel: the new telephone number
     */
    public void modifyTel(boolean isCustomer, int ID, String Tel){
        if(isCustomer){
            var res = db.simpleQuery(Map.of("table", "Authentication", "columns", new String[]{"CustomerID"},
                    "conditions", new String[]{"AccountID = " + ID}));
            if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.NOT_AUTHENTICATED, ID + "");
            db.UpdateTable(Map.of("table", "Customer", "columns", new String[]{"CustomerTel"},
                    "values", new String[]{surround(Tel)}, "conditions", new String[]{"CustomerID = " + ID}));
        }
        else {
            db.UpdateTable(Map.of("table", "Employee", "columns", new String[]{"EmployeeTel"},
                    "values", new String[]{surround(Tel)}, "conditions", new String[]{"EmployeeID = " + ID}));
        }
    }

    public void uploadAvatar(boolean isCustomer, int ID, String Avatar){
        if(isCustomer){
            db.UpdateTable(Map.of("table", "Account", "columns", new String[]{"AccountAvatar"},
                    "values", new String[]{surround(Avatar)}, "conditions", new String[]{"AccountID = " + ID}));
        }
        else {
            db.UpdateTable(Map.of("table", "Employee", "columns", new String[]{"EmployeeAvatar"},
                    "values", new String[]{surround(Avatar)}, "conditions", new String[]{"EmployeeID = " + ID}));
        }

    }

    public Map<String, Object> getEmployeeList(){
        return db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"*"}));
    }

    public Map<String, Object> getEmployeeName(int EmployeeID){
        var res = db.simpleQuery(Map.of("table", "Employee", "columns", new String[]{"EmployeeName"},
                "conditions", new String[]{"EmployeeID = " + EmployeeID}));
        if(res.get("count").equals(0))
            throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, EmployeeID + "");
        return res;
    }

    public Map<String, Object> getBankListDetail() {
        return db.simpleQuery(Map.of("table", "Bank", "columns", new String[]{"*"}));
    }

    public double saveMoney(int id, int amount) {
        var res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"Balance"},
                "conditions", new String[]{"AccountID = " + id}));
        if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, id + "");
        var balance = (double) ((Object[]) res.get("Balance"))[0];
        db.UpdateTable(Map.of("table", "Account", "columns", new String[]{"Balance"},
                "values", new String[]{(balance + amount) + ""}, "conditions", new String[]{"AccountID = " + id}));
        res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"Balance"},
                "conditions", new String[]{"AccountID = " + id}));
        if(balance + amount != (double) ((Object[]) res.get("Balance"))[0]){
            db.UpdateTable(Map.of("table", "Account", "columns", new String[]{"Balance"},
                    "values", new String[]{(balance) + ""}, "conditions", new String[]{"AccountID = " + id}));
            throw new DatabaseException(DatabaseExceptionType.TOO_MUCH_MONEY, id + "");
        }
        return balance + amount;
    }

    public double withdrawMoney(int id, int amount) {
        var res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"Balance"},
                "conditions", new String[]{"AccountID = " + id}));
        if(res.get("count").equals(0)) throw new DatabaseException(DatabaseExceptionType.ACCOUNT_NOT_FOUND, id + "");
        var balance = (double) ((Object[]) res.get("Balance"))[0];
        if(balance < amount) {
            throw new DatabaseException(DatabaseExceptionType.BALANCE_NOT_ENOUGH, "Balance: " + balance );
        }
        db.UpdateTable(Map.of("table", "Account", "columns", new String[]{"Balance"},
                "values", new String[]{(balance - amount) + ""}, "conditions", new String[]{"AccountID = " + id}));
        res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"Balance"},
                "conditions", new String[]{"AccountID = " + id}));
        if(balance - amount != (double) ((Object[]) res.get("Balance"))[0]) {
            db.UpdateTable(Map.of("table", "Account", "columns", new String[]{"Balance"},
                    "values", new String[]{(balance) + ""}, "conditions", new String[]{"AccountID = " + id}));
            throw new DatabaseException(DatabaseExceptionType.UNKNOWN_ERROR, id + "");
        }
        return balance - amount;
    }
}
