package fi.vm.sade.cas.oppija.controller;

import fi.vm.sade.cas.oppija.exception.ApplicationException;
import fi.vm.sade.cas.oppija.exception.SystemException;

import java.util.function.Supplier;

public final class ControllerUtils {

    private ControllerUtils() {
    }

    public static <T> T wrapExceptionToApplicationException(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

}
