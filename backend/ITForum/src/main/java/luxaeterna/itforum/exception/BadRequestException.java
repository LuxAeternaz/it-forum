package luxaeterna.itforum.exception;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(400, message);
    }
}
