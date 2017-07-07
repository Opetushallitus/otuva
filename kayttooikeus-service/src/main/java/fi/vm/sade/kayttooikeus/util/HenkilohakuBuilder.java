package fi.vm.sade.kayttooikeus.util;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioMinimalDto;
import fi.vm.sade.kayttooikeus.enumeration.HenkilohakuOrderBy;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HenkilohakuBuilder {
    private HenkilohakuCriteriaDto henkilohakuCriteriaDto;
    private List<HenkilohakuResultDto> henkilohakuResultDtoList = new ArrayList<>();
    private List<String> organisationRestrictionList = new ArrayList<>();

    private HenkiloHibernateRepository henkiloHibernateRepository;
    private OrikaBeanMapper mapper;
    private PermissionCheckerService permissionCheckerService;
    private OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository;
    private HenkiloDataRepository henkiloDataRepository;
    private OrganisaatioClient organisaatioClient;

    private HenkilohakuBuilder() {}

    public HenkilohakuBuilder(HenkiloHibernateRepository henkiloHibernateRepository,
                              OrikaBeanMapper mapper,
                              PermissionCheckerService permissionCheckerService,
                              OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository,
                              HenkiloDataRepository henkiloDataRepository,
                              OrganisaatioClient organisaatioClient) {
        this.henkiloHibernateRepository = henkiloHibernateRepository;
        this.mapper = mapper;
        this.permissionCheckerService = permissionCheckerService;
        this.organisaatioHenkiloDataRepository = organisaatioHenkiloDataRepository;
        this.henkiloDataRepository = henkiloDataRepository;
        this.organisaatioClient = organisaatioClient;
    }

    public HenkilohakuBuilder builder(HenkilohakuCriteriaDto henkilohakuCriteriaDto) {
        this.henkilohakuCriteriaDto = henkilohakuCriteriaDto;
        this.henkilohakuResultDtoList = new ArrayList<>();
        return this;
    }

    public List<HenkilohakuResultDto> build() {
        return this.henkilohakuResultDtoList;
    }

    // Find nimi, kayttajatunnus and oidHenkilo
    public HenkilohakuBuilder search(Long offset, HenkilohakuOrderBy orderBy) {
        this.henkilohakuResultDtoList = this.henkiloHibernateRepository
                .findByCriteria(this.mapper.map(this.henkilohakuCriteriaDto, HenkiloCriteria.class),
                        offset,
                        this.organisationRestrictionList,
                        orderBy != null ? orderBy.getValue() : null);
        return this;
    }

    // Remove henkilos the user has no access (and who have organisation)
    public HenkilohakuBuilder exclusion() {
        if(!this.permissionCheckerService.isCurrentUserAdmin()) {

        }

        return this;
    }

    // Find organisaatioNimiList
    public HenkilohakuBuilder enrichment() {
        List<String> oidList = this.henkilohakuResultDtoList.stream().map(HenkilohakuResultDto::getOidHenkilo).collect(Collectors.toList());
        List<Henkilo> henkiloList = this.henkiloDataRepository.readByOidHenkiloIn(oidList);
        this.henkilohakuResultDtoList = this.henkilohakuResultDtoList.stream().map(henkilohakuResultDto -> {
            Henkilo henkilo = henkiloList.stream().filter(henkilo1 -> Objects.equals(henkilo1.getOidHenkilo(), henkilohakuResultDto.getOidHenkilo()))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            henkilohakuResultDto.setOrganisaatioNimiList(henkilo.getOrganisaatioHenkilos().stream()
                    .filter(((Predicate<OrganisaatioHenkilo>) OrganisaatioHenkilo::isPassivoitu).negate())
                    .map(OrganisaatioHenkilo::getOrganisaatioOid)
                    .map(organisaatioOid -> {
                        OrganisaatioPerustieto organisaatio = this.organisaatioClient.getOrganisaatioPerustiedotCached(organisaatioOid,
                                OrganisaatioClient.Mode.requireCache())
                                .orElseGet(() -> OrganisaatioPerustieto.builder()
                                        .oid(organisaatioOid)
                                        .nimi(new HashMap<String, String>() {{
                                            put("fi", "Tuntematon organisaatio");
                                            put("sv", "Okänd organisation");
                                            put("en", "Unknown organisation");
                                        }})
                                        .tyypit(Lists.newArrayList())
                                        .build());
                        return new OrganisaatioMinimalDto(organisaatioOid, organisaatio.getTyypit(), organisaatio.getNimi());
                    })
                    .collect(Collectors.toList()));
            return henkilohakuResultDto;
        }).collect(Collectors.toList());

        return this;
    }

}
