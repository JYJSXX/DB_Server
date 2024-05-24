import DB_Server.db_helper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class db_test {
    @Test
    public void testRegister(){
        db_helper dbh = new db_helper();
        dbh.db_helper();
        assertTrue(dbh.Register("Tom", "aB.pn24jd8", "Saving", 1));
    }
    @Test
    public void testCheckAuthenticate(){
        db_helper dbh = new db_helper();
        dbh.db_helper();
        dbh.Authenticate(1, "JYJS", "123456789123456789");
    }
}
