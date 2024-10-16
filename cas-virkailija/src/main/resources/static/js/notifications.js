function jqueryReady() {
  var text_fi =
    "Oma Opintopolussa ja Opintopolku-palveluiden Suomi.fi-kirjautumissa on käyttökatko testiympäristössä maanantaina 21.10.2024 klo 11–14 ja tuotantoympäristössä perjantaina 25.10.2024 klo 11–14. Käyttökatkojen aikana Oma Opintopolku sekä Suomi.fi-tunnistautuminen kaikissa Opintopolku-palveluissa ovat poissa käytöstä. Pahoittelemme katkojen aiheuttamaa haittaa.";
  var text_sv =
    "Min Studieinfo -tjänsten och Suomi.fi-inloggningen i alla Studieinfo-testtjänsterna ur bruk i testmiljön på måndagen den 21.10.2024 kl. 11–14 och i produktionmiljön på fredagen den 25.10.2024 kl 11–14. Vi ber om ursäkt för eventuella olägenheter som avbrotten orsakar.";
  var language = $("body").attr("data-language") || "fi";

  $("<div></div>", {
    class: "row justify-content-center ilmoitus",
    text: language === "sv" ? text_sv : text_fi,
  }).prependTo("main");
}
