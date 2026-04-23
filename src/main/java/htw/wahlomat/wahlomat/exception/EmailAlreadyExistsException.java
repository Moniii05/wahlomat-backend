package htw.wahlomat.wahlomat.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("E-Mail bereits registriert: " + email);
    }
}
