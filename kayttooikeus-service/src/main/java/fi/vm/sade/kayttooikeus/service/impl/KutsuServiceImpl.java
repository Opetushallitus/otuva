package fi.vm.sade.kayttooikeus.service.impl;

import com.google.gson.internal.LinkedHashTreeMap;
import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsuOrganisaatioListDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.repositories.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.OrderBy;
import fi.vm.sade.kayttooikeus.service.KutsuService;
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
import static fi.vm.sade.kayttooikeus.model.KutsunTila.AVOIN;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.ORGANISAATIO;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
public class KutsuServiceImpl extends AbstractService implements KutsuService {
    private final KutsuRepository kutsuDao;
    private final OrganisaatioClient organisaatioClient;

    @Autowired
    public KutsuServiceImpl(KutsuRepository kutsuDao, OrganisaatioClient organisaatioClient) {
        this.kutsuDao = kutsuDao;
        this.organisaatioClient = organisaatioClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KutsuListDto> listAvoinKutsus(OrderBy<KutsuOrganisaatioOrder> orderBy) {
        KutsuCriteria criteria = new KutsuCriteria().withTila(AVOIN)
                .withKutsuja(getCurrentUserOid());
        Map<Long,KutsuListDto> byIds = kutsuDao.listKutsuListDtos(criteria).stream().collect(toMap(KutsuListDto::getId, identity()));
        List<KutsuOrganisaatioListDto> kutsuOrganisaatios = kutsuDao.listKutsuOrganisaatioListDtos(criteria, orderBy);
        Map<String,List<KutsuOrganisaatioListDto>> byOrganisaatioOids = kutsuOrganisaatios.stream()
                .collect(groupingBy(KutsuOrganisaatioListDto::getOid));
        organisaatioClient.listActiveOganisaatioPerustiedot(byOrganisaatioOids.keySet())
                .stream().map(perustiedot -> new SimpleEntry<>(perustiedot.getOid(), new TextGroupMapDto(null, perustiedot.getNimi())))
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
}
