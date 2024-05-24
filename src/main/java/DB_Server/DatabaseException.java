package DB_Server;

public class DatabaseException extends RuntimeException{
    final DatabaseExceptionType type;
    final String msg;
    public DatabaseException(DatabaseExceptionType type, String msg) {
        this.type = type;
        switch (type){
            case INVALID_PASSWORD:
                this.msg = "Invalid password: " + msg;
                break;
            case INVALID_ACCOUNT_NAME:
                this.msg = "Invalid account name: " + msg;
                break;
            case INVALID_Customer_WITH_ID:
                this.msg = "Invalid customer Identity: " + msg;
                break;
            default:
                this.msg = "Unknown error";
        }
    }
    @Override
    public String getMessage() {
        return msg;
    }
}


