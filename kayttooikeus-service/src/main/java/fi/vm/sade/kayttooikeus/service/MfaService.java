package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.GoogleAuthSetupDto;

public interface MfaService {
    /**
     * Saves a new Google Auth token for the current user and returns everything needed to setup it in the frontend
     */
    GoogleAuthSetupDto setupGoogleAuth();
}
