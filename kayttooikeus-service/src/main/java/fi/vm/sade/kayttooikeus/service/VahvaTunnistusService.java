package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusLisatiedotDto;

public interface VahvaTunnistusService {

    /**
     * Tunnistaa käyttäjän vahvasti järjestelmään.
     *
     * @param loginToken tunnistautumisen kertakäyttöinen avain
     * @param lisatiedotDto käyttäjän syöttämät lisätiedot jotka tallennetaan
     * @return kertakäyttöinen avain kirjautumiseen ("authToken")
     */
    String tunnistaudu(String loginToken, VahvaTunnistusLisatiedotDto lisatiedotDto);

}
