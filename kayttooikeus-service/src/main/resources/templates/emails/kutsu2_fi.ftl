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
        <h1>${subject}</h1>
        <p>
            Hei ${kutsu.etunimi} ${kutsu.sukunimi},
        </p>
        <p>
            sinut on kutsuttu Virkailijan opintopolkuun. Sinulle on annettu seuraavat käyttöoikeudet:
        </p>
        <#if organisaatiot?? && (organisaatiot?size > 0)>
            <#list organisaatiot as org>
                <p>
                <#if org.name??>
                    <strong>${org.name}</strong>
                </#if>
                <#if org.permissions?? && (org.permissions?size > 0)>
                    <ul>
                        <#list org.permissions as permission><li>${permission}</li></#list>
                    </ul>
                </#if>
                </p>
            </#list>
        </#if>
        <#if kutsu.saate??>
            <p>
                Saateviesti kutsujalta: ${kutsu.saate}
            </p>
        </#if>
        <p>
            Rekisteröidy käyttäjäksi alla olevan linkin kautta. Rekisteröityminen vaatii vahvan tunnistautumisen.
        </p>
        <p>
            <a href="${linkki}">Linkki rekisteröitymiseen</a>
        </p>
        <p>
            Kutsun lähetti: ${kutsuja}
        </p>
        <p>
            Kutsu on voimassa ${voimassa} asti
        </p>
        <p>
            Lisätietoa Virkailijan opintopolusta:
            <ul>
                <li>Virkailijan opintopolun osoite <a href="https://virkailija.opintopolku.fi">https://virkailija.opintopolku.fi</a>.</li>
                <li>Ohjeet: <a href="https://wiki.eduuni.fi/x/f5vTGg">https://wiki.eduuni.fi/x/f5vTGg</a></li>
            </ul>
        </p>
        <p>
            Virkailijan opintopolun käyttäjien henkilötiedot tallennetaan oppijanumerorekisteriin ja niitä käsitellään tietosuojaselosteen mukaisesti: <a href="https://opintopolku.fi/konfo/fi/sivu/tietosuojaselosteet-ja-evasteet">https://opintopolku.fi/konfo/fi/sivu/tietosuojaselosteet-ja-evasteet</a>
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="https://virkailija.opintopolku.fi/cas/images/logo_oph.svg" alt="Opetushallitus" />
    </div>
</body>
</html>

