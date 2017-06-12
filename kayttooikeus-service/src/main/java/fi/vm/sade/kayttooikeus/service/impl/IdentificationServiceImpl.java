package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.AsiointikieliDto;
import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;
import fi.vm.sade.kayttooikeus.dto.YhteystietojenTyypit;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Identification;
import static fi.vm.sade.kayttooikeus.model.Identification.STRONG_AUTHENTICATION_IDP;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.IdentificationRepository;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;

@Service
public class IdentificationServiceImpl extends AbstractService implements IdentificationService {

    private IdentificationRepository identificationRepository;
    private HenkiloDataRepository henkiloDataRepository;
    private KayttoOikeusService kayttoOikeusService;
    private LdapSynchronizationService ldapSynchronizationService;
    private OrikaBeanMapper mapper;
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Autowired
    public IdentificationServiceImpl(IdentificationRepository identificationRepository,
                                     HenkiloDataRepository henkiloDataRepository,
                                     KayttoOikeusService kayttoOikeusService,
                                     LdapSynchronizationService ldapSynchronizationService,
                                     OrikaBeanMapper mapper,
                                     OppijanumerorekisteriClient oppijanumerorekisteriClient) {
        this.identificationRepository = identificationRepository;
        this.henkiloDataRepository = henkiloDataRepository;
        this.kayttoOikeusService = kayttoOikeusService;
        this.ldapSynchronizationService = ldapSynchronizationService;
        this.mapper = mapper;
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
    }

    @Override
    @Transactional
    public String generateAuthTokenForHenkilo(String oid, String idpKey, String idpIdentifier) {
        logger.info("generateAuthTokenForHenkilo henkilo:[{}] idp:[{}] identifier:[{}]", oid, idpKey, idpIdentifier);
        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(oid).orElseThrow(()
                -> new NotFoundException("no henkilo found with oid:[" + oid + "]"));

        Optional<Identification> identification = henkilo.getIdentifications().stream()
                .filter(henkiloIdentification ->
                        idpIdentifier.equals(henkiloIdentification.getIdentifier()) && idpKey.equals(henkiloIdentification.getIdpEntityId()))
                .findFirst();

        String token = generateToken();
        if (identification.isPresent()) {
            updateToken(identification.get(), token);
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

        HenkiloDto perustiedot = oppijanumerorekisteriClient.getHenkiloByOid(identification.getHenkilo().getOidHenkilo());
        IdentifiedHenkiloTypeDto dto = mapper.map(identification, IdentifiedHenkiloTypeDto.class);
        dto.setHenkiloTyyppi(perustiedot.getHenkiloTyyppi().name());
        dto.setPassivoitu(perustiedot.isPassivoitu());
        dto.setAuthorizationData(kayttoOikeusService.findAuthorizationDataByOid(dto.getOidHenkilo()));

        dto.setEtunimet(perustiedot.getEtunimet());
        dto.setKutsumanimi(perustiedot.getKutsumanimi());
        dto.setSukunimi(perustiedot.getSukunimi());
        dto.setHetu(perustiedot.getHetu());
        if (!StringUtils.isEmpty(perustiedot.getSukupuoli())) {
            dto.setSukupuoli(perustiedot.getSukupuoli().equals("1") ? "MIES" : "NAINEN");
        }
        if (perustiedot.getAsiointiKieli() != null) {
            dto.setAsiointiKieli(new AsiointikieliDto(perustiedot.getAsiointiKieli().getKieliKoodi(), perustiedot.getAsiointiKieli().getKieliTyyppi()));
        }

        YhteystietoUtil.getYhteystietoArvo(perustiedot.getYhteystiedotRyhma(),
                YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI,
                YhteystietojenTyypit.PRIORITY_ORDER).ifPresent(email -> {
                    dto.setEmail(email);
                    identification.setEmail(email);
                });
        return dto;
    }

    @Override
    @Transactional
    public String updateIdentificationAndGenerateTokenForHenkiloByHetu(String hetu) {
        String oid = oppijanumerorekisteriClient.getOidByHetu(hetu);
        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(oid).orElseThrow(()
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

    @Override
    @Transactional(readOnly = true)
    public Set<String> getHakatunnuksetByHenkiloAndIdp(String oid, String ipdKey) {
        List<Identification> identifications = findIdentificationsByHenkiloAndIdp(oid, "haka");
        return identifications.stream().map(Identification::getIdentifier).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Set<String> updateHakatunnuksetByHenkiloAndIdp(String oid, String idpKey, Set<String> hakatunnukset) {
        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(oid).orElseThrow(() -> new NotFoundException("Henkilo not found"));
        List<Identification> identifications = findIdentificationsByHenkiloAndIdp(oid, "haka");
        identificationRepository.delete(identifications);
        List<Identification> updatedIdentifications = hakatunnukset.stream().map(hakatunnus -> new Identification(henkilo, "haka", hakatunnus)).collect(Collectors.toList());
        identificationRepository.save(updatedIdentifications);
        ldapSynchronizationService.updateHenkiloAsap(oid);
        return hakatunnukset;
    }

    private List<Identification> findIdentificationsByHenkiloAndIdp(String oid, String idp) {
        return identificationRepository.findByHenkiloOidHenkiloAndIdpEntityId(oid, idp);
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
    }

    private void updateToken(Identification identification, String token) {
        identification.setAuthtoken(token);
        logger.info("old identification found, setting new token:[{}]", token);
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(128, random).toString(32);
    }

}
