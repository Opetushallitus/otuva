package fi.vm.sade.kayttooikeus.service.impl.ldap;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.controller.LdapRoolitBuilder;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttaja;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.Ryhma;
import fi.vm.sade.kayttooikeus.repositories.KayttajaRepository;
import fi.vm.sade.kayttooikeus.repositories.RyhmaRepository;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Palvelu LDAP:n käsittelyyn ({@link Kayttaja} ja {@link Ryhma}).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LdapService {

    private final KayttajaRepository kayttajaRepository;
    private final RyhmaRepository ryhmaRepository;
    private final OrikaBeanMapper mapper;

    /**
     * Lisää tai päivittää henkilön tiedot.
     *
     * @param entity käyttöoikeuspalvelu henkilö
     * @param dto oppijanumerorekisteri henkilö
     * @param myonnetyt henkilölle myönnetyt käyttöoikeudet
     */
    public void upsert(Henkilo entity, HenkiloDto dto, List<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnetyt) {
        String kayttajatunnus = entity.getKayttajatiedot().getUsername();
        Kayttaja kayttaja = kayttajaRepository.findByKayttajatunnus(kayttajatunnus).orElseGet(()
                -> Kayttaja.builder().kayttajatunnus(kayttajatunnus).build());

        // muodostetaan henkilön ldap-roolit
        LdapRoolitBuilder roolit = new LdapRoolitBuilder()
                // käyttöoikeuspalvelun roolit
                .identifications(entity.getIdentifications())
                .myonnetyt(myonnetyt)
                // oppijanumerorekisterin roolit
                .henkiloTyyppi(dto.getHenkiloTyyppi())
                .asiointikieli(dto.getAsiointiKieli());

        // päivitetään käyttäjän perustiedot
        mapper.map(entity, kayttaja);
        mapper.map(dto, kayttaja);
        kayttaja.setRoolit(roolit.asString());
        kayttajaRepository.save(kayttaja);

        // päivitetään käyttäjän ryhmät
        String jasen = kayttaja.getDnAsString();
        List<Ryhma> ldapRyhmat = ryhmaRepository.findByJasenet(jasen);
        Set<String> ldapRoolit = ldapRyhmat.stream().map(Ryhma::getNimi).collect(toSet());
        Set<String> dbRoolit = roolit.asSet();
        ldapRyhmat.stream()
                .filter(ldapRyhma -> !dbRoolit.contains(ldapRyhma.getNimi()))
                .forEach(ldapRyhma -> deleteFromRyhma(ldapRyhma, jasen));
        dbRoolit.stream()
                .filter(dbRooli -> !ldapRoolit.contains(dbRooli))
                .forEach(dbRooli -> addToRyhma(dbRooli, jasen));
    }

    /**
     * Poistaa henkilön tiedot.
     *
     * @param kayttajatunnus käyttäjätunnus
     */
    public void delete(String kayttajatunnus) {
        kayttajaRepository.findByKayttajatunnus(kayttajatunnus)
                .ifPresent(this::delete);
    }

    private void delete(Kayttaja kayttaja) {
        kayttajaRepository.delete(kayttaja);
        String jasen = kayttaja.getDnAsString();
        ryhmaRepository.findByJasenet(jasen)
                .forEach(t -> deleteFromRyhma(t, jasen));
    }

    private void addToRyhma(String ryhmaNimi, String jasen) {
        Ryhma ryhma = ryhmaRepository.findByNimi(ryhmaNimi)
                .orElseGet(() -> Ryhma.builder().nimi(ryhmaNimi).build());
        addToRyhma(ryhma, jasen);
    }

    private void addToRyhma(Ryhma ryhma, String jasen) {
        if (ryhma.addJasen(jasen)) {
            ryhmaRepository.save(ryhma);
        }
    }

    private void deleteFromRyhma(Ryhma ryhma, String jasen) {
        if (ryhma.deleteJasen(jasen)) {
            if (ryhma.isEmpty()) {
                ryhmaRepository.delete(ryhma);
            } else {
                ryhmaRepository.save(ryhma);
            }
        }
    }

}
