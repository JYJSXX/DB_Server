package DB_Server;

public enum DatabaseExceptionType{
    INVALID_PASSWORD(),
    INVALID_ACCOUNT_NAME(),
    INVALID_Customer_WITH_ID(),
    ACCOUNT_NOT_FOUND(),
    ACCOUNT_PASSWORD_NOT_MATCH(),
    INSERT_ERROR(),
    NOT_AUTHENTICATED(),
    INVALID_LOAN_AMOUNT(),
    UPDATE_LOAN_DATE_ILLEGALLY(),
    BALANCE_NOT_ENOUGH(),
    LOAN_NOT_FOUND(),
    LOAN_NOT_VALID(),
    TOO_MUCH_MONEY(),
    UNKNOWN_ERROR()
}
