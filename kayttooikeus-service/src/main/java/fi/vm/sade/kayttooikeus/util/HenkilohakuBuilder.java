package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioMinimalDto;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private CommonProperties commonProperties;

    private HenkilohakuBuilder() {
    }

    public HenkilohakuBuilder(HenkiloHibernateRepository henkiloHibernateRepository,
                              OrikaBeanMapper mapper,
                              PermissionCheckerService permissionCheckerService,
                              OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository,
                              OrganisaatioHenkiloRepository organisaatioHenkiloRepository,
                              HenkiloDataRepository henkiloDataRepository,
                              OrganisaatioClient organisaatioClient,
                              CommonProperties commonProperties) {
        this.henkiloHibernateRepository = henkiloHibernateRepository;
        this.mapper = mapper;
        this.permissionCheckerService = permissionCheckerService;
        this.organisaatioHenkiloDataRepository = organisaatioHenkiloDataRepository;
        this.henkiloDataRepository = henkiloDataRepository;
        this.organisaatioClient = organisaatioClient;
        this.organisaatioHenkiloRepository = organisaatioHenkiloRepository;
        this.commonProperties = commonProperties;

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
    public HenkilohakuBuilder search(Long offset, OrderByHenkilohaku orderBy) {
        this.henkilohakuResultDtoList = this.henkiloHibernateRepository
                .findByCriteria(this.mapper.map(this.henkilohakuCriteriaDto, HenkiloCriteria.class),
                        offset,
                        this.organisationRestrictionList,
                        orderBy != null ? orderBy.getValue() : null);
        return this;
    }

    // Remove henkilos the user has no access (and who have organisation)
    public HenkilohakuBuilder exclusion() {
        if (!this.permissionCheckerService.isCurrentUserAdmin()) {
            List<String> currentUserOrganisaatioOids = this.organisaatioHenkiloRepository
                    .findDistinctOrganisaatiosForHenkiloOid(this.permissionCheckerService.getCurrentUserOid());

            if (!currentUserOrganisaatioOids.contains(this.commonProperties.getRootOrganizationOid())) {
                if (henkilohakuCriteriaDto.getOrganisaatioOids() == null) {
                    henkilohakuCriteriaDto.setOrganisaatioOids(currentUserOrganisaatioOids);
                }
                else {
                    List<String> allCurrentUserOrganisaatioOids = currentUserOrganisaatioOids.stream()
                            .flatMap(currentUserOrganisaatioOid ->
                                    this.organisaatioClient.getActiveChildOids(currentUserOrganisaatioOid).stream())
                            .collect(Collectors.toList());
                    allCurrentUserOrganisaatioOids.addAll(currentUserOrganisaatioOids);
                    allCurrentUserOrganisaatioOids.retainAll(henkilohakuCriteriaDto.getOrganisaatioOids());
                    henkilohakuCriteriaDto.setOrganisaatioOids(allCurrentUserOrganisaatioOids);
                }
            }
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
                        OrganisaatioPerustieto organisaatio = this.organisaatioClient.getOrganisaatioPerustiedotCached(organisaatioOid)
                                .orElseGet(() -> UserDetailsUtil.createUnknownOrganisation(organisaatioOid));
                        return new OrganisaatioMinimalDto(organisaatioOid, organisaatio.getTyypit(), organisaatio.getNimi());
                    })
                    .collect(Collectors.toList()));
            return henkilohakuResultDto;
        }).collect(Collectors.toList());

        return this;
    }

}
