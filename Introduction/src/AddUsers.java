import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.io.IOException;

import com.CB.PasswordEncryptionService;

public class AddUsers {
    public static void main(String[] args)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
                IOException {
        PasswordEncryptionService pes = new PasswordEncryptionService();

        pes.useFile("/usr/share/tomcat7/webapps/Introduction/WEB-INF/" +
                        "users.db");
        pes.readFile();

        pes.printDatabase();
        pes.addUser("Statler", "password");
        pes.addUser("Waldorf", "password");
        pes.printDatabase();

        pes.writeFile();
    }
}