package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class KutsuHakuBuilder {
    private final PermissionCheckerService permissionCheckerService;
    private final LocalizationService localizationService;

    private final CommonProperties commonProperties;

    private final MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;
    private final KutsuRepository kutsuRepository;
    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;

    private final OrikaBeanMapper mapper;

    private final KutsuCriteria kutsuCriteria;
    private List<KutsuReadDto> result;

    public KutsuHakuBuilder prepareCommon() {
        if (BooleanUtils.isTrue(kutsuCriteria.getAdminView()) && !this.permissionCheckerService.isCurrentUserAdmin()) {
            kutsuCriteria.setAdminView(null);
        }
        if (BooleanUtils.isTrue(kutsuCriteria.getOphView()) && this.permissionCheckerService.isCurrentUserMiniAdmin()) {
            kutsuCriteria.setKutsujaOrganisaatioOid(this.commonProperties.getRootOrganizationOid());
            kutsuCriteria.setSubOrganisations(false);
        }
        if (BooleanUtils.isTrue(kutsuCriteria.getOnlyOwnKutsus())) {
            kutsuCriteria.setKutsujaOid(this.permissionCheckerService.getCurrentUserOid());
        }
        if (BooleanUtils.isTrue(kutsuCriteria.getKayttooikeusryhmaView())) {
            kutsuCriteria.setKutsujaKayttooikeusryhmaIds(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository
                    .findValidMyonnettyKayttooikeus(this.permissionCheckerService.getCurrentUserOid()).stream()
                    .map(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                    .map(KayttoOikeusRyhma::getId)
                    .collect(Collectors.toSet()));
        }
        // Limit organsiaatio search for non admin users
        if (!this.permissionCheckerService.isCurrentUserAdmin() && !this.permissionCheckerService.isCurrentUserMiniAdmin()) {
            Set<String> organisaatioOidLimit;
            if (!CollectionUtils.isEmpty(kutsuCriteria.getOrganisaatioOids())) {
                organisaatioOidLimit = this.permissionCheckerService.hasOrganisaatioInHierarcy(kutsuCriteria.getOrganisaatioOids());
            }
            else {
                organisaatioOidLimit = new HashSet<>(this.organisaatioHenkiloRepository
                        .findDistinctOrganisaatiosForHenkiloOid(this.permissionCheckerService.getCurrentUserOid()));
            }
            kutsuCriteria.setOrganisaatioOids(organisaatioOidLimit);
            kutsuCriteria.setSubOrganisations(true);
        }
        // Limit käyttöoikeusryhmä search for non admin users
        if (!CollectionUtils.isEmpty(kutsuCriteria.getKayttooikeusryhmaIds())
                && !this.permissionCheckerService.isCurrentUserAdmin()) {
            Set<Long> currentUserActiveKayttooikeusryhmaIds = this.myonnettyKayttoOikeusRyhmaTapahtumaRepository
                    .findValidMyonnettyKayttooikeus(this.permissionCheckerService.getCurrentUserOid()).stream()
                    .map(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                    .map(KayttoOikeusRyhma::getId)
                    .collect(Collectors.toSet());
            kutsuCriteria.setKayttooikeusryhmaIds(currentUserActiveKayttooikeusryhmaIds);
        }
        return this;
    }

    public KutsuHakuBuilder doSearch(KutsuOrganisaatioOrder sortBy, Sort.Direction direction, Long offset, Long amount) {
        this.result = this.mapper.mapAsList(this.kutsuRepository.listKutsuListDtos(this.kutsuCriteria,
                sortBy.getSortWithDirection(direction), offset, amount), KutsuReadDto.class);
        return this;
    }

    public KutsuHakuBuilder localise() {
        this.result.forEach(kutsuReadDto -> this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot()));
        return this;
    }

    public List<KutsuReadDto> build() {
        return this.result;
    }
}
