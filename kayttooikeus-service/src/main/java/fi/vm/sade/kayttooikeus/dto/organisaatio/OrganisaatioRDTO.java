package fi.vm.sade.kayttooikeus.dto.organisaatio;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

/**
 * REST API used DTO, ie. "RDTO" for transmitting Organisaatio related data over
 * REST.
 *
 * .. well, actually "OrganisaatioDTO" and "Organisaatio" were already used and
 * I wanted to avoid confusion. :)
 *
 * "Natural key":
 * <ul>
 * <li>Koulutustoimija - y-tunnus</li>
 * <li>Oppilaitos - oppilaitosnumero</li>
 * <li>Toimipiste - oppilaitosnro + toimipisteenjärjestysnumero (konkatenoituna)
 * sekä yhkoulukoodi</li>
 * </ul>
 *
 * @author mlyly
 */
public class OrganisaatioRDTO implements Serializable {

    private static final long serialVersionUID = -5019270750950297893L;

    private String _oid;

    private int _version;

    private Date _alkuPvm;

    private Date _lakkautusPvm;

    private String _ytjKieli;

    private Date _ytjPaivitysPvm;

    private Set<String> _kieletUris;

    private Set<String> _tyypit;

    private Set<String> _vuosiluokat;

    private Set<String> _ryhmatyypit;

    private Set<String> _kayttoryhmat;

    private Map<String, String> _nimi;

    private List<OrganisaatioNimiRDTO> _nimet;

    private String _status;

    private String _maaUri;

    private String _domainNimi;

    private String _kotipaikkaUri;

    private String _oppilaitosKoodi;

    private String _oppilaitosTyyppiUri;

    private String _yTunnus;

    private String _toimipistekoodi;

    private String _yritysmuoto;

    private String _puhelinnumero; // from List of Yhteystietos

    private String _wwwOsoite; // from List of Yhteystietos

    private String _emailOsoite; // from List of Yhteystietos

    private Map<String, String> _postiosoite;

    private Map<String, String> _kayntiosoite;

    private Set<Map<String, String>> _yhteystiedot;

    private String _kuvaus;

    private Map<String, String> _kuvaus2;

    private String _parentOid;

    private String _parentOidPath;

    private OrganisaatioMetaDataRDTO _metadata;

    private String yhteishaunKoulukoodi;

    private Set<Map<String, String>> _yhteystietoArvos = null;
    private String _virastoTunnus;
    private String _opetuspisteenJarjNro;

    private Timestamp _tarkastusPvm; // täytyy olla Timestamp jotta päivityksen vastauksessa formaatti on oikea

    public String getOid() {
        return _oid;
    }

    public void setOid(String _oid) {
        this._oid = _oid;
    }

    public int getVersion() {
        return _version;
    }

    public void setVersion(int _version) {
        this._version = _version;
    }

    public Date getAlkuPvm() {
        return _alkuPvm;
    }

    public void setAlkuPvm(Date _alkuPvm) {
        this._alkuPvm = _alkuPvm;
    }

    public Date getLakkautusPvm() {
        return _lakkautusPvm;
    }

    public void setLakkautusPvm(Date _lakkautusPvm) {
        this._lakkautusPvm = _lakkautusPvm;
    }

    public String getYTJKieli() {
        return _ytjKieli;
    }

    public void setYTJKieli(String _ytjKieli) {
        this._ytjKieli = _ytjKieli;
    }

    public Date getYTJPaivitysPvm() {
        return _ytjPaivitysPvm;
    }

    public void setYTJPaivitysPvm(Date _ytjPaivitysPvm) {
        this._ytjPaivitysPvm = _ytjPaivitysPvm;
    }

    public Set<String> getKieletUris() {
        if (_kieletUris == null) {
            _kieletUris = new HashSet<>();
        }
        return _kieletUris;
    }

    public void setKieletUris(Set<String> _kieletUris) {
        this._kieletUris = _kieletUris;
    }

    public String getMaaUri() {
        return _maaUri;
    }

    public void setMaaUri(String _maaUri) {
        this._maaUri = _maaUri;
    }

