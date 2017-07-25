package fi.vm.sade.kayttooikeus.enumeration;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyPath;

import java.util.List;

public enum KutsuOrganisaatioOrder {
    NIMI(Lists.newArrayList("sukunimi", "etunimi")),
    SAHKOPOSTI(Lists.newArrayList("sahkoposti")),
    AIKALEIMA(Lists.newArrayList("aikaleima")),
    ;

    private List<String> orders;

    KutsuOrganisaatioOrder(List<String> orders) {
        this.orders = orders;
    }

    public Sort getSortWithDirection() {
        return this.getSortWithDirection(Sort.DEFAULT_DIRECTION);
    }

    public Sort getSortWithDirection(Sort.Direction direction) {
        PropertyPath.from("organisaatiot", Kutsu.class);
        return new Sort(direction, this.orders);
    }
}
