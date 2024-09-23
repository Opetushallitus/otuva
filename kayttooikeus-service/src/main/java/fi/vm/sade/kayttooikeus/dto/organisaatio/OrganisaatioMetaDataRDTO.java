/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */
package fi.vm.sade.kayttooikeus.dto.organisaatio;

import java.io.Serializable;
import java.util.*;

public class OrganisaatioMetaDataRDTO implements Serializable {

    private static final long serialVersionUID = 604193749288966081L;

    private Map<String, Map<String, String>> _data = new HashMap<String, Map<String, String>>();

    private Map<String, String> _hakutoimistonNimi;

    private Map<String, String> _nimi;

    private Map<String, String> _hakutoimistoEctsEmail;

    private Map<String, String> _hakutoimistoEctsNimi;

    private Map<String, String> _hakutoimistoEctsPuhelin;

    private Map<String, String> _hakutoimistoEctsTehtavanimike;

    private String _koodi;

    private String _kuvaEncoded;

    private Date _luontiPvm;

    private Date _muokkausPvm;

    private List<Map<String, String>> _yhteystiedot;

    public void addByKey(String key, Map<String, String> map) {
        _data.put(key, map);
    }

    public Map<String, Map<String, String>> getData() {
        return _data;
    }

    public void setData(Map<String, Map<String, String>> _data) {
        this._data = _data;
    }

    public Map<String, String> getHakutoimistoEctsEmail() {
        return _hakutoimistoEctsEmail;
    }

    public void setHakutoimistoEctsEmail(Map<String, String> _hakutoimistoEctsEmail) {
        this._hakutoimistoEctsEmail = _hakutoimistoEctsEmail;
    }

    public Map<String, String> getHakutoimistoEctsNimi() {
        return _hakutoimistoEctsNimi;
    }

    public void setHakutoimistoEctsNimi(Map<String, String> _hakutoimistoEctsNimi) {
        this._hakutoimistoEctsNimi = _hakutoimistoEctsNimi;
    }

    public Map<String, String> getHakutoimistoEctsPuhelin() {
        return _hakutoimistoEctsPuhelin;
    }

    public void setHakutoimistoEctsPuhelin(Map<String, String> _hakutoimistoEctsPuhelin) {
        this._hakutoimistoEctsPuhelin = _hakutoimistoEctsPuhelin;
    }

    public Map<String, String> getHakutoimistoEctsTehtavanimike() {
        return _hakutoimistoEctsTehtavanimike;
    }

    public void setHakutoimistoEctsTehtavanimike(Map<String, String>_hakutoimistoEctsTehtavanimike) {
        this._hakutoimistoEctsTehtavanimike = _hakutoimistoEctsTehtavanimike;
    }

    public Map<String, String> getHakutoimistonNimi() {
        return _hakutoimistonNimi;
    }

    public void setHakutoimistonNimi(Map<String, String> _hakutoimistonNimi) {
        this._hakutoimistonNimi = _hakutoimistonNimi;
    }

    public String getKoodi() {
        return _koodi;
    }

    public void setKoodi(String _koodi) {
        this._koodi = _koodi;
    }

    public String getKuvaEncoded() {
        return _kuvaEncoded;
    }

    public void setKuvaEncoded(String _kuvaEncoded) {
        this._kuvaEncoded = _kuvaEncoded;
    }

    public Date getLuontiPvm() {
        return _luontiPvm;
    }

    public void setLuontiPvm(Date _luontiPvm) {
        this._luontiPvm = _luontiPvm;
    }

    public Date getMuokkausPvm() {
        return _muokkausPvm;
    }

    public void setMuokkausPvm(Date _muokkausPvm) {
        this._muokkausPvm = _muokkausPvm;
    }

    public Map<String, String> getNimi() {
        return _nimi;
    }

    public void setNimi(Map<String, String> _nimi) {
        this._nimi = _nimi;
    }

    public List<Map<String, String>> getYhteystiedot() {
        if (_yhteystiedot == null) {
            _yhteystiedot = new ArrayList<Map<String, String>>();
        }
        return _yhteystiedot;
    }

    public void setYhteystiedot(List<Map<String, String>> _yhteystiedot) {
        this._yhteystiedot = _yhteystiedot;
    }
}
