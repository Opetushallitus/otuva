package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.dto.PalveluRooliDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Period;
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
                        myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.tunniste.as("ryhmaName"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.nimi.id.as("ryhmaDescriptionId")
                ))
                .where(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.goe(now)
                        .and(expireConditions))
                .orderBy(henkilo.oidHenkilo.asc()).fetch();
    }

    @Override
    public List<String> findHenkilosByRyhma(long id) {
        LocalDate now = LocalDate.now();
        return jpa().from(myonnettyKayttoOikeusRyhmaTapahtuma)
                .innerJoin(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .select(henkilo.oidHenkilo)
                .where(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id.eq(id)
                        .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.goe(now))
                        .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaAlkuPvm.lt(now))
                ).fetch();
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
