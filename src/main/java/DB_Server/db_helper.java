package DB_Server;

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
    public void db_helper(){
        db = new db_server();
        db.db_server();
    }
    public boolean Register(String AccountName, String password, String AccountType, int BankID) {
        if(!isValidPassword(password)) throw new DatabaseException(DatabaseExceptionType.INVALID_PASSWORD, password);
        var res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"AccountID"},
                "conditions", new String[]{"AccountName = '" + AccountName + "'"})).get("AccountID");
        if(res != null) throw new DatabaseException(DatabaseExceptionType.INVALID_ACCOUNT_NAME, AccountName);
        try {
            res = db.InsertTable("Account", Column_map.get("Account"),
                    new String[]{surround(AccountName), surround(AccountType), BankID + ""});
        } catch (Exception e) {
            return false;
        }
        res = db.simpleQuery(Map.of("table", "Account", "columns", new String[]{"AccountID"},
                "conditions", new String[]{"AccountName = '" + AccountName + "'"})).get("AccountID");
        if(res == null) return false;
        String AccountID = res.toString();
        return db.InsertTable("Password", Column_map.get("Password"), new String[]{AccountID,surround(password)});
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
        if(AccountID < 0) {
            Map<String, Object> res = new HashMap<>();
            res.put("isAuthenticated", false);
        }
        return db.CheckAuthentication(AccountID);
    }

    public Map<String, Object> CheckCustomerExisted(int AccountID){
        Map<String,Object> res = new HashMap<>();
        Map<String, Object> json = new HashMap<>();
        json.put("table", "Customer");
        json.put("columns", new String[]{"CustomerID"});
        json.put("conditions", new String[]{"AccountID = " + AccountID});
        res = db.simpleQuery(json);
        return res;
    }

    public void Authenticate(int AccountID, String CustomerName, String CustomerID){
        if(CheckIDNumber(CustomerName, CustomerID)){
            Map<String, Object> json = new HashMap<>();
            json.put("table", "Customer");
            json.put("columns", new String[]{"CustomerID"});
            json.put("conditions", new String[]{"CustomerID = " + surround(CustomerID)});
            var res = db_server.simpleQuery(json);
            assert res != null;
            if(res.isEmpty()){
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
}
