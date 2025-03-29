package model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a user with an ID, username, email, and password.
 * Provides methods to update user details and is serializable for easy storage and retrieval.
 * @author MarioG
 */
public class User implements Serializable {
    private final String id;
    private String username;
    private String email;
    private String password;

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID().toString(); // -Generating new random ID-
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Updates the username and email for the user.
     *
     * @param username the new username
     * @param email    the new email address
     */
    public void saveNewIdentity(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /**
     * Updates the password for the user.
     *
     * @param password the new password
     */
    public void saveNewPassword(String password) {
        this.password = password;
    }
}
