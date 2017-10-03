package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import fi.vm.sade.kayttooikeus.model.QHenkilo;
import fi.vm.sade.kayttooikeus.model.QKayttajatiedot;
import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KayttooikeusCriteria {
    String username;
    String oidHenkilo;

    public Predicate condition(QKayttajatiedot kayttajatiedot, QHenkilo henkilo) {
        BooleanBuilder builder = new BooleanBuilder();
        if(StringUtils.hasLength(this.username)) {
            builder.and(kayttajatiedot.username.eq(this.username));
        }
        if(StringUtils.hasLength(this.oidHenkilo)) {
            builder.and(henkilo.oidHenkilo.eq(this.oidHenkilo));
        }
        return builder;
    }
}
