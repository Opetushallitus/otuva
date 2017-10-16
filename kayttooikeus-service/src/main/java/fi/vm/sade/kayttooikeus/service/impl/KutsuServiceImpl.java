package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.dto.KutsuUpdateDto;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkiloCreateByKutsuDto;
import fi.vm.sade.kayttooikeus.service.*;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.validators.KutsuValidator;
import fi.vm.sade.kayttooikeus.util.KutsuHakuBuilder;
import fi.vm.sade.oppijanumerorekisteri.dto.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;

@Service
@RequiredArgsConstructor
public class KutsuServiceImpl implements KutsuService {
    private final KutsuRepository kutsuRepository;
    private final HenkiloDataRepository henkiloDataRepository;

    private final OrikaBeanMapper mapper;
    private final KutsuValidator validator;

    private final EmailService emailService;
    private final LocalizationService localizationService;
    private final CryptoService cryptoService;
    private final KayttajatiedotService kayttajatiedotService;
    private final KayttooikeusAnomusService kayttooikeusAnomusService;
    private final IdentificationService identificationService;
    private final LdapSynchronizationService ldapSynchronizationService;
    private final PermissionCheckerService permissionCheckerService;

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    private final CommonProperties commonProperties;

    @Override
    @Transactional(readOnly = true)
    public List<KutsuReadDto> listKutsus(KutsuOrganisaatioOrder sortBy,
                                         Sort.Direction direction,
                                         KutsuCriteria kutsuCriteria,
                                         Long offset,
                                         Long amount) {
        return new KutsuHakuBuilder(this.permissionCheckerService,
                this.localizationService,
                this.commonProperties,
                this.myonnettyKayttoOikeusRyhmaTapahtumaRepository,
                this.kutsuRepository,
                this.organisaatioHenkiloRepository,
                this.mapper,
                kutsuCriteria)
                .prepareCommon()
                .doSearch(sortBy, direction, offset, amount)
                .localise()
                .build();
    }

    @Override
    @Transactional
    public long createKutsu(KutsuCreateDto dto) {
         if (!kutsuRepository.findBySahkopostiAndTila(dto.getSahkoposti(), AVOIN).isEmpty()) {
             throw new IllegalArgumentException("kutsu_with_sahkoposti_already_sent");
         }

        final Kutsu newKutsu = mapper.map(dto, Kutsu.class);

        newKutsu.setId(null);
        newKutsu.setAikaleima(LocalDateTime.now());
        newKutsu.setKutsuja(this.permissionCheckerService.getCurrentUserOid());
        newKutsu.setSalaisuus(UUID.randomUUID().toString());
        newKutsu.setTila(AVOIN);
        newKutsu.getOrganisaatiot().forEach(kutsuOrganisaatio -> kutsuOrganisaatio.setKutsu(newKutsu));

        validator.validate(newKutsu);

        Kutsu persistedNewKutsu = this.kutsuRepository.save(newKutsu);
        
        this.emailService.sendInvitationEmail(persistedNewKutsu);

        return persistedNewKutsu.getId();
    }


