package fi.vm.sade.kayttooikeus.service.exception;

public class GoneException extends RuntimeException {

    public GoneException() {
    }

    public GoneException(String message) {
        super(message);
    }

    public GoneException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoneException(Throwable cause) {
        super(cause);
    }
}
