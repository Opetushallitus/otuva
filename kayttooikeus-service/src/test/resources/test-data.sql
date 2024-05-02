INSERT INTO henkilo (id, oidhenkilo, henkilotyyppi, etunimet_cached, sukunimi_cached, passivoitu_cached, duplicate_cached, vahvasti_tunnistettu, hetu_cached, kutsumanimi_cached, sahkopostivarmennus_aikaleima)
VALUES
    (1,'1.2.246.562.24.37535704268', 'VIRKAILIJA', 'Hetuttomien', 'Hypistelijä', false, false, true, '280760-542K', 'Hetuttomien', null),
    (2,'1.2.246.562.24.67357428459', 'VIRKAILIJA', 'Hetullisten', 'Hypistelijä', false, false, true, '280760-542K', 'Hetullisten', null);

INSERT INTO organisaatiohenkilo (id, version, organisaatio_oid, henkilo_id, passivoitu, tyyppi, voimassa_alku_pvm, voimassa_loppu_pvm)
VALUES
    (1,0,'1.2.246.562.10.71948887212',1,false,null,null,null),
    (2,0,'1.2.246.562.10.71948887213',1,false,null,null,null),
    (3,0,'1.2.246.562.10.71948887213',2,false,null,null,null);

INSERT INTO text_group (id, version)
VALUES
    (1,0),
    (2,0),
    (3,0),
    (4,0),
    (5,0),
    (6,0),
    (7,0),
    (8,0),
    (9,0),
    (10,0);

INSERT INTO text (id, version, lang, text, textgroup_id)
VALUES
    (1,0,'FI','Lukuoikeus',1),
    (2,0,'FI','Kirjoitusoikeus',2),
    (3,0,'FI','Oikeus',3),
    (4,0,'FI','Ryhma1',4),
    (5,0,'FI','Ryhma2',5),
    (6,0,'FI','Palvelu1',6),
    (7,0,'FI','Palvelu2',7),
    (8,0,'FI','Kuvaus1',8),
    (9,0,'FI','Kuvaus2',9),
    (10,0,'FI','',10);

INSERT INTO palvelu (id, version, name, palvelutyyppi, textgroup_id, kokoelma_id)
VALUES
    (1,0,'Palvelu','YKSITTAINEN',6,null),
    (2,0,'Palvelu2','YKSITTAINEN',7,null);

INSERT INTO kayttooikeus (id, version, palvelu_id, rooli, textgroup_id)
VALUES
    (1,0,1,'READ',1),
    (2,0,1,'WRITE',2),
    (3,0,2,'READ',3);

INSERT INTO kayttooikeusryhma (id, version, name, textgroup_id, hidden, rooli_rajoite, kuvaus_id, ryhma_restriction, allowed_usertype, muokattu, muokkaaja)
VALUES
    (1,0,'nimi',4,false,null,8,false,null,'2021-01-04 13:04:15.659','1.2.246.562.24.30433385401'),
    (2,0,'nimi2',5,false,null,9,false,null,'2021-01-04 13:04:15.659','1.2.246.562.24.30433385401');

INSERT INTO kayttooikeusryhma_kayttooikeus (kayttooikeusryhma_id, kayttooikeus_id)
VALUES
    (1,1),
    (1,2),
    (2,3);

INSERT INTO myonnetty_kayttooikeusryhma_tapahtuma (id, version, aikaleima, syy, tila, kasittelija_henkilo_id, kayttooikeusryhma_id, organisaatiohenkilo_id, voimassaalkupvm, voimassaloppupvm)
VALUES
    (1,0,'2017-05-03 15:52:57.253',null,'MYONNETTY',2,1,1,'2017-05-03','2050-05-03'),
    (2,0,'2017-05-03 15:52:57.251',null,'MYONNETTY',2,2,1,'2017-05-02','2050-05-04'),
    (3,0,'2017-05-03 15:52:57.232',null,'MYONNETTY',1,1,2,'2017-05-08','2050-05-14'),
    (4,0,'2017-05-03 15:52:57.252',null,'MYONNETTY',1,2,2,'2017-05-06','2050-05-02');

INSERT INTO kayttooikeusryhma_tapahtuma_historia (id, version, aikaleima, tila, syy, kayttooikeusryhma_id, organisaatiohenkilo_id, kasittelija_henkilo_id)
VALUES
    (1,0,'2014-03-27 12:30:06.97','MYONNETTY','Oikeuksien lisäys',1,1,2),
    (2,0,'2014-03-27 12:30:06.96','UUSITTU','Oikeuksien lisäys',1,2,1),
    (3,0,'2014-03-27 12:30:06.94','MYONNETTY','Oikeuksien lisäys',2,2,1);