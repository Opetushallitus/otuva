package fi.vm.sade.client;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String uri) {
        super("404 Not Found: " + uri);
    }
}
