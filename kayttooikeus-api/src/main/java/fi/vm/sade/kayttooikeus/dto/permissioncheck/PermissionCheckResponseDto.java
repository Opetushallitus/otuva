package fi.vm.sade.kayttooikeus.dto.permissioncheck;

public class PermissionCheckResponseDto {

    private boolean accessAllowed = false;
    private String errorMessage;

    public boolean isAccessAllowed() {
        return accessAllowed;
    }

    public void setAccessAllowed(boolean accessAllowed) {
        this.accessAllowed = accessAllowed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}