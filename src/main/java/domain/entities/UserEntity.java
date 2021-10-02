package domain.entities;

public class UserEntity extends Entity {
    private String email;
    private String fullName;
    private UserRole role;
    private UserStatus status;

    public UserEntity(String id, String email, String name, UserRole role, UserStatus status) {
        this.email = email;
        this.id = id;
        this.fullName = name;
        this.role = role;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
