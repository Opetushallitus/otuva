package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.authentication.business.exception.IdentificationExpiredException;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.AuthProperties;
import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Identification;
import fi.vm.sade.kayttooikeus.repositories.HenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.IdentificationRepository;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

@Service
public class IdentificationServiceImpl extends AbstractService implements IdentificationService {

    private static final String STRONG_AUTHENTICATION_IDP = "vetuma";
    private static final String HAKA_AUTHENTICATION_IDP = "haka";

    private IdentificationRepository identificationRepository;
    private HenkiloRepository henkiloRepository;
    private KayttoOikeusService kayttoOikeusService;
    private AuthProperties authProperties;
    private OrikaBeanMapper mapper;

    @Autowired
    public IdentificationServiceImpl(IdentificationRepository identificationRepository,
                                     HenkiloRepository henkiloRepository,
                                     KayttoOikeusService kayttoOikeusService,
                                     AuthProperties authProperties,
                                     OrikaBeanMapper mapper){
        this.identificationRepository = identificationRepository;
        this.henkiloRepository = henkiloRepository;
        this.kayttoOikeusService = kayttoOikeusService;
        this.authProperties = authProperties;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public String generateAuthTokenForHenkilo(String oid, String idpKey, String idpIdentifier) {
        logger.info("generateAuthTokenForHenkilo henkilo:[{}] idp:[{}] identifier:[{}]", oid, idpKey, idpIdentifier);
        Henkilo henkilo = henkiloRepository.findByOidHenkilo(oid).orElseThrow(()
                -> new NotFoundException("no henkilo found with oid:[" + oid + "]"));

        Optional<Identification> identification = henkilo.getIdentifications().stream()
                .filter(henkiloIdentification ->
                        idpIdentifier.equals(henkiloIdentification.getIdentifier()) && idpKey.equals(henkiloIdentification.getIdpEntityId()))
                .findFirst();

        String token = generateToken();
        if (identification.isPresent()) {
            updateTokenIfNotExpired(identification.get(), token);
        } else {
            createIdentification(henkilo, token, idpIdentifier, idpKey);
        }

        return token;

    }

    @Override
    @Transactional(readOnly = true)
    public String getHenkiloOidByIdpAndIdentifier(String idpKey, String idpIdentifier) {
        return this.identificationRepository.findByidpEntityIdAndIdentifier(idpKey, idpIdentifier)
                .orElseThrow(() -> new NotFoundException("Identification not found"))
                .getHenkilo()
                .getOidHenkilo();
    }

    @Override
    @Transactional
    public IdentifiedHenkiloTypeDto findByTokenAndInvalidateToken(String token) {
        logger.info("validateAuthToken:[{}]", token);
        Identification identification = identificationRepository.findByAuthtoken(token)
                .orElseThrow(() -> new NotFoundException("identification not found"));
        identification.setAuthtoken(null);

        IdentifiedHenkiloTypeDto dto = mapper.map(identification, IdentifiedHenkiloTypeDto.class);
        dto.setAuthorizationData(kayttoOikeusService.findAuthorizationDataByOid(dto.getOidHenkilo()));
        return dto;
    }

    @Override
    @Transactional
    public String updateIdentificationAndGenerateTokenForHenkilo(String oid) {
        Henkilo henkilo = henkiloRepository.findByOidHenkilo(oid).orElseThrow(()
                -> new NotFoundException("henkilo not found"));
        String token = generateToken();
        Optional<Identification> henkiloIdentification = henkilo.getIdentifications().stream()
                .filter(identification -> STRONG_AUTHENTICATION_IDP.equals(identification.getIdpEntityId()))
                .findFirst();

        if (henkiloIdentification.isPresent()) {
            Identification identification = henkiloIdentification.get();
            identification.setIdpEntityId(STRONG_AUTHENTICATION_IDP);
            identification.setIdentifier(henkilo.getKayttajatiedot().getUsername());
            identification.setAuthtoken(token);
        } else {
            Identification identification = new Identification();
            henkilo.getIdentifications().add(identification);
            identification.setHenkilo(henkilo);
            identification.setIdpEntityId(STRONG_AUTHENTICATION_IDP);
            identification.setIdentifier(henkilo.getKayttajatiedot().getUsername());
            identification.setAuthtoken(token);
        }

        return token;
    }

    private void createIdentification(Henkilo henkilo, String token, String identifier, String idpKey) {
        logger.info("creating new identification token:[{}]", token);
        Identification identification = new Identification();
        identification.setHenkilo(henkilo);

        if (henkilo.getIdentifications() == null) {
            henkilo.setIdentifications(new HashSet<>());
        }

        henkilo.getIdentifications().add(identification);
        identification.setIdentifier(identifier);
        identification.setIdpEntityId(idpKey);
        identification.setAuthtoken(token);
        if (HAKA_AUTHENTICATION_IDP.equals(idpKey)) {
            identification.setExpirationDate(getExpirationDateFromNow());
        }
    }

    private void updateTokenIfNotExpired(Identification identification, String token) {
        if (identification.getExpirationDate() != null && new Date().after(identification.getExpirationDate())) {
            logger.info("Login denied. Haka identification expired for user {} on expiration date {}",
                    identification.getIdentifier(), identification.getExpirationDate());
            throw new IdentificationExpiredException("Haka identification expired");
        } else {
            identification.setAuthtoken(token);
            logger.info("old identification found, setting new token:[{}]", token);

            if (HAKA_AUTHENTICATION_IDP.equals(identification.getIdpEntityId())) {
                identification.setExpirationDate(getExpirationDateFromNow());
                logger.debug("New expiration date for haka user {}: {}", identification.getIdentifier(),
                        identification.getExpirationDate());
            }
        }
    }

    private Date getExpirationDateFromNow() {
        return new DateTime().withPeriodAdded(authProperties.getExpirationMonths(), 1).toDate();
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(128, random).toString(32);
    }

}