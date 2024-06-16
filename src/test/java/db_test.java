import DB_Server.db_helper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class db_test {
//    @Test
//    public void testRegister(){
//        db_helper dbh = new db_helper();
//        dbh.db_helper();
//        assertTrue(dbh.Register("Tom", "aB.pn24jd8", "Saving", 1));
//    }
    @Test
    public void testCheckAuthenticate(){
        db_helper dbh = new db_helper();
        dbh.init();
        dbh.Authenticate(1, "JYJS", "123456789123456789");
    }
    @Test
    public void testCheckAuthenticated(){
        db_helper dbh = new db_helper();
        dbh.init();
        assertTrue((Boolean) dbh.checkAuthenticated(1).get("isAuthenticated"));
    }
    @Test
    public void testGetBankList(){
        db_helper dbh = new db_helper();
        dbh.init();
        assertTrue(((Object[]) dbh.getBankList().get("BankName")).length > 0);
        System.out.println(dbh.getBankList());
    }
    @Test
    public void testApplyLoan(){
        db_helper dbh = new db_helper();
        dbh.init();
        dbh.applyLoan( 1, 1000, 1);
    }
    @Test
    public void testGetLoanList(){
        db_helper dbh = new db_helper();
        dbh.init();
        System.out.println(dbh.getLoanList(1, true));
    }
    @Test
    public void testCancelLoan(){
        db_helper dbh = new db_helper();
        dbh.init();
        dbh.applyLoan( 1, 1000, 1);
        dbh.confirmLoan(1, 1);
//        dbh.cancelLoan(1);
    }
    @Test
    public void testGetUserDetail(){
        db_helper dbh = new db_helper();
        dbh.init();
        dbh.Register("Tom", "aB.pn24jd8", "Saving", 1);
        System.out.println(dbh.getUserDetail(2, true));
    }
    @Test
    public void testModifyMail(){
        db_helper dbh = new db_helper();
        dbh.init();
        dbh.modifyMail(true, 1, "123@qq.com");
    }
}
