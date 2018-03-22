package fi.vm.sade.kayttooikeus.service.impl.ldap;

import java.security.SecureRandom;

import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;

public final class LdapUtils {

    private LdapUtils() {
    }

    public static byte[] generateRandomPassword() {
        byte[] password = new byte[4];
        new SecureRandom().nextBytes(password);
        return encrypt(password.toString());
    }

    private static byte[] encrypt(final String plaintext) {
        BytesKeyGenerator bytesKeyGenerator = KeyGenerators.secureRandom(4);
        LdapShaPasswordEncoder encoder = new LdapShaPasswordEncoder(bytesKeyGenerator);
        String digest = encoder.encode(plaintext);
        return digest.getBytes();
    }

}
