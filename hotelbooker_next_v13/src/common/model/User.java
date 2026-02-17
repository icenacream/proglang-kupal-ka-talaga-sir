package common.model;

import java.time.LocalDateTime;

/**
 * Simple guest account model.
 * Stored in data/users.txt
 */
public class User {
    private String userId;
    private String fullName;
    private String email;
    private String password; // NOTE: plain text for school project simplicity
    private LocalDateTime createdAt;

    public User() {}

    public User(String userId, String fullName, String email, String password, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return userId + "|" + fullName + "|" + email + "|" + password + "|" + createdAt;
    }
}
