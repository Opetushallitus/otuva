package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.properties.AuthProperties;
import fi.vm.sade.kayttooikeus.service.CryptoService;
import fi.vm.sade.kayttooikeus.service.exception.PasswordException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CryptoServiceImpl implements CryptoService {

    private static final Logger log = LoggerFactory.getLogger(CryptoServiceImpl.class);

    private static final int iterations =  2 * 1024;
    private static final int saltLen = 128;
    private static final int desiredKeyLen = 128;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

    private String STATIC_SALT;

    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()~`-=_+[]{}|:\";',./<>?";

    private Integer passwordMinLen;

    private Integer passwordMinAmoungSpecialChars;

    private Integer passwordMinAmountNumbers;

    private Boolean passwordLowerAndUpperCase;

    @Autowired
    public CryptoServiceImpl(AuthProperties authProperties) {
        this.STATIC_SALT = authProperties.getCryptoService().getStaticSalt();
        this.passwordMinLen = authProperties.getPassword().getMinLen();
        this.passwordMinAmoungSpecialChars = authProperties.getPassword().getMinAmountSpecialChars();
        this.passwordMinAmountNumbers = authProperties.getPassword().getMinAmountNumbers();
        this.passwordLowerAndUpperCase = authProperties.getPassword().getLowerAndUpperCase();
    }

    /**
     * Computes a salted PBKDF2 hash of given plain text password suitable for
     * storing in a database.
     */
    @Override
    public String getSaltedHash(String password, String salt) {
        try {
            return hash(password, Base64.decodeBase64(combineSalt(salt)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private String combineSalt (String salt) {
        return STATIC_SALT + salt;
    }

    /**
     * Generates 128 long salt String, Base64 encoded
     */
    @Override
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(saltLen, random).toString(32);
    }

    /**
     * Checks whether given plain text password corresponds to a stored salted
     * hash of the password.
     */
    @Override
    public boolean check(String password, String storedHash, String storedSalt) {
        String hashOfInput = this.getSaltedHash(password, storedSalt);
        return hashOfInput.equals(storedHash);
    }


    // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
    // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
    private String hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen));
        return Base64.encodeBase64String(key.getEncoded()).trim();
        //trim is needed to remove CRLF that base64Encode puts there
    }

    @Override
    public List<String> isStrongPassword(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null) {
            errors.add("validPassword.empty");
            return errors;
        }
        password = password.trim();
        if (password.length() < passwordMinLen) {
            errors.add("validPassword.short;" + passwordMinLen);
        }
        char[] allchars = password.toCharArray();
        int upcase = 0;
        int lowcase = 0;
        int digit = 0;
        int special = 0;

        for (char c : allchars) {
            if (Character.isUpperCase(c)) {
                ++upcase;
            } else if (Character.isLowerCase(c)) {
                ++lowcase;
            } else if (Character.isDigit(c)) {
                ++digit;
            } else if (SPECIAL_CHARACTERS.contains(String.valueOf(c))) {
                ++special;
            }
        }
        if (passwordLowerAndUpperCase) {
            if (upcase == 0 || lowcase == 0) {
                errors.add("validPassword.uppercase");
            }
        }
        if (digit < passwordMinAmountNumbers) {
            if (1 == passwordMinAmountNumbers) {
                errors.add("validPassword.number");
            } else {
                errors.add("validPassword.nonumber;" + passwordMinAmountNumbers);
            }
        }
        if (special < passwordMinAmoungSpecialChars) {
            if (passwordMinAmoungSpecialChars == 1) {
                errors.add("validPassword.special");
            } else {
                errors.add("validPassword.nospecial;" + passwordMinAmoungSpecialChars);
            }
        }
        return errors;
    }

    @Override
    public void throwIfNotStrongPassword(String password) {
        isStrongPassword(password)
                .stream().findFirst().ifPresent((error) -> {throw new PasswordException(error);});
    }
}
