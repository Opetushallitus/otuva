INSERT INTO henkilo (id, oidhenkilo, henkilotyyppi, etunimet_cached, sukunimi_cached, passivoitu_cached, duplicate_cached, vahvasti_tunnistettu, hetu_cached, kutsumanimi_cached, sahkopostivarmennus_aikaleima)
VALUES
    (333,'1.2.246.562.24.37535704268', 'VIRKAILIJA', 'Opa', 'Opetushallituslainen', false, false, true, '160807A963M', 'Opa', null),
    (444,'1.2.246.562.24.23462357366', 'VIRKAILIJA', 'Ville', 'Virkailija', false, false, true, '010108A9195', 'Ville', null),
    (555,'1.2.246.562.24.12342342565', 'VIRKAILIJA', 'Pasi', 'Passivoitu', false, false, true, '040591-921T', 'Pasi', null),
    (666,'1.2.246.562.24.53673452656', 'VIRKAILIJA', 'Olli', 'Oppija', false, false, true, '160807A963M', 'Olli', null),
    (777,'1.2.246.562.24.73645346564', 'PALVELU', '_', 'kryptinen nimi joka ei vastaa palvelua', false, false, true, null, '_', null),
    (888,'1.2.246.562.24.73645346565', 'PALVELU','_','Organisaatio jolla on alaorganisaatioita',false,false,true,null,'_',null),
    (889,'1.2.246.562.24.73645346566','PALVELU','_','Suuremman organisaation 1. aliorganisaatio',false,false,true,null,'_',null),
    (890,'1.2.246.562.24.73645346567','PALVELU','_','Passivoitu suuremman organisaation 2. aliorganisaatio',false,false,true,null,'_',null),
    (901,'1.2.246.562.24.73645346568','PALVELU','_','Toinen suurempi organisaatio',false,false,true,null,'_',null),
    (902,'1.2.246.562.24.73645346569','PALVELU','_','2. suuremman organisaation aliorganisaatio',false,false,true,null,'_',null);

INSERT INTO kayttajatiedot (id, version, password, salt, henkiloid, createdat, invalidated, username, mfaprovider, passwordchange)
VALUES
    (333, 0, '***', '***', 333, now(), false, 'opa', null, null),
    (444, 0, '***', '***', 444, now(), false, 'ville', null, null),
    (555, 0, '***', '***', 555, now(), false, 'pasi', null, null),
    (666, 0, '***', '***', 777, now(), false, 'patenpalvelu', null, null),
    (777,0,'***','***',888,now(),false,'isoorganisaatiopalvelu',null,null),
    (778,0,'***','***',889,now(),false,'aliorganisaatiopalvelu1',null,null),
    (779,0,'***','***',890,now(),false,'aliorganisaatiopalvelu2_passivoitu',null,null),
    (901,0,'***', '***',901,now(),false,'toinen_suurempi_org',null,null),
    (902,0,'***', '***',902,now(),false,'2org_aliorg',null,null);


INSERT INTO organisaatiohenkilo (id, version, organisaatio_oid, henkilo_id, passivoitu)
VALUES
    (333,0,'1.2.246.562.10.00000000001',333,false),
    (444,0,'1.2.246.562.10.71948887212',333,false),
    (555,0,'1.2.246.562.10.71948887212',444,false),
    (666,0,'1.2.246.562.10.71948887212',555,true),
    (777,0,'1.2.246.562.10.00000000001',777,false),
    (888,0,'1.2.246.562.10.722837895010',888,false),
    (889,0,'1.2.246.562.10.79146016781',889,false),
    (890,0,'1.2.246.562.10.80321339568',890,true),
    (901,0,'1.2.246.562.10.00000000030',901,false),
    (902,0,'1.2.246.562.10.00000000031',902,false);

INSERT INTO text_group (id, version)
VALUES
    (1,0);

INSERT INTO text (id, version, lang, text, textgroup_id)
VALUES
    (1,0,'FI','',1);

INSERT INTO palvelu (id, version, name, palvelutyyppi, textgroup_id, kokoelma_id)
VALUES
    (333,0,'KAYTTOOIKEUS','YKSITTAINEN',1,null);

INSERT INTO kayttooikeus (id, version, palvelu_id, rooli, textgroup_id)
VALUES
    (333,0,333,'CRUD',333);

INSERT INTO kayttooikeusryhma (id, version, name, textgroup_id, hidden, rooli_rajoite, kuvaus_id, ryhma_restriction, allowed_usertype, muokattu, muokkaaja)
VALUES
    (333,0,'CRUD',1,false,null,1,false,null,'2021-01-04 13:04:15.659','1.2.246.562.24.30433385401'),
    (444,0,'PALVELU',1,false,null,1,false,null,'2021-01-04 13:04:15.659','1.2.246.562.24.30433385401');

INSERT INTO kayttooikeusryhma_kayttooikeus (kayttooikeusryhma_id, kayttooikeus_id)
VALUES
    (333,333),
    (444,333);

INSERT INTO myonnetty_kayttooikeusryhma_tapahtuma (id, version, aikaleima, syy, tila, kasittelija_henkilo_id, kayttooikeusryhma_id, organisaatiohenkilo_id, voimassaalkupvm, voimassaloppupvm)
VALUES
    (1,0,'2017-05-03 15:52:57.253',null,'MYONNETTY',333,333,333,'2017-05-03','2099-05-03'),
    (2,0,'2017-05-03 15:52:57.251',null,'MYONNETTY',333,333,555,'2017-05-02','2099-05-04'),
    (3,0,'2017-05-03 15:52:57.251',null,'MYONNETTY',333,444,777,'2017-05-02','2099-05-04');