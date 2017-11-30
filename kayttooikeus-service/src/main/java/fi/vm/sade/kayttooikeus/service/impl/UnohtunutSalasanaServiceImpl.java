package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.YhteystietojenTyypit;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.VarmennusPoletti;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.VarmennusPolettiRepository;
import fi.vm.sade.kayttooikeus.service.*;

import java.time.LocalDateTime;
import java.util.UUID;

import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.ifPresentOrElse;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi;
import java.time.Duration;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UnohtunutSalasanaServiceImpl implements UnohtunutSalasanaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnohtunutSalasanaServiceImpl.class);
    private static final Duration POLETTI_VOIMASSA = Duration.ofMinutes(60L);

    private final TimeService timeService;
    private final EmailService emailService;
    private final HenkiloDataRepository henkiloDataRepository;
    private final VarmennusPolettiRepository varmennusPolettiRepository;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final CryptoService cryptoService;
    private final LdapSynchronizationService ldapSynchronizationService;
    private final KayttajatiedotRepository kayttajatiedotRepository;

    @Override
    public void lahetaPoletti(String kayttajatunnus) {
        ifPresentOrElse(henkiloDataRepository.findByKayttajatiedotUsername(kayttajatunnus), this::lahetaPoletti,
                () -> LOGGER.warn("Unohtunut salasana -sähköpostia ei lähetetty koska käyttäjätunnuksella {} ei löytynyt henkilöä", kayttajatunnus));
    }

    private void lahetaPoletti(Henkilo henkilo) {
        long poistetut = varmennusPolettiRepository.deleteByHenkiloAndTyyppi(henkilo, VarmennusPoletti.VarmennusPolettiTyyppi.HAVINNYT_SALASANA);
        VarmennusPoletti varmennusPoletti = varmennusPolettiRepository.save(VarmennusPoletti.builder()
                .poletti(UUID.randomUUID().toString())
                .tyyppi(VarmennusPoletti.VarmennusPolettiTyyppi.HAVINNYT_SALASANA)
                .voimassa(timeService.getDateTimeNow().plus(POLETTI_VOIMASSA))
                .henkilo(henkilo)
                .build());
        LOGGER.info("Luotiin henkilölle {} uusi {}-poletti, voimassa {} asti. Poistettin vanhat poletit ({}kpl)",
                henkilo.getOidHenkilo(), varmennusPoletti.getTyyppi(), varmennusPoletti.getVoimassa(), poistetut);

        try {
            lahetaPoletti(henkilo.getOidHenkilo(), varmennusPoletti.getPoletti(), POLETTI_VOIMASSA);
        } catch (Exception e) {
            LOGGER.error("Unohtunut salasana -sähköpostin lähetys epäonnistui henkilölle {}", henkilo.getOidHenkilo(), e);
        }
    }

    private void lahetaPoletti(String oid, String poletti, Duration voimassa) {
        HenkiloDto henkilo = oppijanumerorekisteriClient.getHenkiloByOid(oid);
        String sahkoposti = YhteystietoUtil.getYhteystietoArvo(henkilo.getYhteystiedotRyhma(),
                YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI,
                YhteystietojenTyypit.PRIORITY_ORDER)
                .orElseThrow(() -> new DataInconsistencyException("Henkilöllä " + oid + " ei ole sähköpostiosoitetta"));

        // varmistetaan että sähköpostiosoite on käytössä vain yhdellä henkilöllä
        Set<String> oids = oppijanumerorekisteriClient.listOidByYhteystieto(sahkoposti);
        if (oids.size() > 1 || !oids.contains(oid)) {
            throw new DataInconsistencyException("Sähköpostiosoite " + sahkoposti + " on käytössä useammalla henkilöllä: " + oids);
        }

        emailService.sendEmailReset(henkilo, sahkoposti, poletti, voimassa);
    }

    @Override
    @Transactional
    public void resetoiSalasana(String base64EncodedPoletti, String password) {
        this.cryptoService.throwIfNotStrongPassword(password);
        String poletti = new String(Base64.decodeBase64(base64EncodedPoletti));
        VarmennusPoletti varmennusPoletti = varmennusPolettiRepository.findByPoletti(poletti).
                orElseThrow(() -> new NotFoundException("Varmennuspolettia ei löytynyt: " + poletti));
        if(!LocalDateTime.now().isBefore(varmennusPoletti.getVoimassa())) {
            throw new ForbiddenException("Varmennuspoletti ei ole voimassa");
        }
        Henkilo henkilo = varmennusPoletti.getHenkilo();
        Kayttajatiedot kayttajatiedot = henkilo.getKayttajatiedot();
        String salt = this.cryptoService.generateSalt();
        String hash = this.cryptoService.getSaltedHash(password, salt);
        kayttajatiedot.setSalt(salt);
        kayttajatiedot.setPassword(hash);

        ldapSynchronizationService.updateHenkiloAsap(henkilo.getOidHenkilo());
        LOGGER.info("Käytettiin henkilölle {} luotu poletti {}.",
                henkilo.getOidHenkilo(), varmennusPoletti.getPoletti());
    }

}
