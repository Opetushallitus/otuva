package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.repositories.dto.PalveluKayttoOikeusDto;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeus.kayttoOikeus;
import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
import static fi.vm.sade.kayttooikeus.model.QPalvelu.palvelu;

@Repository
public class KayttoOikeusRepositoryImpl extends AbstractRepository implements KayttoOikeusRepository {
    public static BooleanExpression voimassa(QMyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma, LocalDate at) {
        return tapahtuma.voimassaAlkuPvm.loe(at).and(tapahtuma.voimassaLoppuPvm.isNull().or(tapahtuma.voimassaLoppuPvm.goe(at)));
    }
    
    @Override
    public boolean isHenkiloMyonnettyKayttoOikeusToPalveluInRole(String henkiloOid, String palveluName, String role) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttoOikeusRyhma ryhma = tapahtuma.kayttoOikeusRyhma;
        QKayttoOikeus oikeus = new QKayttoOikeus("oikeus");
        QPalvelu palvelu = oikeus.palvelu;
        return exists(jpa().from(tapahtuma)
                .innerJoin(ryhma.kayttoOikeus, oikeus)
                .where(tapahtuma.organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid)
                    .and(oikeus.rooli.eq(role))
                    .and(palvelu.name.eq(palveluName))
                    .and(voimassa(tapahtuma, new LocalDate()))
                    .and(OrganisaatioHenkiloRepositoryImpl.voimassa(tapahtuma.organisaatioHenkilo, new LocalDate()))
                ).select(tapahtuma.id));
    }

    @Override
    public List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName) {
        return jpa().from(palvelu)
                .innerJoin(palvelu.kayttoOikeus, kayttoOikeus)
                .where(palvelu.name.eq(palveluName))
                .select(Projections.constructor(PalveluKayttoOikeusDto.class,
                    kayttoOikeus.rooli,
                    kayttoOikeus.textGroup.id
                )).fetch();
    }

    @Override
    public List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForHenkilo(String henkiloOid) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma mkort = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QOrganisaatioHenkilo oh = QOrganisaatioHenkilo.organisaatioHenkilo;
        QKayttoOikeusRyhma kor = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QKayttoOikeus ko = QKayttoOikeus.kayttoOikeus;
        QTextGroup description = QTextGroup.textGroup;
        QHenkilo h = QHenkilo.henkilo;
        return jpa().from(mkort)
                    .leftJoin(mkort.kasittelija, h)
                    .leftJoin(mkort.organisaatioHenkilo, oh)
                    .leftJoin(mkort.kayttoOikeusRyhma, kor)
                    .leftJoin(kor.description, description)
                    .leftJoin(kor.kayttoOikeus, ko)
                    .leftJoin(description.texts)
                .select(Projections.bean(KayttoOikeusHistoriaDto.class,
                        oh.organisaatioOid.as("organisaatioOid"),
                        ko.id.as("kayttoOikeusId"),
                        oh.tehtavanimike.as("tehtavanimike"),
                        description.id.as("kuvausId"),
                        mkort.tila.as("tila"),
                        mkort.voimassaAlkuPvm.as("voimassaAlkuPvm"),
                        mkort.voimassaLoppuPvm.as("voimassaLoppuPvm"),
                        mkort.aikaleima.as("aikaleima"),
                        mkort.kasittelija.oidHenkilo.as("kasittelija")
                ))
                .where(oh.henkilo.oidHenkilo.eq(henkiloOid)).distinct()
                .orderBy(mkort.aikaleima.desc()).fetch();
    }
}
