package fi.vm.sade.kayttooikeus.service.validators;

import fi.vm.sade.kayttooikeus.dto.MyonnettyKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.KutsuOrganisaatio;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

@Component
public class KutsuValidator {

    private final KayttoOikeusService kayttoOikeusService;

    public KutsuValidator(KayttoOikeusService kayttoOikeusService) {
        this.kayttoOikeusService = kayttoOikeusService;
    }

    public void validate(Kutsu kutsu) {
        kutsu.getOrganisaatiot().forEach((kutsuOrganisaatio) -> validate(kutsuOrganisaatio, kutsu.getKutsuja()));
    }

    private void validate(KutsuOrganisaatio kutsuOrganisaatio, String kutsujaOid) {
        Set<KayttoOikeusRyhma> kutsuttuKayttoOikeusRyhmat = kutsuOrganisaatio.getRyhmat();
        String organisaatioOid = kutsuOrganisaatio.getOrganisaatioOid();
        List<MyonnettyKayttoOikeusDto> kutsujaKayttoOikeudet = kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(kutsujaOid, organisaatioOid, kutsujaOid);

        kutsuttuKayttoOikeusRyhmat.forEach(kutsuttuKayttoOikeusRyhma -> {
            Predicate<MyonnettyKayttoOikeusDto> predicate = (MyonnettyKayttoOikeusDto t)
                    -> Objects.equals(t.getRyhmaId(), kutsuttuKayttoOikeusRyhma.getId());
            if (kutsujaKayttoOikeudet.stream().noneMatch(predicate)) {
                throw new ForbiddenException("KayttoOikeusryhma with id " + kutsuttuKayttoOikeusRyhma.getId() + " not allowed for organisaatio " + organisaatioOid);
            }
        });
    }

}
