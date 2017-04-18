package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import org.joda.time.LocalDate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository extends CrudRepository<MyonnettyKayttoOikeusRyhmaTapahtuma, Long> {

    List<MyonnettyKayttoOikeusRyhmaTapahtuma>
    findByOrganisaatioHenkiloHenkiloOidHenkiloAndVoimassaAlkuPvmBeforeAndVoimassaLoppuPvmAfterAndOrganisaatioHenkiloPassivoituAndOrganisaatioHenkiloHenkiloPassivoitu(
            String oidHenkilo,LocalDate voimassaAlkuPvm,LocalDate voimassaLoppuPvm, boolean organisaatiohenkiloPassivoitu, boolean henkiloPassivoitu);
    default List<MyonnettyKayttoOikeusRyhmaTapahtuma> findValidMyonnettyKayttooikeus(String oidHenkilo) {
        return findByOrganisaatioHenkiloHenkiloOidHenkiloAndVoimassaAlkuPvmBeforeAndVoimassaLoppuPvmAfterAndOrganisaatioHenkiloPassivoituAndOrganisaatioHenkiloHenkiloPassivoitu(
                oidHenkilo, LocalDate.now(), LocalDate.now(), false, false);
    };

    Optional<MyonnettyKayttoOikeusRyhmaTapahtuma> findFirstByKayttoOikeusRyhmaIdAndOrganisaatioHenkiloOrganisaatioOidAndOrganisaatioHenkiloHenkiloOidHenkilo(
            Long kayttooikeusryhmaId, String organisaatioOid, String oidHenkilo
    );
    default Optional<MyonnettyKayttoOikeusRyhmaTapahtuma> findMyonnettyTapahtuma(Long kayttooikeusryhmaId, String organisaatioOid, String oidHenkilo) {
        return findFirstByKayttoOikeusRyhmaIdAndOrganisaatioHenkiloOrganisaatioOidAndOrganisaatioHenkiloHenkiloOidHenkilo(
                kayttooikeusryhmaId, organisaatioOid, oidHenkilo);
    }

}
