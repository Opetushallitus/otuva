package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import org.joda.time.LocalDate;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma.kayttoOikeusRyhma;

@Repository
public class KayttoOikeusRyhmaRepositoryImpl extends BaseRepositoryImpl<KayttoOikeusRyhma> implements KayttoOikeusRyhmaRepository {

    private QBean<KayttoOikeusRyhmaDto> KayttoOikeusRyhmaDtoBean() {
        return Projections.bean(KayttoOikeusRyhmaDto.class,
                kayttoOikeusRyhma.id.as("id"),
                kayttoOikeusRyhma.name.as("name"),
                kayttoOikeusRyhma.rooliRajoite.as("rooliRajoite"),
                kayttoOikeusRyhma.description.id.as("descriptionId"));
    }

    @Override
    public List<KayttoOikeusRyhmaDto> findByIdList(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)){
            return new ArrayList<>();
        }

        BooleanBuilder booleanBuilder = new BooleanBuilder()
                .and(kayttoOikeusRyhma.hidden.eq(false))
                .and(kayttoOikeusRyhma.id.in(idList));

        return jpa().from(kayttoOikeusRyhma)
                .leftJoin(kayttoOikeusRyhma.description)
                .where(booleanBuilder)
                .orderBy(kayttoOikeusRyhma.id.asc())
                .select(KayttoOikeusRyhmaDtoBean())
                .fetch();
    }

    @Override
    public Optional<KayttoOikeusRyhma> findByRyhmaId(Long id) {
        return Optional.ofNullable(from(kayttoOikeusRyhma)
                .where(kayttoOikeusRyhma.hidden.eq(false)
                        .and(kayttoOikeusRyhma.id.eq(id)))
                .distinct()
                .select(kayttoOikeusRyhma).fetchFirst());
    }

    @Override
    public Boolean ryhmaNameFiExists(String ryhmaNameFi) {
        return exists(jpa().from(kayttoOikeusRyhma)
                .where(kayttoOikeusRyhma.description.texts.any().lang.eq("FI"),
                        kayttoOikeusRyhma.description.texts.any().text.eq(ryhmaNameFi))
                .select(kayttoOikeusRyhma));
    }

    @Override
    public List<KayttoOikeusRyhmaDto> listAll() {
        return jpa().from(kayttoOikeusRyhma)
                .leftJoin(kayttoOikeusRyhma.description)
                .where(kayttoOikeusRyhma.hidden.eq(false))
                .orderBy(kayttoOikeusRyhma.id.asc())
                .select(KayttoOikeusRyhmaDtoBean())
                .fetch();
    }

    @Override
    public List<Tuple> findOrganisaatioOidAndRyhmaIdByHenkiloOid(String oid) {
        QHenkilo henkilo = QHenkilo.henkilo;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma mkt = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;

        JPAQuery<Tuple> query = jpa().select(organisaatioHenkilo.organisaatioOid, mkt.kayttoOikeusRyhma.id);
        query.from(henkilo)
                .innerJoin(henkilo.organisaatioHenkilos, organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.myonnettyKayttoOikeusRyhmas, mkt)
                .innerJoin(mkt.kayttoOikeusRyhma, kayttoOikeusRyhma);
        query.where(henkilo.oidHenkilo.eq(oid)
                .and(kayttoOikeusRyhma.hidden.isFalse())
                .and(organisaatioHenkilo.passivoitu.isFalse())
                .and(mkt.tila.eq(KayttoOikeudenTila.MYONNETTY).or(mkt.tila.eq(KayttoOikeudenTila.UUSITTU)))
                .and(mkt.voimassaAlkuPvm.before(LocalDate.now()))
                .and(mkt.voimassaLoppuPvm.after(LocalDate.now())));
        return query.fetch();
    }
}
