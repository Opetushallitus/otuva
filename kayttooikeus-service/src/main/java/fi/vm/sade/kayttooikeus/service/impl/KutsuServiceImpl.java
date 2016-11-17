package fi.vm.sade.kayttooikeus.service.impl;

import com.google.gson.internal.LinkedHashTreeMap;
import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsuOrganisaatioListDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.OrderBy;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.querydsl.core.types.Order.DESC;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.ORGANISAATIO;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import org.joda.time.DateTime;

@Service
public class KutsuServiceImpl extends AbstractService implements KutsuService {

    private final KutsuRepository kutsuRepository;
    private final OrikaBeanMapper mapper;
    private final KutsuValidator validator;
    private final OrganisaatioClient organisaatioClient;

    @Autowired
    public KutsuServiceImpl(KutsuRepository kutsuRepository,
            OrikaBeanMapper mapper, KutsuValidator validator,
            OrganisaatioClient organisaatioClient) {
        this.kutsuRepository = kutsuRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.organisaatioClient = organisaatioClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KutsuListDto> listAvoinKutsus(OrderBy<KutsuOrganisaatioOrder> orderBy) {
        KutsuCriteria criteria = new KutsuCriteria().withTila(AVOIN)
                .withKutsuja(getCurrentUserOid());
        Map<Long,KutsuListDto> byIds = kutsuRepository.listKutsuListDtos(criteria).stream().collect(toMap(KutsuListDto::getId, identity()));
        List<KutsuOrganisaatioListDto> kutsuOrganisaatios = kutsuRepository.listKutsuOrganisaatioListDtos(criteria, orderBy);
        Map<String,List<KutsuOrganisaatioListDto>> byOrganisaatioOids = kutsuOrganisaatios.stream()
                .collect(groupingBy(KutsuOrganisaatioListDto::getOid));
        byOrganisaatioOids.keySet().stream().map(organisaatioClient::getOrganisaatioPerustiedot)
                .filter(perustiedot -> byOrganisaatioOids.containsKey(perustiedot.getOid()))
                .map(perustiedot -> new SimpleEntry<>(perustiedot.getOid(), new TextGroupMapDto(null, perustiedot.getNimi())))
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
        Kutsu entity = mapper.map(dto, Kutsu.class);

        entity.setId(null);
        entity.setAikaleima(DateTime.now());
        entity.setKutsuja(getCurrentUserOid());
        entity.setTila(AVOIN);

        validator.validate(entity);

        entity = kutsuRepository.persist(entity);

        return entity.getId();
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
