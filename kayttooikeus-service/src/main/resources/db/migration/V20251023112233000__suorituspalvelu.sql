SELECT insertpalvelu('SUORITUSPALVELU', 'Suorituspalvelu');
SELECT insertkayttooikeus('SUORITUSPALVELU', 'PAAKAYTTAJA', 'Pääkäyttäjä');
SELECT insertkayttooikeus('SUORITUSPALVELU', 'HAKENEIDEN_KATSELIJA', 'Organisaatioon hakeneiden suoritusten katselija');
SELECT insertkayttooikeus('SUORITUSPALVELU', 'OPPIJOIDEN_KATSELIJA', 'Organisaation oppijoiden suoritusten katselija');
SELECT insertkayttooikeus('SUORITUSPALVELU', 'ULKOISET_RAJAPINNAT', 'Palvelukäyttäjä (ulkopuoliset rajapinnat)');
SELECT insertkayttooikeus('SUORITUSPALVELU', 'SISAISET_RAJAPINNAT', 'Palvelukäyttäjä (sisäiset rajapinnat)');