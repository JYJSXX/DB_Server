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

public class Main {
    static db_helper dbh = new db_helper();
    public static void main(String[] args) {
        int port = 8080; // 监听的端口号
        dbh.db_helper();
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
            String response = "Hello, World!";
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(requestBody);
            String type = jsonNode.get("type").asText();
            switch (type){
                case "login":
                    //向数据库发起查询
                    break;
                case "checkAuthentication":
                    //向数据库发起实名查询
                    int AccountID = jsonNode.get("AccountID").asInt();
                    var authentication = dbh.checkAuthenticated(AccountID);
                    response = authentication.toString();
                    break;
                case "register":
                    String AccountName = jsonNode.get("AccountName").asText();
                    String password = jsonNode.get("password").asText();
                    String AccountType = jsonNode.get("AccountType").asText();
                    int BankID = jsonNode.get("BankID").asInt();
                    try {
                        dbh.Register(AccountName, password, AccountType, BankID);
                    } catch (DatabaseException e) {
                        response = e.getMessage();
                    }
                    break;
                case "Authenticate":
                    //向数据库发起实名认证
                    AccountID = jsonNode.get("CustomerID").asInt();
                    var CustomerID = jsonNode.get("CustomerID").asText();
                    var CustomerName = jsonNode.get("CustomerName").asText();
                    try{
                        dbh.Authenticate(AccountID, CustomerID, CustomerName);
                    } catch (DatabaseException e) {
                        response = e.getMessage();
                    }
                    break;
            }
            System.out.println("get message");
            System.out.println(requestBody);
            exchange.sendResponseHeaders(200, response.getBytes().length);

            // 获取输出流并写入响应内容
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
