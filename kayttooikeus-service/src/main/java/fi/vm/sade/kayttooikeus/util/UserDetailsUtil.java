package fi.vm.sade.kayttooikeus.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class UserDetailsUtil {
    public static String getCurrentUserOid() throws NullPointerException {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        if (oid == null) {
            throw new NullPointerException("No user name available from SecurityContext!");
        }
        return oid;
    }
}
