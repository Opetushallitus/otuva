package fi.vm.sade.kayttooikeus.service;

public interface VahvaTunnistusService {
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
