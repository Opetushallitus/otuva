package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.VarmennusPoletti;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VarmennusPolettiRepository extends JpaRepository<VarmennusPoletti, Long>, VarmennusPolettiRepositoryCustom {

    /**
     * Poistaa {@link VarmennusPoletti varmennuspoletit} annetun henkilön ja
     * tyypin mukaan.
     *
     * @param henkilo henkilö
     * @param tyyppi varmennuspoletin tyyppi
     * @return poistettujen rivien määrä
     */
    long deleteByHenkiloAndTyyppi(Henkilo henkilo, VarmennusPoletti.VarmennusPolettiTyyppi tyyppi);

}
