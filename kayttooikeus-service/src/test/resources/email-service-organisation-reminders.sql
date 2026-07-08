INSERT INTO henkilo (id, oidhenkilo, henkilotyyppi, etunimet_cached, sukunimi_cached, passivoitu_cached, duplicate_cached, vahvasti_tunnistettu, hetu_cached, kutsumanimi_cached, sahkopostivarmennus_aikaleima)
VALUES
    (10001, '1.2.246.562.24.fi', 'VIRKAILIJA', 'Fi', 'Vastuukayttaja', false, false, true, null, 'Fi', null),
    (10002, '1.2.246.562.24.sv', 'VIRKAILIJA', 'Sv', 'Vastuukayttaja', false, false, true, null, 'Sv', null),
    (10003, '1.2.246.562.24.duplicate', 'VIRKAILIJA', 'Duplicate', 'Vastuukayttaja', false, false, true, null, 'Duplicate', null),
    (10004, '1.2.246.562.24.other', 'VIRKAILIJA', 'Other', 'Kayttaja', false, false, true, null, 'Other', null);

INSERT INTO kayttajatiedot (id, version, password, salt, henkiloid, createdat, invalidated, username, mfaprovider, passwordchange)
VALUES
    (10001, 0, '***', '***', 10001, now(), false, 'fi-vastuukayttaja', null, null),
    (10002, 0, '***', '***', 10002, now(), false, 'sv-vastuukayttaja', null, null),
    (10003, 0, '***', '***', 10003, now(), false, 'duplicate-vastuukayttaja', null, null),
    (10004, 0, '***', '***', 10004, now(), false, 'other-kayttaja', null, null);

INSERT INTO organisaatiohenkilo (id, version, organisaatio_oid, henkilo_id, passivoitu)
VALUES
    (10001, 0, '1.2.246.562.10.10001', 10001, false),
    (10002, 0, '1.2.246.562.10.10002', 10002, false),
    (10003, 0, '1.2.246.562.10.10003', 10003, false),
    (10004, 0, '1.2.246.562.10.10004', 10004, false);

INSERT INTO text_group (id, version)
VALUES
    (10001, 0),
    (10002, 0),
    (10003, 0),
    (10004, 0);

INSERT INTO text (id, version, lang, text, textgroup_id)
VALUES
    (10001, 0, 'FI', 'Opintopolun vastuukayttaja', 10001),
    (10002, 0, 'FI', 'Organisaation vastuukayttaja', 10002),
    (10003, 0, 'FI', 'Muu kayttooikeusryhma', 10003),
    (10004, 0, 'FI', 'Muu kuvaus', 10004);

INSERT INTO kayttooikeusryhma (id, version, name, textgroup_id, hidden, rooli_rajoite, kuvaus_id, ryhma_restriction, allowed_usertype, muokattu, muokkaaja)
VALUES
    (123, 0, 'OPINTOPOLUN_VASTUUKAYTTAJA', 10001, false, null, 10002, false, null, '2026-01-01 00:00:00', 'test'),
    (124, 0, 'MUU_KAYTTOOIKEUSRYHMA', 10003, false, null, 10004, false, null, '2026-01-01 00:00:00', 'test');

INSERT INTO myonnetty_kayttooikeusryhma_tapahtuma (id, version, aikaleima, syy, tila, kasittelija_henkilo_id, kayttooikeusryhma_id, organisaatiohenkilo_id, voimassaalkupvm, voimassaloppupvm)
VALUES
    (10001, 0, '2026-01-01 00:00:00', null, 'MYONNETTY', 10001, 123, 10001, '2026-01-01', '2099-01-01'),
    (10002, 0, '2026-01-01 00:00:00', null, 'MYONNETTY', 10001, 123, 10002, '2026-01-01', '2099-01-01'),
    (10003, 0, '2026-01-01 00:00:00', null, 'MYONNETTY', 10001, 123, 10003, '2026-01-01', '2099-01-01'),
    (10004, 0, '2026-01-01 00:00:00', null, 'MYONNETTY', 10001, 124, 10004, '2026-01-01', '2099-01-01');
