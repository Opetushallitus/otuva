package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.KutsuDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.service.EmailService;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.validators.KutsuValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;

@Service
@RequiredArgsConstructor
public class KutsuServiceImpl extends AbstractService implements KutsuService {
    private final KutsuRepository kutsuRepository;
    private final KutsuDataRepository kutsuDataRepository;
    private final OrikaBeanMapper mapper;
    private final KutsuValidator validator;

    private final EmailService emailService;
    private final LocalizationService localizationService;

    @Override
    @Transactional(readOnly = true)
    public List<KutsuReadDto> listAvoinKutsus(KutsuOrganisaatioOrder sortBy, Sort.Direction direction, boolean onlyOwnKutsus) {
        final Sort sort = sortBy.getSortWithDirection(direction);
        Supplier<List<Kutsu>> findMethod = onlyOwnKutsus
                ? () -> this.kutsuDataRepository.findByTilaAndKutsuja(sort, AVOIN, getCurrentUserOid())
                : () -> this.kutsuDataRepository.findByTila(sort, AVOIN);
        List<KutsuReadDto> kutsuReadDtoList = this.mapper.mapAsList(findMethod.get(), KutsuReadDto.class);
        kutsuReadDtoList.forEach(kutsuReadDto -> this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot()));

        return kutsuReadDtoList;
    }

    @Override
    @Transactional
    public long createKutsu(KutsuCreateDto dto) {
         if (!kutsuRepository.listKutsuListDtos(new KutsuCriteria().withTila(AVOIN).withSahkoposti(dto.getSahkoposti())).isEmpty()) {
             throw new IllegalArgumentException("kutsu_with_sahkoposti_already_sent");
         }
        
        Kutsu entity = mapper.map(dto, Kutsu.class);

        entity.setId(null);
        entity.setAikaleima(LocalDateTime.now());
        entity.setKutsuja(getCurrentUserOid());
        entity.setSalaisuus(UUID.randomUUID().toString());
        entity.setTila(AVOIN);

        validator.validate(entity);

        entity = kutsuRepository.persist(entity);
        
        this.emailService.sendInvitationEmail(entity);

        return entity.getId();
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
        Kutsu deletedKutsu = kutsuRepository.findById(id).filter(kutsu -> kutsu.getKutsuja().equals(getCurrentUserOid()))
            .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        deletedKutsu.poista(getCurrentUserOid());
        return deletedKutsu;
    }

    @Override
    @Transactional(readOnly = true)
    public KutsuReadDto getByTemporaryToken(String temporaryToken) {
        Kutsu kutsuByToken = this.kutsuDataRepository.findByTemporaryToken(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken));
        KutsuReadDto kutsuReadDto = this.mapper.map(kutsuByToken, KutsuReadDto.class);
        this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot());
        return kutsuReadDto;
    }
}
