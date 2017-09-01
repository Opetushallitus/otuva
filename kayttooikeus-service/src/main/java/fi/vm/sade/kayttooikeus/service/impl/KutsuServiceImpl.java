package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.Kutsu;
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

import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;

@Service
@RequiredArgsConstructor
public class KutsuServiceImpl extends AbstractService implements KutsuService {
    private final KutsuRepository kutsuRepository;
    private final OrikaBeanMapper mapper;
    private final KutsuValidator validator;

    private final EmailService emailService;
    private final LocalizationService localizationService;

    @Override
    @Transactional(readOnly = true)
    public List<KutsuReadDto> listKutsus(KutsuOrganisaatioOrder sortBy,
                                         Sort.Direction direction,
                                         KutsuCriteria kutsuListCriteria,
                                         Long offset,
                                         Long amount) {
        List<KutsuReadDto> kutsuReadDtoList = this.mapper.mapAsList(this.kutsuRepository.listKutsuListDtos(kutsuListCriteria,
                sortBy.getSortWithDirection(direction), offset, amount), KutsuReadDto.class);
        kutsuReadDtoList.forEach(kutsuReadDto -> this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot()));

        return kutsuReadDtoList;
    }

    @Override
    @Transactional
    public long createKutsu(KutsuCreateDto dto) {
         if (!kutsuRepository.listKutsuListDtos(new KutsuCriteria().withTila(AVOIN).withSahkoposti(dto.getSahkoposti()),
                 KutsuOrganisaatioOrder.AIKALEIMA.getSortWithDirection()).isEmpty()) {
             throw new IllegalArgumentException("kutsu_with_sahkoposti_already_sent");
         }

        final Kutsu newKutsu = mapper.map(dto, Kutsu.class);

        newKutsu.setId(null);
        newKutsu.setAikaleima(LocalDateTime.now());
        newKutsu.setKutsuja(getCurrentUserOid());
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
        Kutsu deletedKutsu = kutsuRepository.findById(id).filter(kutsu -> kutsu.getKutsuja().equals(getCurrentUserOid()))
            .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        deletedKutsu.poista(getCurrentUserOid());
        return deletedKutsu;
    }
}
