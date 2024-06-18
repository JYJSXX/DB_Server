package DB_Server;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.


import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.lang.Object;

public class Main {
    static db_helper dbh = new db_helper();
    public static void main(String[] args) {
        int port = 8080; // 监听的端口号
        dbh.init();
        try {
            // 创建 HttpServer 实例并绑定到指定端口
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("服务器已启动，正在监听端口 " + port);

            // 创建上下文并指定处理器
            server.createContext("/test", new MyHandler());

            // 创建默认的执行器
            server.setExecutor(null);

            // 启动服务器
            server.start();
        } catch (IOException e) {
            System.err.println("服务器启动失败：" + e.getMessage());
        }
    }

    // 自定义处理器类
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(@NotNull HttpExchange exchange) throws IOException {
            // 设置响应头和状态码
            String response;
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(requestBody);
            JsonNode jsonNode = mapper.readTree(requestBody);
            String type = jsonNode.get("type").asText();
            boolean success_access = true;
            Map<String,Object> response_map = new HashMap<>();
            System.out.println("get message");
            try {
                switch (type){
                    case "login":
                        //向数据库发起查询
                    {
                        String AccountName = jsonNode.get("AccountName").asText();
                        String password = jsonNode.get("password").asText();
                        response_map = dbh.Login(AccountName, password);
                        if(response_map.get("isCustomer").equals(true))
                        {
                            var res = dbh.checkAuthenticated((int) ((Object[]) response_map.get("AccountID"))[0]);
                            response_map.putAll(res);
                        }
                        break;
                    }
                    case "checkAuthentication":
                        //向数据库发起实名查询
                    {
                        int AccountID = jsonNode.get("AccountID").asInt();
                        response_map = dbh.checkAuthenticated(AccountID);
                        break;
                    }
                    case "register": {
                        var AccountName = jsonNode.get("AccountName").asText();
                        var password = jsonNode.get("password").asText();
                        String AccountType = jsonNode.get("AccountType").asText();
                        int BankID = jsonNode.get("BankID").asInt();
                        dbh.Register(AccountName, password, AccountType, BankID);
                        break;
                    }
                    case "getUserDetail":{
                        var AccountID = jsonNode.get("AccountID").asInt();
                        var isCustomer = jsonNode.get("isCustomer").asBoolean();
                        response_map = dbh.getUserDetail(AccountID, isCustomer);
                        break;
                    }
                    case "Authenticate":
                        //向数据库发起实名认证
                    {
                        var AccountID = jsonNode.get("AccountID").asInt();
                        var CustomerID = jsonNode.get("CustomerID").asText();
                        var CustomerName = jsonNode.get("CustomerName").asText();
                        dbh.Authenticate(AccountID, CustomerName, CustomerID);
                        break;
                    }
                    case "getBankList": {
                        response_map = dbh.getBankList();
                        break;
                    }
                    case "getDepartmentList": {
                        if(jsonNode.has("BankID")){
                            var BankID = jsonNode.get("BankID").asInt();
                            response_map = dbh.getDepartmentList(BankID);
                        }
                        else{
                            response_map = dbh.getDepartmentList();
                        }
                        break;
                    }
                    case "getLoanList": {
                        var AccountID = jsonNode.get("AccountID").asInt();
                        var isCustomer = jsonNode.get("isCustomer").asBoolean();
                        var name = jsonNode.get("name");
                        var amount = jsonNode.get("amount");
                        var status = jsonNode.get("status");
                        Map<String, Object> con = new HashMap<>();
                        if(name != null){
                            con.put("name", name.asText());
                        }
                        if(amount != null){
                            con.put("amount", amount.asInt());
                        }
                        if(status != null){
                            con.put("status", status.asInt());
                        }
                        response_map = dbh.getLoanList(AccountID,isCustomer, con);
                        break;
                    }
                    case "getEDB":{
                        var ID = jsonNode.get("DepartmentID").asInt();
                        response_map = dbh.getEDB(ID);
                        break;
                    }
                    case "getInterestRate":{
                        var ID = jsonNode.get("AccountID").asInt();
                        var Amount = jsonNode.get("Amount").asInt();
                        var res = dbh.getInterestRate(ID, Amount);
                        response_map.put("InterestRate", res);
                        break;
                    }
                    case "applyLoan":{
                        var ID = jsonNode.get("AccountID").asInt();
                        var Amount = jsonNode.get("Amount").asInt();
                        var Duration = jsonNode.get("Duration").asInt();
                        dbh.applyLoan(ID, Amount, Duration);
                        break;
                    }
                    case "repayLoan":{
                        var ID = jsonNode.get("LoanID").asInt();
                        dbh.repayLoan(ID);
                        break;
                    }
                    case "cancelLoan":{
                        var ID = jsonNode.get("LoanID").asInt();
                        dbh.cancelLoan(ID);
                        break;
                    }
                    case "confirmLoan":{
                        var LoanID = jsonNode.get("LoanID").asInt();
                        var EmployeeID = jsonNode.get("EmployeeID").asInt();
                        dbh.confirmLoan(LoanID, EmployeeID);
                        break;
                    }
                    case "denyLoan":{
                        var LoanID = jsonNode.get("LoanID").asInt();
                        var EmployeeID = jsonNode.get("EmployeeID").asInt();
                        dbh.denyLoan(LoanID, EmployeeID);
                        break;
                    }
                    case "modifyMail":{
                        var isCustomer = jsonNode.get("isCustomer").asBoolean();
                        var ID = jsonNode.get("ID").asInt();
                        var Mail = jsonNode.get("Mail").asText();
                        dbh.modifyMail(isCustomer, ID, Mail);
                        break;
                    }
                    case "modifyTel":{
                        var isCustomer = jsonNode.get("isCustomer").asBoolean();
                        var ID = jsonNode.get("ID").asInt();
                        var Tel = jsonNode.get("Tel").asText();
                        dbh.modifyTel(isCustomer, ID, Tel);
                        break;
                    }
                    case "registerEmployee":{
                        var EmployeeName = jsonNode.get("EmployeeName").asText();
                        var password = jsonNode.get("password").asText();
                        var DepartmentID = jsonNode.get("DepartmentID").asInt();
                        var Salary = jsonNode.get("Salary").asDouble();
                        dbh.Register(EmployeeName, password, DepartmentID, Salary);
                        break;
                    }
                    case "uploadAvatar":{
                        var isCustomer = jsonNode.get("isCustomer").asBoolean();
                        var ID = jsonNode.get("ID").asInt();
                        var Avatar = jsonNode.get("Avatar").asText();
                        dbh.uploadAvatar(isCustomer, ID, Avatar);
                        break;
                    }
                    case "getEmployeeList":{
                        response_map = dbh.getEmployeeList();
                        break;
                    }
                    case "getEmployeeName":{
                        var ID = jsonNode.get("ID").asInt();
                        response_map = dbh.getEmployeeName(ID);
                        break;
                    }
                    case "getBankListDetail":{
                        response_map = dbh.getBankListDetail();
                        break;
                    }
                    case "saveMoney":{
                        var ID = jsonNode.get("AccountID").asInt();
                        var Amount = jsonNode.get("Amount").asInt();
                        double balance = dbh.saveMoney(ID, Amount);
                        response_map.put("balance", balance);
                        break;
                    }
                    case "withdrawMoney":{
                        var ID = jsonNode.get("AccountID").asInt();
                        var Amount = jsonNode.get("Amount").asInt();
                        double balance = dbh.withdrawMoney(ID, Amount);
                        response_map.put("balance", balance);
                        break;
                    }
                    case "modifyDepartment":{
                        var ID = jsonNode.get("DepartmentID").asInt();
                        var DepartmentName = jsonNode.get("DepartmentName").asText();
                        dbh.modifyDepartment(ID, DepartmentName);
                        break;
                    }
                    case "getAccountName":{
                        var ID = jsonNode.get("AccountID").asInt();
                        var AccountName = dbh.getAccountName(ID);
                        response_map.put("AccountName", AccountName);
                        break;
                    }
                    default:
                        response_map.put("msg", "Invalid request type");
                        success_access = false;
                        break;
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
                success_access = false;
                response_map.put("msg", e.getMessage());
            } catch (Exception e){
                success_access = false;
                response_map.put("msg", e.getMessage());
                System.out.println(e.getMessage());
            }
            response_map.put("success", success_access);

            response = mapper.writeValueAsString(response_map);
            exchange.sendResponseHeaders(200, response.getBytes().length);

            // 获取输出流并写入响应内容
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
