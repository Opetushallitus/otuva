package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloCacheModified;
import fi.vm.sade.kayttooikeus.repositories.HenkiloCacheModifiedDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.service.HenkiloCacheService;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloHakuPerustietoDto;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HenkiloCacheServiceImpl implements HenkiloCacheService {
    private static final Logger LOG = LoggerFactory.getLogger(HenkiloCacheServiceImpl.class);

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final HenkiloDataRepository henkiloDataRepository;
    private final HenkiloCacheModifiedDataRepository henkiloCacheModifiedDataRepository;

    @Autowired
    public HenkiloCacheServiceImpl(OppijanumerorekisteriClient oppijanumerorekisteriClient,
                                   HenkiloDataRepository henkiloDataRepository,
                                   HenkiloCacheModifiedDataRepository henkiloCacheModifiedDataRepository) {
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        this.henkiloDataRepository = henkiloDataRepository;
        this.henkiloCacheModifiedDataRepository = henkiloCacheModifiedDataRepository;
    }

    @Override
    @Transactional
    public void updateHenkiloCache() {
        HenkiloCacheModified henkiloCacheModified = this.henkiloCacheModifiedDataRepository.findFirstBy()
                .orElseThrow(DataInconsistencyException::new);
        List<String> modifiedOidHenkiloList = new ArrayList<>();
        long amount = 1000L;
        for(long offset = 0; offset == 0 || !modifiedOidHenkiloList.isEmpty() || !(modifiedOidHenkiloList.size() < amount); offset++) {
            modifiedOidHenkiloList = this.oppijanumerorekisteriClient.getModifiedSince(henkiloCacheModified.getModified(),
                    offset*amount, amount);
            if(!modifiedOidHenkiloList.isEmpty()) {
                this.saveAll(0, amount, modifiedOidHenkiloList);
            }
        }
        henkiloCacheModified.setModified(DateTime.now());
    }

    // Do in single transaction so if something fails results are not partially saved (and missing ones are never fetched again)
    @Override
    @Transactional
    public void forceUpdateHenkiloCache() {
        Long count = 1000L;
        for(long offset = 0; !this.saveAll(offset*count, count, null); offset++) {
            // Escape condition in case of inifine loop (10M+ henkilos)
            if(offset > 10000) {
                LOG.error("Infinite loop detected with page "+ offset + " and count " + count + ". Henkilo cache might not be fully updated!");
                break;
            }
        }
        this.henkiloCacheModifiedDataRepository.findFirstBy()
                .orElseThrow(DataInconsistencyException::new)
                .setModified(DateTime.now());
    }

    private boolean saveAll(long offset, long count, List<String> oidHenkiloList) {
        final List<Henkilo> saveList = new ArrayList<>();
        final List<HenkiloHakuPerustietoDto> onrHenkilohakuResultDto
                = this.oppijanumerorekisteriClient.getAllByOids(offset, count, oidHenkiloList);
        final List<Henkilo> matchingHenkiloList = this.henkiloDataRepository.findByOidHenkiloIn(
                onrHenkilohakuResultDto.stream().map(HenkiloHakuPerustietoDto::getOidHenkilo).collect(Collectors.toList()));

        onrHenkilohakuResultDto.forEach(henkiloHakuDto -> {
            // Find or create matching henkilo
            Henkilo matchingHenkilo = matchingHenkiloList.stream()
                    .filter(henkilo -> henkilo.getOidHenkilo().equals(henkiloHakuDto.getOidHenkilo()))
                    .findFirst()
                    .orElseGet(() -> this.henkiloDataRepository.save(new Henkilo(henkiloHakuDto.getOidHenkilo())));
            matchingHenkilo.setEtunimetCached(henkiloHakuDto.getEtunimet());
            matchingHenkilo.setSukunimiCached(henkiloHakuDto.getSukunimi());
            matchingHenkilo.setDuplicateCached(henkiloHakuDto.getDuplicate());
            matchingHenkilo.setPassivoituCached(henkiloHakuDto.getPassivoitu());
            saveList.add(matchingHenkilo);
        });
        this.henkiloDataRepository.save(saveList);
        return onrHenkilohakuResultDto.isEmpty() || onrHenkilohakuResultDto.size() < count;
    }
}
