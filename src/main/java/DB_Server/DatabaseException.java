package DB_Server;

public class DatabaseException extends RuntimeException{
    final DatabaseExceptionType type;
    final String msg;
    public DatabaseException(DatabaseExceptionType type, String msg) {
        this.type = type;
        msg += ' ';
        switch (type){
            case INVALID_PASSWORD:
                this.msg = "Invalid password: " + msg;
                break;
            case INVALID_ACCOUNT_NAME:
                this.msg = "Account name Exist: " + msg;
                break;
            case INVALID_Customer_WITH_ID:
                this.msg = "Invalid customer Identity: " + msg;
                break;
            case ACCOUNT_NOT_FOUND:
                this.msg = "Account not found: " + msg;
                break;
            case ACCOUNT_PASSWORD_NOT_MATCH:
                this.msg = "Account password not match: " + msg;
                break;
            case INSERT_ERROR:
                this.msg = "Insert error: " + msg;
                break;
            case NOT_AUTHENTICATED:
                this.msg = "Not authenticated: " + msg;
                break;
            case INVALID_LOAN_AMOUNT:
                this.msg = "Invalid loan amount: " + msg;
                break;
            case UPDATE_LOAN_DATE_ILLEGALLY:
                this.msg = "Update loan date illegally: " + msg;
                break;
            case BALANCE_NOT_ENOUGH:
                this.msg = "Balance not enough: " + msg;
                break;
            case LOAN_NOT_FOUND:
                this.msg = "Loan not found: " + msg;
                break;
            case LOAN_NOT_VALID:
                this.msg = "Loan not valid: " + msg;
                break;
            default:
                this.msg = "Unknown error" + msg ;
        }
    }
    @Override
    public String getMessage() {
        return msg;
    }
}


