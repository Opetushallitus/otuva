package fi.vm.sade.kayttooikeus.service.impl;

import com.google.gson.internal.LinkedHashTreeMap;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties.InvitationEmail;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.OrderBy;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient.Mode;
import fi.vm.sade.kayttooikeus.service.external.RyhmasahkopostiClient;
import fi.vm.sade.kayttooikeus.service.validators.KutsuValidator;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailMessage;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipient;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedRecipientReplacementDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Stream;

import static com.querydsl.core.types.Order.DESC;
import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.ORGANISAATIO;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
public class KutsuServiceImpl extends AbstractService implements KutsuService {
    private static final String CALLING_PROCESS = "kayttooikeus.kayttooikeuspalvelu-service";
    private final KutsuRepository kutsuRepository;
    private final OrikaBeanMapper mapper;
    private final KutsuValidator validator;
    private final OrganisaatioClient organisaatioClient;
    private final RyhmasahkopostiClient ryhmasahkopostiClient;
    private final OphProperties urlProperties;
    private final InvitationEmail invitationEmail;

    @Autowired
    public KutsuServiceImpl(KutsuRepository kutsuRepository,
                            OrikaBeanMapper mapper, KutsuValidator validator,
                            OrganisaatioClient organisaatioClient,
                            RyhmasahkopostiClient ryhmasahkopostiClient,
                            OphProperties urlProperties,
                            CommonProperties commonProperties) {
        this.kutsuRepository = kutsuRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.organisaatioClient = organisaatioClient;
        this.ryhmasahkopostiClient = ryhmasahkopostiClient;
        this.urlProperties = urlProperties;
        this.invitationEmail = commonProperties.getInvitationEmail();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KutsuListDto> listAvoinKutsus(OrderBy<KutsuOrganisaatioOrder> orderBy) {
        Mode organizationClientState = Mode.multiple();
        KutsuCriteria criteria = new KutsuCriteria().withTila(AVOIN)
                .withKutsuja(getCurrentUserOid());
        Map<Long,KutsuListDto> byIds = kutsuRepository.listKutsuListDtos(criteria).stream().collect(toMap(KutsuListDto::getId, identity()));
        List<KutsuOrganisaatioListDto> kutsuOrganisaatios = kutsuRepository.listKutsuOrganisaatioListDtos(criteria, orderBy);
        Map<String,List<KutsuOrganisaatioListDto>> byOrganisaatioOids = kutsuOrganisaatios.stream()
                .collect(groupingBy(KutsuOrganisaatioListDto::getOid));
        byOrganisaatioOids.keySet().stream().map(oid -> organisaatioClient.getOrganisaatioPerustiedotCached(oid, organizationClientState))
                .filter(perustiedot -> byOrganisaatioOids.containsKey(perustiedot.getOid()))
                .map(perustiedot -> new SimpleEntry<>(perustiedot.getOid(), new TextGroupMapDto(perustiedot.getNimi())))
                .forEach(e -> byOrganisaatioOids.get(e.getKey()).forEach(dto -> dto.setNimi(e.getValue())));
        Stream<KutsuOrganisaatioListDto> kutsuOrganisaaStream = kutsuOrganisaatios.stream();
        if (orderBy.getBy() == ORGANISAATIO) {
            Comparator<KutsuOrganisaatioListDto> nimiComp = comparing(KutsuOrganisaatioListDto::getNimi);
            kutsuOrganisaaStream = kutsuOrganisaaStream.sorted(orderBy.getDirection() == DESC ? nimiComp.reversed() : nimiComp);
        }
        kutsuOrganisaaStream = kutsuOrganisaaStream.peek(kutsuOrganisaatio ->
                byIds.get(kutsuOrganisaatio.getKutsuId()).getOrganisaatiot().add(kutsuOrganisaatio));
        return kutsuOrganisaaStream.collect(groupingBy(KutsuOrganisaatioListDto::getKutsuId, LinkedHashTreeMap::new, toList()))
            .keySet().stream().map(byIds::get).collect(toList());
    }

    @Override
    @Transactional
    public long createKutsu(KutsuCreateDto dto) {
         if (!kutsuRepository.listKutsuListDtos(new KutsuCriteria().withTila(AVOIN).withSahkoposti(dto.getSahkoposti())).isEmpty()) {
             throw new IllegalArgumentException("kutsu_with_sahkoposti_already_sent");
         }
        
        Kutsu entity = mapper.map(dto, Kutsu.class);

        entity.setId(null);
        entity.setAikaleima(DateTime.now());
        entity.setKutsuja(getCurrentUserOid());
        entity.setSalsisuus(UUID.randomUUID().toString());
        entity.setTila(AVOIN);

        validator.validate(entity);

        entity = kutsuRepository.persist(entity);
        
        sendInvitationEmail(entity);

        return entity.getId();
    }

    private void sendInvitationEmail(Kutsu kutsu) {
        EmailData emailData = new EmailData();
        
        EmailMessage email = new EmailMessage();
        email.setTemplateName(invitationEmail.getTemplate());
        email.setLanguageCode(kutsu.getKieliKoodi());
        email.setCallingProcess(CALLING_PROCESS);
        email.setFrom(invitationEmail.getFrom());
        email.setCharset("UTF-8");
        email.setHtml(true);
        email.setSender(invitationEmail.getSender());
        emailData.setEmail(email);
        
        EmailRecipient recipient = new EmailRecipient();
        recipient.setEmail(kutsu.getSahkoposti());
        recipient.setLanguageCode(kutsu.getKieliKoodi());

        Mode organizationClientState = Mode.multiple();
        recipient.setRecipientReplacements(asList(
                replacement("url", urlProperties.url("kayttooikeus-service.invitation.url", kutsu.getSalsisuus())),
                replacement("etunimi", kutsu.getEtunimi()),
                replacement("sukunimi", kutsu.getSukunimi()),
                replacement("organisaatiot", kutsu.getOrganisaatiot().stream()
                    .map(org -> new OranizationReplacement(new TextGroupMapDto(
                            organisaatioClient.getOrganisaatioPerustiedotCached(org.getOrganisaatioOid(),
                                    organizationClientState).getNimi()).getOrAny(kutsu.getKieliKoodi()).orElse(null),
                            org.getRyhmat().stream().map(KayttoOikeusRyhma::getDescription)
                                .map(desc -> desc.getOrAny(kutsu.getKieliKoodi()).orElse(null))
                                .filter(Objects::nonNull).sorted().collect(toList())
                        )
                    ).sorted(comparing(OranizationReplacement::getName)).collect(toList()))
        ));
        emailData.setRecipient(singletonList(recipient));
        
        logger.info("Sending invitation email to {}", kutsu.getSahkoposti());
        HttpResponse response = ryhmasahkopostiClient.sendRyhmasahkoposti(emailData);
        try {
            logger.info("Sent invitation email to {}, ryhmasahkoposti-result: {}", kutsu.getSahkoposti(),
                    IOUtils.toString(response.getEntity().getContent()));
        } catch (IOException e) {
            logger.error("Could not read ryhmasahkoposti-result: " + e.getMessage(), e);
        }
    }
    
    @Getter
    @AllArgsConstructor
    public static class OranizationReplacement {
        private final String name;
        private final List<String> permissions;
    }

    @NotNull
    private ReportedRecipientReplacementDTO replacement(String name, Object value) {
        ReportedRecipientReplacementDTO replacement = new ReportedRecipientReplacementDTO();
        replacement.setName(name);
        replacement.setValue(value);
        return replacement;
    }

    @Override
    @Transactional(readOnly = true)
    public KutsuReadDto getKutsu(Long id) {
        Kutsu kutsu = kutsuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        return mapper.map(kutsu, KutsuReadDto.class);
    }

    @Override
    @Transactional
    public void deleteKutsu(long id) {
        kutsuRepository.findById(id).filter(kutsu -> kutsu.getKutsuja().equals(getCurrentUserOid()))
            .orElseThrow(() -> new NotFoundException("Kutsu not found"))
            .poista(getCurrentUserOid());
    }
}
