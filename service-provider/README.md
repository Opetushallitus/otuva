Service Provider App
=
Toimii service providerina HAKA kirjautumisessa. Kaikki faktat HAKA-kirjautumisesta ja vähän päälle löytyy
lähtökohtaisesti OPH:n wikistä sivulta [HAKA-autentikaatio](https://wiki.eduuni.fi/display/OPHSS/HAKA-autentikaatio).
Muita lukemisen arvoisia sivuja ovat [Haka metadata](https://wiki.eduuni.fi/display/CSCHAKA/Haka+metadata),
[Testipalvelimet](https://wiki.eduuni.fi/display/CSCHAKA/Testipalvelimet),
[Usein kysytyt kysymykset](https://wiki.eduuni.fi/display/CSCHAKA/Usein+kysytyt+kysymykset)
ja [Haka](https://wiki.eduuni.fi/display/OPHPALV/Haka).

Lokaali ajaminen tapahtuu QA-ympäristöä vasten käyttäen luokalle konfiguroitua testihakaa. Tämä joudutaan tekemään näin koska haka palautuu itest-virkailija.oph.ware.fi osoitteeseen ja jotta tämä saadaan ohjattua localhostiin pitää se varata hosts-tiedostoon jolloin luokan käyttö dns-nimellä ei onnistu.

Toistuvat ylläpidolliset toimet
-
* [HAKA varmenteen uusiminen](haka-varmenteen-uusiminen.md)

Lokaali ajaminen
-
`./start-local-env.sh`