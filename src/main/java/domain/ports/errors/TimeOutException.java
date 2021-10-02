package domain.ports.errors;

public class TimeOutException extends Exception {
    public TimeOutException(String message) {
        super(message);
    }
}
