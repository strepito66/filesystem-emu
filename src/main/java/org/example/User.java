package org.example;
import java.io.Serial;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static int nextUser_id = 0;
    private int user_id;
    private String username;
    private Group group;
    private String hashedPass;
    private byte[] salt;


    private User(String username, Group group, String password) {
        this.user_id = nextUser_id;
        nextUser_id++;
        this.username = username;
        this.group = group;
        this.group.addMember(this);
        hashedPass = hash(password);
    }


    public int getId() {
        return user_id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    private String hash(String pass) {
        byte[] salt = new byte[16];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(salt);
        this.salt = salt;

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(salt);

        byte[] hashedPassword = md.digest(pass.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }

    private boolean checkHash(String pass) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(this.salt);

        byte[] hashedInputPassword = md.digest(pass.getBytes());
        String newHash = Base64.getEncoder().encodeToString(hashedInputPassword);

        return newHash.equals(hashedPass);
    }

    public static User signup(String username, Group group, String password) {

        return new User(username, group, password);
    }

    public boolean authenticate(String password) {
        return checkHash(password);
    }


}
