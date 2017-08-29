package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.model.QKutsu;
import fi.vm.sade.kayttooikeus.model.QKutsuOrganisaatio;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Getter @Setter
public class KutsuCriteria extends BaseCriteria {
    private List<KutsunTila> tilas;
    private String kutsujaOid;
    private String sahkoposti;
    private String organisaatioOid;
    private String searchTerm;
    private Boolean onlyOwnKutsus;

    public BooleanBuilder onCondition(QKutsu kutsu, QKutsuOrganisaatio kutsuOrganisaatio, String currentUserOid) {
        BooleanBuilder builder = new BooleanBuilder();
        if (used(tilas)) {
            builder.and(kutsu.tila.in(tilas));
        }
        if (used(kutsujaOid)) {
            builder.and(kutsu.kutsuja.eq(kutsujaOid));
        }
        if (used(sahkoposti)) {
            builder.and(kutsu.sahkoposti.eq(sahkoposti));
        }
        if(StringUtils.hasLength(this.organisaatioOid)) {
            builder.and(kutsuOrganisaatio.organisaatioOid.eq(this.organisaatioOid));
        }
        if(StringUtils.hasLength(this.searchTerm)) {
            Arrays.stream(this.searchTerm.split(" "))
                    .forEach(searchTerm -> builder.and(kutsu.etunimi.containsIgnoreCase(searchTerm)
                            .or(kutsu.sukunimi.containsIgnoreCase(searchTerm))));
        }
        if(BooleanUtils.isTrue(this.onlyOwnKutsus)) {
            builder.and(kutsu.kutsuja.eq(currentUserOid));
        }

        return builder;
    }
    
    public KutsuCriteria withTila(KutsunTila...tila) {
        this.tilas = params(tila);
        return this;
    }

    public KutsuCriteria withKutsuja(String kutsujaOid) {
        this.kutsujaOid = kutsujaOid;
        return this;
    }

    public KutsuCriteria withSahkoposti(String sahkoposti) {
        this.sahkoposti = sahkoposti;
        return this;
    }

    public KutsuCriteria withQuery(String query) {
        this.searchTerm = query;
        return this;
    }
}
