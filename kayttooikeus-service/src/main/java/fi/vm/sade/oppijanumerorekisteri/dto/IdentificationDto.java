package fi.vm.sade.oppijanumerorekisteri.dto;

public class IdentificationDto {
    private IdpEntityId idpEntityId;

    private String identifier;

    public static IdentificationDto of(IdpEntityId idpEntityId, String identifier) {
        IdentificationDto dto = new IdentificationDto();
        dto.setIdpEntityId(idpEntityId);
        dto.setIdentifier(identifier);
        return dto;
    }

    public IdpEntityId getIdpEntityId() {
        return idpEntityId;
    }

    public void setIdpEntityId(IdpEntityId idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
