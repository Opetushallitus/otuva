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

    /**
     * Tunnistaa käyttäjän vahvasti. Ohjaa käyttäjän sisään palveluun jos lisätietoja ei tarvita. Jos lisätietoja tarvitaan
     * ohjaa web-sivulle lisätietojen täyttämistä varten.
     * @param loginToken Kirjautumistunniste
     * @param kielisyys Käyttäjän asiointikieli
     * @param hetu Käyttäjän henkilötunnus
     * @return Osoite johon käyttäjä tulee ohjata
     */
    String kirjaaVahvaTunnistus(String loginToken, String kielisyys, String hetu);

    /**
     * Ohjaa käyttäjän itserekisteröintisivulle. Vaihtaa käyttäjän kutsutunnisteen väliaikaiseen tunnisteeseen
     * itserekisteröitymistä varten. Virhetilanteessa käyttäjä ohjataan virhesivulle.
     * @param kutsuToken Kutsutunniste
     * @param kielisyys Käyttäjän asiointikieli
     * @param hetu Käyttäjän henkilötunnus
     * @param etunimet Käyttäjän etunimet
     * @param sukunimi Käyttäjän sukunimi
     * @return
     */
    String kasitteleKutsunTunnistus(String kutsuToken, String kielisyys, String hetu, String etunimet, String sukunimi);
}
