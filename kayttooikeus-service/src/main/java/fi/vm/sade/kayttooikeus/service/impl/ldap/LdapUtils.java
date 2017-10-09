package fi.vm.sade.kayttooikeus.service.impl.ldap;

import java.security.SecureRandom;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

public final class LdapUtils {

    private LdapUtils() {
    }

    public static byte[] generateRandomPassword() {
        byte[] password = new byte[4];
        new SecureRandom().nextBytes(password);
        return encrypt(password.toString());
    }

    private static byte[] encrypt(final String plaintext) {
        byte[] salt = new byte[4];
        new SecureRandom().nextBytes(salt);
        LdapShaPasswordEncoder encoder = new LdapShaPasswordEncoder();
        String digest = encoder.encodePassword(plaintext, salt);
        return digest.getBytes();
    }

}
