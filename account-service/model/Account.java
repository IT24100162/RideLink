package lk.ridelink.account_service.model;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
public class Account {
    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;
    private String phone;
    private LocalDate dateOfBirth;
    private String profileImageUrl;
    private AccountRole role;
    private AccountStatus status;
    private int tokenVersion;
    private Instant createdAt;
    private Instant updatedAt;

    public Account() {
    }

    public Account(
            String fullName,
            String email,
            String passwordHash,
            String phone,
            LocalDate dateOfBirth,
            String profileImageUrl,
            AccountRole role,
            AccountStatus status,
            int tokenVersion,
            Instant createdAt,
            Instant updatedAt) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.status = status;
        this.tokenVersion = tokenVersion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public AccountRole getRole() { return role; }
    public void setRole(AccountRole role) { this.role = role; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public int getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(int tokenVersion) { this.tokenVersion = tokenVersion; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}