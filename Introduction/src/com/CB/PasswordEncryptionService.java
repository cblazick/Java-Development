package com.CB;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.io.*;
import java.lang.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordEncryptionService {

    private static int LEN_PASS = 20;
    private static int LEN_SALT = 8;

    private String usersFile;

    private ArrayList<String> users = new ArrayList<String>();
    private ArrayList<byte[]> encryptedPasswords = new ArrayList<byte[]>();
    private ArrayList<byte[]> salts = new ArrayList<byte[]>();

    public void printBytes(byte[] bytes) {
        // print a byte array as hex pairs
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        System.out.print(sb.toString());
    }

    public void printArrayList(ArrayList<byte[]> a) {
        // print out a whole ArrayList. calls printBytes
        System.out.print("[");
        for(int i = 0; i < a.size(); i++) {
            printBytes(a.get(i));
            if (i < a.size()-1) {
                System.out.print(",");
            }
        }
        System.out.println("]");
    }

    public void printDatabase() {
        // print the current state of the database
        System.out.print("users: ");
        System.out.println(users);
        System.out.print("passwords: ");
        printArrayList(encryptedPasswords);
        System.out.print("salts: ");
        printArrayList(salts);

        System.out.println();
    }

    public boolean useFile(String file) {
        usersFile = file;

        return true;
    }

    public String returnFile() {
        return usersFile;
    }

    public boolean addUser(String username, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        // use the provided functions to generate these values
        byte[] salt = generateSalt();
        byte[] encryptedPassword = getEncryptedPassword(password, salt);

        // if the user doesn't exist, add it, and all its values
        if (!users.contains(username)){
            users.add(username);
            encryptedPasswords.add(encryptedPassword);
            salts.add(salt);
        }

        return true;
    }

    public boolean writeFile()
            throws IOException {
        // verify there are always the same number of entries
        if ((users.size() != encryptedPasswords.size())
                || (encryptedPasswords.size() != salts.size())) {
            return false;
        }

        // write name surrounded by quotes
        // followed by encrypted password and salt which are fixed lengths
        FileOutputStream fos = new FileOutputStream(usersFile);
        for(int i=0; i<users.size(); i++) {
            fos.write('"');
            fos.write(users.get(i).getBytes());
            fos.write('"');
            fos.write(encryptedPasswords.get(i));
            fos.write(salts.get(i));
            fos.write(new String("\n").getBytes());
        }
        fos.close();

        return true;
    }

    public boolean readFile()
            throws IOException {
        //System.out.println("loading: " + usersFile + " ...");

        File file = new File(usersFile);
        if (!file.exists()) {
            // do nothing and return. No file to load
            return true;
        }

        // initialize variables for "db" read
        FileInputStream fis = new FileInputStream(file);
        int c;
        String name = "";
        byte[] pass = new byte[LEN_PASS];
        byte[] salt = new byte[LEN_SALT];
        int nl;
        boolean newline = true;

        // step through the file
        while ((c = fis.read()) != -1) {
            if (newline && (char)c == '"') {
                //should only happen for the first character of a line
                newline = false;
                continue;
            }
            if ((char)c == '"') {
                // done reading the name.
                // password and salt are a given length, so grab them next
                //System.out.println("==" + name + "==");
                fis.read(pass);
                //printBytes(pass);
                fis.read(salt);
                //printBytes(salt);
                //System.out.println();

                // add what we got from the file to the database
                users.add(name);
                encryptedPasswords.add(pass.clone());
                salts.add(salt.clone());

                // verify the next char is a newline and discard
                nl = fis.read();
                if ((char)nl != '\n') {
                    System.out.println("newline not found");
                    return false;
                }
                else {
                    name = "";
                    newline = true;
                    continue;
                }
            }

            // add the current byte to the name string
            name += String.valueOf((char)c);
        }

        fis.close();

        return true;
    }

    public boolean readFile(String filename)
            throws IOException {
        useFile(filename);
        return readFile();
    }

    public boolean authenticate(String user, String pass)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if(!users.contains(user)) {
            return false;
        }

        int i = users.indexOf(user);
        return authenticate(pass, encryptedPasswords.get(i), salts.get(i));
    }

    public boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Encrypt the clear-text password using the same salt that was used to
        // encrypt the original password
        byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);

        // Authentication succeeds if encrypted password that the user entered
        // is equal to the stored hash
        return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
    }

    public byte[] getEncryptedPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
        // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
        String algorithm = "PBKDF2WithHmacSHA1";
        // SHA-1 generates 160 bit hashes, so that's what makes sense here
        int derivedKeyLength = LEN_PASS*8; // derivedKeyLength needs to be in bits, hence *8
        // Pick an iteration count that works for you. The NIST recommends at
        // least 1,000 iterations:
        // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
        // iOS 4.x reportedly uses 10,000:
        // http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
        int iterations = 20000;

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

        SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

        return f.generateSecret(spec).getEncoded();
    }

    public byte[] generateSalt() throws NoSuchAlgorithmException {
        // VERY important to use SecureRandom instead of just Random
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
        byte[] salt = new byte[LEN_SALT];
        random.nextBytes(salt);

        return salt;
    }
}