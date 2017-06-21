package fi.vm.sade.kayttooikeus.repositories;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import fi.vm.sade.kayttooikeus.model.LdapPriorityType;
import fi.vm.sade.kayttooikeus.model.LdapStatusType;
import fi.vm.sade.kayttooikeus.model.QLdapUpdateData;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class LdapUpdateDataCriteria {

    private final Collection<LdapPriorityType> priorities;
    private final Collection<LdapStatusType> statuses;

    public Predicate condition(QLdapUpdateData qLdapUpdateData) {
        BooleanBuilder builder = new BooleanBuilder();
        if (priorities != null) {
            builder.and(qLdapUpdateData.priority.in(priorities));
        }
        if (statuses != null) {
            builder.and(qLdapUpdateData.status.in(statuses));
        }
        return builder;
    }

}
