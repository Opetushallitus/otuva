<!doctype html>
<html lang="sv">
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
            Hej ${kutsu.etunimi} ${kutsu.sukunimi},
        </p>
        <p>
            du har blivit inbjuden till Studieinfo för administratörer. Du har beviljats följande användarrättigheter:
        </p>
        <#if organisaatiot?? && (organisaatiot?size > 0)>
            <#list organisaatiot as org>
                <p>
                <#if org.name??>
                    <strong>${org.name}</strong>
                </#if>
                <#if org.permissions?? && (org.permissions?size > 0)>
                    <ul>
                        <#list org.permissions as permission>
                            <li>${permission}</li>
                        </#list>
                    </ul>
                </#if>
                </p>
            </#list>
        </#if>
        <#if kutsu.saate??>
            <p>
                Meddelande från avsändaren: ${kutsu.saate}
            </p>
        </#if>
        <p>
            Registrera dig som användare via länken nedan. Registreringen kräver stark autentisering.
        </p>
        <p>
            <a href="${linkki}">Länk till registreringen</a>
        </p>
        <p>
            Inbjudan skickad av: ${kutsuja}
        </p>
        <p>
            Inbjudan är giltig till och med ${voimassa}
        </p>
        <p>
            Mer information om Studieinfo för administratörer:
            <ul>
                <li>Adress till Studieinfo för administratör: <a href="https://virkailija.opintopolku.fi">https://virkailija.opintopolku.fi</a>.</li>
                <li>Anvisningar: <a href="https://wiki.eduuni.fi/x/f5vTGg">https://wiki.eduuni.fi/x/f5vTGg</a></li>
            </ul>
        </p>
        <p>
            Personuppgifter för användare av Studieinfo för administratörer lagras i lärandenummerregistret och behandlas i enlighet med dataskyddsbeskrivningen: <a href="https://opintopolku.fi/konfo/sv/sivu/dataskyddsbeskrivningar-och-webbkakor">https://opintopolku.fi/konfo/sv/sivu/dataskyddsbeskrivningar-och-webbkakor</a>
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="https://virkailija.opintopolku.fi/cas/images/logo_oph.svg" alt="Opetushallitus" />
    </div>
</body>
</html>

