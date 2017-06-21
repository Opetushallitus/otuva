package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository extends CrudRepository<MyonnettyKayttoOikeusRyhmaTapahtuma, Long> {

    List<MyonnettyKayttoOikeusRyhmaTapahtuma>
    findByOrganisaatioHenkiloHenkiloOidHenkiloAndVoimassaAlkuPvmLessThanEqualAndVoimassaLoppuPvmGreaterThanEqualAndOrganisaatioHenkiloPassivoitu(
            String oidHenkilo, LocalDate voimassaAlkuPvm, LocalDate voimassaLoppuPvm, boolean organisaatiohenkiloPassivoitu);
    default List<MyonnettyKayttoOikeusRyhmaTapahtuma> findValidMyonnettyKayttooikeus(String oidHenkilo) {
        return findByOrganisaatioHenkiloHenkiloOidHenkiloAndVoimassaAlkuPvmLessThanEqualAndVoimassaLoppuPvmGreaterThanEqualAndOrganisaatioHenkiloPassivoitu(
                oidHenkilo, LocalDate.now(), LocalDate.now(), false);
    }

    Optional<MyonnettyKayttoOikeusRyhmaTapahtuma> findFirstByKayttoOikeusRyhmaIdAndOrganisaatioHenkiloOrganisaatioOidAndOrganisaatioHenkiloHenkiloOidHenkilo(
            Long kayttooikeusryhmaId, String organisaatioOid, String oidHenkilo
    );
    default Optional<MyonnettyKayttoOikeusRyhmaTapahtuma> findMyonnettyTapahtuma(Long kayttooikeusryhmaId, String organisaatioOid, String oidHenkilo) {
        return findFirstByKayttoOikeusRyhmaIdAndOrganisaatioHenkiloOrganisaatioOidAndOrganisaatioHenkiloHenkiloOidHenkilo(
                kayttooikeusryhmaId, organisaatioOid, oidHenkilo);
    }

}
