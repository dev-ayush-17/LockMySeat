package exception;

public class SeatAlreadyBookedException extends AppException {
    public SeatAlreadyBookedException(String message) {
        super(message, 409);
    }
}
