package fi.vm.sade.kayttooikeus.service.exception;

public class UsernameAlreadyExistsException extends SadeBusinessException {

    public UsernameAlreadyExistsException() {
        super();
    }

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorKey() {
        return UsernameAlreadyExistsException.class.getCanonicalName();
    }
}
