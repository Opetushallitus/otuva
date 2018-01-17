package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusRequestDto;
import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusResponseDto;

public interface VahvaTunnistusService {

    /**
     * Tunnistaa käyttäjän vahvasti järjestelmään.
     *
     * @param loginToken tunnistautumisen kertakäyttöinen avain
     * @param lisatiedotDto käyttäjän syöttämät lisätiedot jotka tallennetaan
     * @return uudelleenohjaukseen tarvittavat parametrit
     */
    VahvaTunnistusResponseDto tunnistaudu(String loginToken, VahvaTunnistusRequestDto lisatiedotDto);

}
