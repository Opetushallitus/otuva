package fi.vm.sade.cas.oppija.controller;

import fi.vm.sade.cas.oppija.exception.ApplicationException;
import fi.vm.sade.cas.oppija.exception.SystemException;

import java.io.IOException;
import java.util.function.Supplier;

public final class ControllerUtils {

    private ControllerUtils() {
    }
    @FunctionalInterface
    public interface IOExceptionThrowingSupplier<T> {
        T get() throws IOException;
    }

    public static <T> T ioExceptionToSystemException(IOExceptionThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
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
