package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.VarmennusPoletti;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

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


    /**
     *
     * Palauttaa {@Link VarmennusPoletti varmennuspoletti} entiteetin, jota vastaava poletti annetaan parametrina
     *
     * @param poletti
     * @return polettia vastaava entiteetti
     */
    Optional<VarmennusPoletti> findByPoletti(String poletti);
}
