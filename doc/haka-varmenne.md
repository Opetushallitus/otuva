HAKA varmenteen uusiminen
=
SAML kirjautuminen perustuu julkisen avaimen infrastruktuuriin. HAKA luottamusverkosto
ylläpitää [resurssirekisteriä](https://haka.funet.fi/metadata/haka-metadata.xml) josta eri
palveluntarjoajien julkiset avaimet löytyvät.

Cas-virkailijan Haka-varmenteet ovat tallessa ympäristöjen parameter storeissa, joista saa tarvittaessa ulos resurssirekisterin vaatiman julkisen avaimen.

Varmenteet eivät ole ikuisia vaan vanhenevat määrättynä ajankohtana.
Varmenteen vanheneminen taroittaa että kyseinen sisäänkirjautumismenetelmä ei toimi.
Tyypillisesti tästä saa resurssirekisteristä automaattisesti varoituksen ennakkoon.

Varmenteen vanhetessa luodaan uusi vanhentuneen tilalle sekä keystoreen
että resurssirekisteriin.

Tarvittavat työkalut
-

* [keytool](https://docs.oracle.com/en/java/javase/11/tools/keytool.html)

Työvaiheet
-

Tarkista vanhan varmenteen tiedot
--

Ota talteen vanhan varmenteen tiedot:

`keytool -list -v -alias ${key-alias} -keystore ${keystore} --storepass ${keystore-password}`

Generoi uusi varmenne
--

Aliaksena käytetään muotoa `${ympäristö}_hakasp_selfsigned_${vuosi}` esim. `untuva_hakasp_selfsigned_2023`.

Huom! Varmista että dname parametrin tiedot ovat samat kuin aiemmassa varmenteessa.

Esimerkki untuva-ympäristöstä vuoden 2023 sertifikaatin luonnista:

```
keytool -keystore ${keystore} -storepass ${keystore-password} -genkey -keyalg RSA -sigalg SHA256withRSA -validity 1800 -keysize 4096 \
  -alias untuva_hakasp_selfsigned_2023 -keypass ${key-password} \
  -dname "CN=virkailija.untuvaopintopolku.fi, OU=Opetushallitus, O=Opetushallitus, L=HELSINKI, ST=UUSIMAA, C=FI"
```

Lisää uusi varmenne resurssirekisteriin
--

Toimita julkinen avain resurssirekisterin ylläpidosta vastaavalle virkamiehelle, joka lisää sertifikaatin resurssirekisteriin aiemman varmenteen rinnalle.

```
keytool -export -rfc -alias ${key-alias} -keystore ${keystore} --storepass ${keystore-password} > untuva_hakasp_selfsigned_2023.cert
```

Odota kunnes resurssirekisteri on päivittynyt ([tukee](https://wiki.eduuni.fi/display/CSCHAKA/SAML-varmenteen+vaihtaminen) useampia samanaikaisia varmenteita)

Päivitä salaisuuksienhallinta
--

Informoi tunnistuspalveluita
--

Uusi varmenne ei päivity automaattisesti kaikkiin tunnistuspalveluhin vaan se tulee tehdä niihin manuaalisesti.
Ainakin Valtorin ylläpitämä Valtti on tällainen.
Ko. tunnistuspalveluita pitää siis informoida ajoissa ja kooridinoida varmenteen vaihdon ajankohta.

Vanhan varmenteen poisto käytöstä
--