    public String getDomainNimi() {
        return _domainNimi;
    }

    public void setDomainNimi(String _domainNimi) {
        this._domainNimi = _domainNimi;
    }

    public String getKotipaikkaUri() {
        return _kotipaikkaUri;
    }

    public void setKotipaikkaUri(String _kotipaikkaUri) {
        this._kotipaikkaUri = _kotipaikkaUri;
    }

    public Map<String, String> getNimi() {
        if (_nimi == null) {
            _nimi = new HashMap<>();
        }
        return _nimi;
    }

    public void setNimi(Map<String, String> _nimi) {
        this._nimi = _nimi;
    }

    public List<OrganisaatioNimiRDTO> getNimet() {
         if (_nimet == null) {
            _nimet = new ArrayList<>();
        }
        return _nimet;
    }

    public void setNimet(List<OrganisaatioNimiRDTO> _nimet) {
        this._nimet = _nimet;
    }

    public String getOppilaitosKoodi() {
        return _oppilaitosKoodi;
    }

    public void setOppilaitosKoodi(String _oppilaitosKoodi) {
        this._oppilaitosKoodi = _oppilaitosKoodi;
    }

    public String getOppilaitosTyyppiUri() {
        return _oppilaitosTyyppiUri;
    }

    public void setOppilaitosTyyppiUri(String _oppilaitosTyyppiUri) {
        this._oppilaitosTyyppiUri = _oppilaitosTyyppiUri;
    }

    public String getYTunnus() {
        return _yTunnus;
    }

    public void setYTunnus(String _yTunnus) {
        this._yTunnus = _yTunnus;
    }

    public Set<String> getTyypit() {
        if (_tyypit == null) {
            _tyypit = new HashSet<>();
        }
        return _tyypit;
    }

    public void setTyypit(Set<String> _tyypit) {
        this._tyypit = _tyypit;
    }

    public String getToimipistekoodi() {
        return _toimipistekoodi;
    }

    public void setToimipistekoodi(String _toimipistekoodi) {
        this._toimipistekoodi = _toimipistekoodi;
    }

    public String getYritysmuoto() {
        return _yritysmuoto;
    }

    public void setYritysmuoto(String _yritysmuoto) {
        this._yritysmuoto = _yritysmuoto;
    }

    public Set<String> getVuosiluokat() {
        if (_vuosiluokat == null) {
            _vuosiluokat = new HashSet<>();
        }
        return _vuosiluokat;
    }

    public void setVuosiluokat(Set<String> _vuosiluokat) {
        this._vuosiluokat = _vuosiluokat;
    }

    public Set<String> getRyhmatyypit() {
        if (_ryhmatyypit == null) {
            _ryhmatyypit = new HashSet<>();
        }
        return _ryhmatyypit;
    }

    public void setRyhmatyypit(Set<String> _ryhmatyypit) {
        this._ryhmatyypit = _ryhmatyypit;
    }

    public Set<String> getKayttoryhmat() {
        if (_kayttoryhmat == null) {
            _kayttoryhmat = new HashSet<>();
        }
        return _kayttoryhmat;
    }

    public void setKayttoryhmat(Set<String> _kayttoryhmat) {
        this._kayttoryhmat = _kayttoryhmat;
    }

    public Map<String, String> getKayntiosoite() {
        if (_kayntiosoite == null) {
            _kayntiosoite = new HashMap<String, String>();
        }
        return _kayntiosoite;
    }

    public void setKayntiosoite(Map<String, String> _kayntiosoite) {
        this._kayntiosoite = _kayntiosoite;
    }

    public Map<String, String> getPostiosoite() {
        if (_postiosoite == null) {
            _postiosoite = new HashMap<String, String>();
        }
        return _postiosoite;
    }

    public void setPostiosoite(Map<String, String> _postiosoite) {
        this._postiosoite = _postiosoite;
    }

    public String getKuvaus() {
        return _kuvaus;
    }

    public void setKuvaus(String _kuvaus) {
        this._kuvaus = _kuvaus;
    }

