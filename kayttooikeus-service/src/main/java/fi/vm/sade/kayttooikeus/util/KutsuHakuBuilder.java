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
import fi.vm.sade.kayttooikeus.repositories.criteria.AuthorizationCriteria;
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
    private final AuthorizationCriteria authorizationCriteria;

    private List<KutsuReadDto> result;

    public KutsuHakuBuilder prepareByAuthority() {
        this.prepareCommon();
        if (this.permissionCheckerService.isCurrentUserAdmin()) {
            return this.prepareForAdmin();
        }
        else if (this.permissionCheckerService.isCurrentUserMiniAdmin()) {
            return prepareForMiniAdmin();
        }
        return prepareForNormalUser();
    }

    private KutsuHakuBuilder prepareForAdmin() {

        return this;
    }

    private KutsuHakuBuilder prepareForMiniAdmin() {
        // Force OPH-view
        this.kutsuCriteria.setKutsujaOrganisaatioOid(this.commonProperties.getRootOrganizationOid());
        this.kutsuCriteria.setSubOrganisations(false);
        // TODO add condition for fetching only kutsus user is authorized to (see granting käyttöoikeus)

        return this;
    }

    private KutsuHakuBuilder prepareForNormalUser() {
        // Limit organsiaatio search for non admin users
        Set<String> organisaatioOidLimit;
        if (!CollectionUtils.isEmpty(this.kutsuCriteria.getOrganisaatioOids())) {
            organisaatioOidLimit = this.permissionCheckerService.hasOrganisaatioInHierarcy(this.kutsuCriteria.getOrganisaatioOids());
        }
        else {
            organisaatioOidLimit = new HashSet<>(this.organisaatioHenkiloRepository
                    .findDistinctOrganisaatiosForHenkiloOid(this.permissionCheckerService.getCurrentUserOid()));
        }
        this.kutsuCriteria.setOrganisaatioOids(organisaatioOidLimit);
        this.kutsuCriteria.setSubOrganisations(true);
        // TODO add condition for fetching only kutsus user is authorized to (see granting käyttöoikeus)

        return this;
    }

    private void prepareCommon() {
        if (BooleanUtils.isTrue(this.kutsuCriteria.getOphView())) {
            this.kutsuCriteria.setKutsujaOrganisaatioOid(this.commonProperties.getRootOrganizationOid());
            this.kutsuCriteria.setSubOrganisations(false);
        }
        if (BooleanUtils.isTrue(this.kutsuCriteria.getOnlyOwnKutsusView())) {
            this.kutsuCriteria.setKutsujaOid(this.permissionCheckerService.getCurrentUserOid());
        }
        if (BooleanUtils.isTrue(this.kutsuCriteria.getKayttooikeusryhmaView())) {
            this.kutsuCriteria.setKutsujaKayttooikeusryhmaIds(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository
                    .findValidMyonnettyKayttooikeus(this.permissionCheckerService.getCurrentUserOid()).stream()
                    .map(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                    .map(KayttoOikeusRyhma::getId)
                    .collect(Collectors.toSet()));
        }
    }


    public KutsuHakuBuilder doSearch(KutsuOrganisaatioOrder sortBy, Sort.Direction direction, Long offset, Long amount) {
        this.result = this.mapper.mapAsList(this.kutsuRepository.listKutsuListDtos(this.kutsuCriteria, this.authorizationCriteria,
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
