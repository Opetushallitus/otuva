<!doctype html>
<html lang="fi">
<head>
    <meta charset="utf-8">
    <title>${subject}</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
    <div class="box">
        <h3>${subject}</h3>
        <p>
            Hei ${kutsu.etunimi} ${kutsu.sukunimi},
        </p>
        <p>
            sinut on kutsuttu virkailijaksi Opetushallinnon palvelukokonaisuuteen. Sinulle on annettu käyttöoikeudet alla olevan mukaisesti.
        </p>
        <p>
            <#if organisaatiot?? && (organisaatiot?size > 0)>
                <p>
                    <#list organisaatiot as org>
                        <#if org.name??>
                            <strong>${org.name}</strong>
                        </#if>
                        <#if org.permissions?? && (org.permissions?size > 0)>
                            <p>
                                <#list org.permissions as permission>${permission}<br/></#list>
                            </p>
                        </#if>
                    </#list>
                </p>
            </#if>
        <#if kutsu.saate??><p>Saate: ${kutsu.saate}</p></#if>
        <p>
            Päästäksesi käyttämään palvelua, sinun tulee rekisteröityä alla olevan linkin kautta ja tunnistautua vahvasti mobiilivarmenteen, pankkitunnusten tai varmennekortin avulla.
        </p>
        <p>
            <a href="${linkki}">Linkki rekisteröitymiseen</a>
        </p>
        <p>
            Rekisteröitymisen jälkeen palveluun kirjaudutaan osoitteessa <a href="https://virkailija.opintopolku.fi">https://virkailija.opintopolku.fi</a>.
        </p>
        <p>
            Kutsun lähetti ${kutsuja}
        </p>
        <p>
            Kutsu on voimassa ${voimassa} asti
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>

