package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.QKayttoOikeus;
import fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.QPalvelu;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusDao;
import org.springframework.stereotype.Repository;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeus.kayttoOikeus;
import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;

@Repository
public class KayttoOikeusDaoImpl extends AbstractDao implements KayttoOikeusDao {
    @Override
    public boolean isHenkiloMyonnettyKayttoOikeusToPalveluInRole(String henkiloOid, String palveluName, String role) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttoOikeusRyhma ryhma = tapahtuma.kayttoOikeusRyhma;
        QKayttoOikeus oikeus = kayttoOikeus;
        QPalvelu palvelu = oikeus.palvelu;
        return exists(jpa().from(tapahtuma)
                .innerJoin(ryhma.kayttoOikeus, oikeus)
                .where(tapahtuma.organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid)
                    .and(kayttoOikeus.rooli.eq(role))
                    .and(palvelu.name.eq(palveluName))
                ).select(kayttoOikeus.id));
    }
}
