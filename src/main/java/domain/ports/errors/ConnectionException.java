package domain.ports.errors;

public class ConnectionException extends Exception {
    public ConnectionException(String message){
        super(message);
    }
}
