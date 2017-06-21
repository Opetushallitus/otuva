package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttaja;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import java.security.SecureRandom;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class KayttajaHenkiloEntityMapper extends CustomMapper<Kayttaja, Henkilo> {

    @Override
    public void mapBtoA(Henkilo b, Kayttaja a, MappingContext context) {
        Kayttajatiedot kayttajatiedot = b.getKayttajatiedot();
        a.setKayttajatunnus(b.getKayttajatiedot().getUsername());

        if (StringUtils.isNotBlank(kayttajatiedot.getSalt())
                && StringUtils.isNotBlank(kayttajatiedot.getPassword())) {
            StringBuilder builder = new StringBuilder();
            builder.append("{PBKDF2}2048");
            builder.append("$");
            builder.append(Base64.encodeBase64String(Base64.decodeBase64(kayttajatiedot.getSalt().getBytes())));
            builder.append("$");
            builder.append(kayttajatiedot.getPassword());
            a.setSalasana(builder.toString().getBytes());
        } else {
            byte[] salasana = new byte[4];
            new SecureRandom().nextBytes(salasana);
            a.setSalasana(encrypt(salasana.toString()));
        }
    }

    private static byte[] encrypt(final String plaintext) {
        byte[] salt = new byte[4];
        new SecureRandom().nextBytes(salt);
        LdapShaPasswordEncoder encoder = new LdapShaPasswordEncoder();
        String digest = encoder.encodePassword(plaintext, salt);
        return digest.getBytes();
    }

    @Override
    public void mapAtoB(Kayttaja a, Henkilo b, MappingContext context) {
        Kayttajatiedot kayttajatiedot = b.getKayttajatiedot() != null
                ? b.getKayttajatiedot()
                : new Kayttajatiedot();
        kayttajatiedot.setUsername(a.getKayttajatunnus());
        kayttajatiedot.setPassword("MOCK");
    }

}
