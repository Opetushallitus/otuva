package fi.vm.sade.kayttooikeus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class OrganisaatioHenkiloWithOrganisaatioDto extends OrganisaatioHenkiloDto {
    private OrganisaatioWithChildrenDto organisaatio;

    @Builder(builderMethodName = "organisaatioBuilder")
    public OrganisaatioHenkiloWithOrganisaatioDto(long id, String organisaatioOid,boolean passivoitu, OrganisaatioWithChildrenDto organisaatio) {
        super(id, organisaatioOid, passivoitu);
        this.organisaatio = organisaatio;
    }

    @Override
    @JsonIgnore
    public String getOrganisaatioOid() {
        return super.getOrganisaatioOid();
    }

    public void setOrganisaatioOid(String oid) {
        super.setOrganisaatioOid(oid);
        if (organisaatio == null) {
            organisaatio = new OrganisaatioWithChildrenDto();
        }
        organisaatio.setOid(oid);
    }

}
