package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.dto.PalveluRooliDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

import static com.querydsl.core.types.dsl.Expressions.FALSE;
import static fi.vm.sade.kayttooikeus.model.QHenkilo.henkilo;
import static fi.vm.sade.kayttooikeus.model.QKayttoOikeus.kayttoOikeus;
import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.model.QPalvelu.palvelu;

@Repository
public class KayttoOikeusRepositoryImpl extends BaseRepositoryImpl<KayttoOikeus> implements KayttoOikeusRepository {
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
                )).orderBy(kayttoOikeus.rooli.asc()).fetch();
    }

    @Override
    public List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForHenkilo(String henkiloOid) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma mkort = myonnettyKayttoOikeusRyhmaTapahtuma;
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

    @Override
    public List<ExpiringKayttoOikeusDto> findSoonToBeExpiredTapahtumas(LocalDate now, Period... expireThresholds) {
        BooleanExpression expireConditions = Stream.of(expireThresholds).map(now::plus)
                .map(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm::eq).reduce(FALSE.eq(true), BooleanExpression::or);
        return jpa().from(myonnettyKayttoOikeusRyhmaTapahtuma)
                    .innerJoin(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                    .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .select(Projections.bean(ExpiringKayttoOikeusDto.class,
                        henkilo.oidHenkilo.as("henkiloOid"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.id.as("myonnettyTapahtumaId"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.as("voimassaLoppuPvm"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.name.as("ryhmaName"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.description.id.as("ryhmaDescriptionId")
                ))
                .where(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.goe(now)
                        .and(expireConditions))
                .orderBy(henkilo.oidHenkilo.asc()).fetch();
    }

    @Override
    public List<PalveluRooliDto> findPalveluRoolitByKayttoOikeusRyhmaId(Long ryhmaId) {
        return jpa().from(kayttoOikeus)
                .innerJoin(kayttoOikeus.kayttooikeusRyhmas, kayttoOikeusRyhma)
                .innerJoin(kayttoOikeus.palvelu, palvelu)
                .where(kayttoOikeusRyhma.id.eq(ryhmaId))
                .select(Projections.bean(PalveluRooliDto.class,
                            palvelu.name.as("palveluName"),
                            palvelu.description.id.as("palveluTextsId"),
                            kayttoOikeus.rooli.as("rooli"),
                            kayttoOikeus.textGroup.id.as("rooliTextsId")
                        ))
                .fetch();
    }

    @Override
    public KayttoOikeus findByRooliAndPalvelu(String rooli, String palvelu) {
        return jpa().from(kayttoOikeus)
                .where(kayttoOikeus.palvelu.name.eq(palvelu),
                        kayttoOikeus.rooli.eq(rooli))
                .select(kayttoOikeus).fetchFirst();
    }

}