package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioCache;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import org.joda.time.LocalDate;

import javax.persistence.EntityManager;

import static fi.vm.sade.kayttooikeus.repositories.populate.Populator.first;
import static java.util.Optional.ofNullable;

public class OrganisaatioHenkiloPopulator implements Populator<OrganisaatioHenkilo> {
    private final Populator<Henkilo> henkilo;
    private final String organisaatioOid;
    private LocalDate voimassaAlku = new LocalDate();
    private String tehtavanimike;

    public OrganisaatioHenkiloPopulator(String henkiloOid, String organisaatioOid) {
        this.henkilo = new HenkiloPopulator(henkiloOid);
        this.organisaatioOid = organisaatioOid;
    }

    public OrganisaatioHenkiloPopulator(Populator<Henkilo> henkilo, String organisaatioOid) {
        this.henkilo = henkilo;
        this.organisaatioOid = organisaatioOid;
    }

    public static OrganisaatioHenkiloPopulator organisaatioHenkilo(String henkiloOid, String organisaatioOid) {
        return new OrganisaatioHenkiloPopulator(henkiloOid, organisaatioOid);
    }
    
    public static OrganisaatioHenkiloPopulator organisaatioHenkilo(Populator<Henkilo> henkilo, String organisaatioOid) {
        return new OrganisaatioHenkiloPopulator(henkilo, organisaatioOid);
    }
    
    public OrganisaatioHenkiloPopulator voimassaAlkaen(LocalDate alkaen) {
        this.voimassaAlku = alkaen;
        return this;
    }
    
    public OrganisaatioHenkiloPopulator tehtavanimike(String tehtavanimike) {
        this.tehtavanimike = tehtavanimike;
        return this;
    }
    
    @Override
    public OrganisaatioHenkilo apply(EntityManager entityManager) {
        Henkilo henkilo = this.henkilo.apply(entityManager);
        OrganisaatioHenkilo found = first(entityManager.createQuery("select o from OrganisaatioHenkilo o " +
                    "where o.henkilo.oidHenkilo=:henkiloOid and o.organisaatioOid = :oOid")
                .setParameter("henkiloOid", henkilo.getOidHenkilo())
                .setParameter("oOid", organisaatioOid));
        if (found != null) {
            return found;
        }
        OrganisaatioHenkilo organisaatioHenkilo = new OrganisaatioHenkilo();
        organisaatioHenkilo.setHenkilo(henkilo);
        organisaatioHenkilo.setOrganisaatioCache(ofNullable(entityManager.find(OrganisaatioCache.class, organisaatioOid))
                .orElseGet(() ->OrganisaatioCache.builder()
                    .organisaatioOid(organisaatioOid)
                    .organisaatioOidPath(organisaatioOid)
                .build()));
        organisaatioHenkilo.setOrganisaatioOid(organisaatioOid);
        organisaatioHenkilo.setTehtavanimike(tehtavanimike);
        organisaatioHenkilo.setPassivoitu(false);
        organisaatioHenkilo.setVoimassaAlkuPvm(voimassaAlku);
        organisaatioHenkilo.setOrganisaatioHenkiloTyyppi(OrganisaatioHenkiloTyyppi.VIRKAILIJA);
        entityManager.persist(organisaatioHenkilo);
        return organisaatioHenkilo;
    }
}
