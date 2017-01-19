import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.io.IOException;

import com.CB.PasswordEncryptionService;

public class Authenticate {
    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException,
                InvalidKeySpecException {
        PasswordEncryptionService pes = new PasswordEncryptionService();

        pes.readFile("/usr/share/tomcat7/webapps/Introduction/WEB-INF/" +
                "users.db");

        System.out.println(pes.authenticate("Statler", "password"));
        System.out.println(pes.authenticate("Waldorf", "wrong"));
    }
}