    public Map<String, String> getKuvaus2() {
        if (_kuvaus2 == null) {
            _kuvaus2 = new HashMap<String, String>();
        }
        return _kuvaus2;
    }

    public void setKuvaus2(Map<String, String> _kuvaus2) {
        this._kuvaus2 = _kuvaus2;
    }

    public String getParentOid() {
        return _parentOid;
    }

    public void setParentOid(String _parentOid) {
        this._parentOid = _parentOid;
    }

    public String getParentOidPath() {
        return _parentOidPath;
    }

    public void setParentOidPath(String _parentOidPath) {
        this._parentOidPath = _parentOidPath;
    }

    public OrganisaatioMetaDataRDTO getMetadata() {
        return _metadata;
    }

    public void setMetadata(OrganisaatioMetaDataRDTO _metadata) {
        this._metadata = _metadata;
    }

    /**
     * @return
     * @deprecated Do not use this method! Use getYhteystiedot() instead!
     */
    @Deprecated
    public String getEmailOsoite() {
        return _emailOsoite;
    }

    /**
     * @param _emailOsoite
     * @deprecated Do not use this method! Use setYhteystiedot() instead!
     */
    @Deprecated
    public void setEmailOsoite(String _emailOsoite) {
        this._emailOsoite = _emailOsoite;
    }

    /**
     * @return
     * @deprecated Do not use this method! Use getYhteystiedot() instead!
     */
    @Deprecated
    public String getPuhelinnumero() {
        return _puhelinnumero;
    }

    /**
     * @param _puhelinnumero
     * @deprecated Do not use this method! Use setYhteystiedot() instead!
     */
    @Deprecated
    public void setPuhelinnumero(String _puhelinnumero) {
        this._puhelinnumero = _puhelinnumero;
    }

    /**
     * @return
     * @deprecated Do not use this method! Use getYhteystiedot() instead!
     */
    @Deprecated
    public String getWwwOsoite() {
        return _wwwOsoite;
    }

    /**
     * @param _wwwOsoite
     * @deprecated Do not use this method! Use setYhteystiedot() instead!
     */
    @Deprecated
    public void setWwwOsoite(String _wwwOsoite) {
        this._wwwOsoite = _wwwOsoite;
    }

    @Deprecated
    public String getYhteishaunKoulukoodi() {
        return yhteishaunKoulukoodi;
    }

    @Deprecated
    public void setYhteishaunKoulukoodi(String yhteishaunKoulukoodi) {
        this.yhteishaunKoulukoodi = yhteishaunKoulukoodi;
    }

    public Set<Map<String, String>> getYhteystietoArvos() {
        return _yhteystietoArvos;
    }

    public void setYhteystietoArvos(Set<Map<String, String>> yhteystietoArvos) {
        this._yhteystietoArvos = yhteystietoArvos;
    }

    public String getVirastoTunnus() {
        return _virastoTunnus;
    }

    public void setVirastoTunnus(String _virastotunnus) {
        this._virastoTunnus = _virastotunnus;
    }

    public String getOpetuspisteenJarjNro() {
        return _opetuspisteenJarjNro;
    }

    public void setOpetuspisteenJarjNro(String _opetuspisteenJarjNro) {
        this._opetuspisteenJarjNro = _opetuspisteenJarjNro;
    }

    public Timestamp getTarkastusPvm() {
        return _tarkastusPvm;
    }

    public void setTarkastusPvm(Timestamp _tarkastusPvm) {
        this._tarkastusPvm = _tarkastusPvm;
    }

    public Set<Map<String, String>> getYhteystiedot() {
        if (_yhteystiedot == null) {
            _yhteystiedot = new HashSet<>();
        }
        return _yhteystiedot;
    }

    public void setYhteystiedot(Set<Map<String, String>> _yhteystiedot) {
        this._yhteystiedot = _yhteystiedot;
    }

    public void addYhteystieto(Map<String, String> yhtMap) {
        getYhteystiedot().add(yhtMap);
    }

    /**
     * @return the _status
     */
    public String getStatus() {
        return _status;
    }

    /**
     * @param _status the _status to set
     */
    public void setStatus(String _status) {
        this._status = _status;
    }
}
