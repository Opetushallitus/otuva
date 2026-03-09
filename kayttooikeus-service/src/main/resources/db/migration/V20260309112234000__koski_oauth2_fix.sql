update text
set text = 'OmaData OAuth2 scope OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT_JA_VALINTATIEDOT'
where textgroup_id = (
    select textgroup_id
    from kayttooikeus
    where rooli = 'OMADATAOAUTH2_OPISKELUOIKEUDET_KAIKKI_TIEDOT_JA_VALINTATIEDOT'
);