    @Override
    @Transactional(readOnly = true)
    public KutsuReadDto getKutsu(Long id) {
        Kutsu kutsu = kutsuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        KutsuReadDto kutsuReadDto = mapper.map(kutsu, KutsuReadDto.class);
        this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot());
        return kutsuReadDto;
    }

    @Override
    @Transactional
    public Kutsu deleteKutsu(long id) {
        Kutsu deletedKutsu = kutsuRepository.findById(id)
                .filter(kutsu -> this.permissionCheckerService.isCurrentUserAdmin()
                        || kutsu.getKutsuja().equals(this.permissionCheckerService.getCurrentUserOid()))
                .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        deletedKutsu.poista(this.permissionCheckerService.getCurrentUserOid());
        return deletedKutsu;
    }

    @Override
    @Transactional(readOnly = true)
    public KutsuReadDto getByTemporaryToken(String temporaryToken) {
        Kutsu kutsuByToken = this.kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken + " or token is invalid"));
        KutsuReadDto kutsuReadDto = this.mapper.map(kutsuByToken, KutsuReadDto.class);
        this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot());
        return kutsuReadDto;
    }

    @Override
    @Transactional
    public String createHenkilo(String temporaryToken, HenkiloCreateByKutsuDto henkiloCreateByKutsuDto) {
        Kutsu kutsuByToken = this.kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken + " or token is invalid"));
        if(StringUtils.isEmpty(kutsuByToken.getHakaIdentifier())) {
            // Validation
            this.cryptoService.throwIfNotStrongPassword(henkiloCreateByKutsuDto.getPassword());
            this.kayttajatiedotService.throwIfUsernameExists(henkiloCreateByKutsuDto.getKayttajanimi());
            this.kayttajatiedotService.throwIfUsernameIsNotValid(henkiloCreateByKutsuDto.getKayttajanimi());
        }

        // Create henkilo
        String henkiloOid = this.oppijanumerorekisteriClient
                .createHenkiloForKutsu(this.getHenkiloCreateDto(henkiloCreateByKutsuDto, kutsuByToken))
                .orElseGet(() -> this.oppijanumerorekisteriClient.getOidByHetu(kutsuByToken.getHetu()));

        // Set henkilo strongly identified
        Henkilo henkilo = this.henkiloDataRepository.findByOidHenkilo(henkiloOid)
                .orElseGet(() -> this.henkiloDataRepository.save(Henkilo.builder().oidHenkilo(henkiloOid).build()));
        henkilo.setVahvastiTunnistettu(true);

        // Create or update credentials and add privileges
        this.createOrUpdateCredentialsAndPrivileges(henkiloCreateByKutsuDto, kutsuByToken, henkiloOid);

        // Update kutsu
        kutsuByToken.setKaytetty(LocalDateTime.now());
        kutsuByToken.setTemporaryToken(null);
        kutsuByToken.setLuotuHenkiloOid(henkiloOid);
        kutsuByToken.setTila(KutsunTila.KAYTETTY);

        this.ldapSynchronizationService.updateHenkiloAsap(henkiloOid);
        return henkiloOid;
    }

    // In case virkailija already exists
    private void createOrUpdateCredentialsAndPrivileges(HenkiloCreateByKutsuDto henkiloCreateByKutsuDto, Kutsu kutsuByToken, String henkiloOid) {
        Optional<Kayttajatiedot> kayttajatiedot = this.kayttajatiedotService.getKayttajatiedotByOidHenkilo(henkiloOid);
        // Create username/password and haka identifier if provided
        if(StringUtils.hasLength(kutsuByToken.getHakaIdentifier())) {
            // If haka identifier is provided add it to henkilo identifiers
            Set<String> hakaIdentifiers = this.identificationService.getHakatunnuksetByHenkiloAndIdp(henkiloOid);
            hakaIdentifiers.add(kutsuByToken.getHakaIdentifier());
            this.identificationService.updateHakatunnuksetByHenkiloAndIdp(henkiloOid, hakaIdentifiers);
            if(!kayttajatiedot.isPresent() || StringUtils.isEmpty(kayttajatiedot.get().getUsername())) {
                this.createHakaUsername(henkiloCreateByKutsuDto, kutsuByToken);
            }
        }
        this.kayttajatiedotService.createOrUpdateUsername(henkiloOid, henkiloCreateByKutsuDto.getKayttajanimi(),
                LdapSynchronizationService.LdapSynchronizationType.ASAP);
        if(StringUtils.isEmpty(kutsuByToken.getHakaIdentifier())) {
            this.kayttajatiedotService.changePasswordAsAdmin(henkiloOid, henkiloCreateByKutsuDto.getPassword());
        }

        kutsuByToken.getOrganisaatiot().forEach(kutsuOrganisaatio ->
                this.kayttooikeusAnomusService.grantKayttooikeusryhmaAsAdminWithoutPermissionCheck(
                        henkiloOid,
                        kutsuOrganisaatio.getOrganisaatioOid(),
                        kutsuOrganisaatio.getRyhmat(),
                        kutsuByToken.getKutsuja()));

    }

    private void createHakaUsername(HenkiloCreateByKutsuDto henkiloCreateByKutsuDto, Kutsu kutsuByToken) {
        String parsedIdentifier = kutsuByToken.getHakaIdentifier().replaceAll("[^A-Za-z0-9]", "");
        String username = parsedIdentifier + (new Random().nextInt(900) + 100); // 100-999
        // This username that is not meant to be used for CAS authentication
        // CAS does not accept empty password authentication so this is fine without password
        henkiloCreateByKutsuDto.setKayttajanimi(username);
    }

    @NotNull
    private HenkiloCreateDto getHenkiloCreateDto(HenkiloCreateByKutsuDto henkiloCreateByKutsuDto, Kutsu kutsuByToken) {
        HenkiloCreateDto henkiloCreateDto = new HenkiloCreateDto();
        henkiloCreateDto.setHetu(kutsuByToken.getHetu());
        henkiloCreateDto.setAsiointiKieli(henkiloCreateByKutsuDto.getAsiointiKieli());
        henkiloCreateDto.setEtunimet(kutsuByToken.getEtunimi());
        henkiloCreateDto.setSukunimi(kutsuByToken.getSukunimi());
        henkiloCreateDto.setKutsumanimi(henkiloCreateByKutsuDto.getKutsumanimi());
        henkiloCreateDto.setHenkiloTyyppi(HenkiloTyyppi.VIRKAILIJA);
        henkiloCreateDto.setYhteystiedotRyhma(Sets.newHashSet(YhteystiedotRyhmaDto.builder()
                .ryhmaAlkuperaTieto(this.commonProperties.getYhteystiedotRyhmaAlkuperaVirkailijaUi())
                .ryhmaKuvaus(this.commonProperties.getYhteystiedotRyhmaKuvausTyoosoite())
                .yhteystieto(YhteystietoDto.builder()
                        .yhteystietoArvo(kutsuByToken.getSahkoposti())
                        .yhteystietoTyyppi(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI)
                        .build()).build()));
        return henkiloCreateDto;
    }

    @Override
    @Transactional
    // Nulls are not mapped for KutsuUpdateDto -> Kutsu
    public void updateHakaIdentifierToKutsu(String temporaryToken, KutsuUpdateDto kutsuUpdateDto) {
        Kutsu kutsu = this.kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken + " or token is invalid"));
        this.mapper.map(kutsuUpdateDto, kutsu);
    }
}
