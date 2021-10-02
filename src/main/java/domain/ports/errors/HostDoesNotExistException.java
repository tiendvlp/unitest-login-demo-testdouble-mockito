package domain.ports.errors;

public class HostDoesNotExistException extends Exception {
    public HostDoesNotExistException(String message) {
        super(message);
    }
}
