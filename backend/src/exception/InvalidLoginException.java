package exception;

public class InvalidLoginException extends AppException {
    public InvalidLoginException(String message) {
        super(message, 401);
    }
}
