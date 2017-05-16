package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.MyonnettyKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Helper class to create email data for approved/rejected access right
 * requisition.
 */
public class AnomusKasiteltySahkopostiBuilder {

    private final KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;
    private final String languageCode;
    private final Collection<MyonnettyKayttoOikeusDto> kasitellyt;
    private final Collection<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnetyt;
    private final Collection<HaettuKayttoOikeusRyhma> hylatyt;

    /**
     * Constructor.
     *
     * @param kayttoOikeusRyhmaRepository access right group data access object
     * @param languageCode language code which is used to create email data
     */
    public AnomusKasiteltySahkopostiBuilder(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository, String languageCode) {
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
        this.languageCode = languageCode;
        this.kasitellyt = new ArrayList<>();
        this.myonnetyt = new ArrayList<>();
        this.hylatyt = new ArrayList<>();
    }

    /**
     * Returns access right group data for email.
     *
     * @return access right group data
     */
    public Iterable<RooliDto> build() {
        List<RooliDto> lista = new ArrayList<>();
        for (MyonnettyKayttoOikeusDto myonnettyKayttoOikeus : this.kasitellyt) {
            lista.add(newRooli(myonnettyKayttoOikeus));
        }
        for (MyonnettyKayttoOikeusRyhmaTapahtuma haettuKayttoOikeusRyhma : this.myonnetyt) {
            lista.add(newRooli(haettuKayttoOikeusRyhma, KayttoOikeudenTila.MYONNETTY));
        }
        for (HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma : this.hylatyt) {
            lista.add(newRooli(haettuKayttoOikeusRyhma, KayttoOikeudenTila.HYLATTY));
        }
        return lista;
    }

    public AnomusKasiteltySahkopostiBuilder kasitellyt(Collection<MyonnettyKayttoOikeusDto> myonnetytKayttoOikeudet) {
        this.kasitellyt.addAll(myonnetytKayttoOikeudet);
        return this;
    }

    public AnomusKasiteltySahkopostiBuilder myonnetyt(Collection<MyonnettyKayttoOikeusRyhmaTapahtuma> haetutKayttoOikeudet) {
        this.myonnetyt.addAll(haetutKayttoOikeudet);
        return this;
    }

    public AnomusKasiteltySahkopostiBuilder hylatyt(Collection<HaettuKayttoOikeusRyhma> haetutKayttoOikeudet) {
        this.hylatyt.addAll(haetutKayttoOikeudet);
        return this;
    }

    private RooliDto newRooli(MyonnettyKayttoOikeusDto myonnettyKayttoOikeus) {
        KayttoOikeusRyhma kayttoOikeusRyhma = this.kayttoOikeusRyhmaRepository.findById(myonnettyKayttoOikeus.getRyhmaId())
                .orElseThrow(() -> new NotFoundException("Kayttooikeusryhma not found with id " + myonnettyKayttoOikeus.getRyhmaId()));
        return newRooli(kayttoOikeusRyhma, myonnettyKayttoOikeus.getTila());
    }

    private RooliDto newRooli(MyonnettyKayttoOikeusRyhmaTapahtuma haettuKayttoOikeusRyhma, KayttoOikeudenTila tila) {
        KayttoOikeusRyhma kayttoOikeusRyhma = haettuKayttoOikeusRyhma.getKayttoOikeusRyhma();
        return newRooli(kayttoOikeusRyhma, tila);
    }

    private RooliDto newRooli(HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma, KayttoOikeudenTila tila) {
        KayttoOikeusRyhma kayttoOikeusRyhma = haettuKayttoOikeusRyhma.getKayttoOikeusRyhma();
        return newRooli(kayttoOikeusRyhma, tila);
    }

    private RooliDto newRooli(final KayttoOikeusRyhma kayttoOikeusRyhma, KayttoOikeudenTila tila) {
        String kayttoOikeusRyhmaNimi = LocalisationUtils.getText(this.languageCode, kayttoOikeusRyhma.getDescription(),
                kayttoOikeusRyhma::getName);
        return new RooliDto(kayttoOikeusRyhmaNimi, tila);
    }

    public static class RooliDto {

        private final String nimi;
        private final KayttoOikeudenTila tila;

        public RooliDto(String nimi, KayttoOikeudenTila tila) {
            this.nimi = nimi;
            this.tila = tila;
        }

        public String getNimi() {
            return nimi;
        }

        public KayttoOikeudenTila getTila() {
            return tila;
        }

        @Override
        public String toString() {
            return "RooliDto{" + "nimi=" + nimi + ", tila=" + tila + '}';
        }

    }

}
