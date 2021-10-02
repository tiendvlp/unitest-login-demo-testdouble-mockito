package domain.ports.errors;

public class NotFoundException extends Exception {
    public NotFoundException (String message){
        super(message);
    }
}
