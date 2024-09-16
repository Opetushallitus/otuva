package fi.vm.sade.cas.oppija.surrogate.exception;

import javax.security.auth.login.LoginException;

public class SurrogateNotAllowedException extends LoginException {

    public SurrogateNotAllowedException(String msg) {
        super(msg);
    }

}
