package domain.entities;

public class UserStatus {
    public enum STATUS {
        BLOCKED, ACTIVE
    }

    private STATUS status;

    public UserStatus(STATUS status) {
        this.status = status;
    }

    public STATUS getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "status=" + status +
                '}';
    }
}
