package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloUpdateDto;

public interface EmailVerificationService {

    /*
     * Päivittää henkilon tiedot, kun käyttäjä on tarkistanut sähköpostiosoitteensa
     */
    String emailVerification(HenkiloUpdateDto henkiloUpdate, String kielisyys, String loginToken);

    /*
     * Palauttaa uudelleenohjausurlin logintokenin perusteella
     */
    String redirectUrlByLoginToken(String loginToken, String kielisyys);

    /*
     * Hakee henkilön tiedot loginTokenin perusteella. Tarkoitettu sähköpostinvarmennus-näkymän populointiin
     */
    HenkiloDto getHenkiloByLoginToken(String loginToken);


}
