INSERT INTO henkilo (id, oidhenkilo, henkilotyyppi, etunimet_cached, sukunimi_cached, passivoitu_cached, duplicate_cached, vahvasti_tunnistettu, hetu_cached, kutsumanimi_cached, sahkopostivarmennus_aikaleima)
VALUES
    (1111, '1.2.246.562.24.43006465835', 'PALVELU', '_', 'testi_rekisterinpitaja_palvelukayttaja', false, false, true, null, '_', null);

INSERT INTO kayttajatiedot (id, version, password, salt, henkiloid, createdat, invalidated, username, mfaprovider, passwordchange)
VALUES
    (2222,0,'***','***',1111,now(),false,'testi_client',null,null);

INSERT INTO organisaatiohenkilo (id, version, organisaatio_oid, henkilo_id, passivoitu)
VALUES
    (1111,0,'1.2.246.562.10.00000000001',1111,false);

INSERT INTO text_group (id, version)
VALUES
    (1,0);

INSERT INTO text (id, version, lang, text, textgroup_id)
VALUES
    (1,0,'FI','',1);

INSERT INTO palvelu (id, version, name, palvelutyyppi, textgroup_id, kokoelma_id)
VALUES
    (1111,0,'KAYTTOOIKEUS','YKSITTAINEN',1,null);

INSERT INTO kayttooikeus (id, version, palvelu_id, rooli, textgroup_id)
VALUES
    (1111,0,1111,'CRUD',null);

INSERT INTO kayttooikeusryhma (id, version, name, textgroup_id, hidden, rooli_rajoite, kuvaus_id, ryhma_restriction, allowed_usertype, muokattu, muokkaaja)
VALUES
    (1111,0,'PALVELU',1,false,null,1,false,null,'2021-01-04 13:04:15.659','1.2.246.562.24.30433385401');

INSERT INTO kayttooikeusryhma_kayttooikeus (kayttooikeusryhma_id, kayttooikeus_id)
VALUES
    (1111,1111);

INSERT INTO myonnetty_kayttooikeusryhma_tapahtuma (id, version, aikaleima, syy, tila, kasittelija_henkilo_id, kayttooikeusryhma_id, organisaatiohenkilo_id, voimassaalkupvm, voimassaloppupvm)
VALUES
    (1,0,'2017-05-03 15:52:57.253',null,'MYONNETTY',1111,1111,1111,'2017-05-03','2099-05-03');

INSERT INTO oauth2_client (id, secret, uuid, kasittelija_henkilo_id, updated, created)
VALUES
    ('testi_client', 'secret', '5bec585a-c066-4dae-9317-4a0078168b9e', 1111, now(), now());