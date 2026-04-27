package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.VahvaTunnistusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VahvaTunnistusServiceImpl implements VahvaTunnistusService {
    private final IdentificationService identificationService;

    @Value("${url-virkailija}")
    private String urlVirkailija;

    @Override
    public String kasitteleKutsunTunnistus(String kutsuToken, String kielisyys, String hetu, String etunimet, String sukunimi) {
        return identificationService.updateKutsuAndGenerateTemporaryKutsuToken(kutsuToken, hetu, etunimet, sukunimi)
                .map(temporaryKutsuToken -> urlVirkailija + "/henkilo-ui/kayttaja/rekisteroidy?temporaryKutsuToken=" + temporaryKutsuToken)
                .orElseGet(() -> urlVirkailija + "/henkilo-ui/kayttaja/kutsu/vanhentunut/" + kielisyys);
    }
}
