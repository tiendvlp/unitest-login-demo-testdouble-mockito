package domain.entities;

public class UserRole {
    public enum TYPE {
        ADMIN, STUDENT, GUEST, MENTOR
    }

    private TYPE type;

    public UserRole(TYPE type) {
        this.type = type;
    }

    public TYPE getType() {
        return type;
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "type=" + type +
                '}';
    }
}
