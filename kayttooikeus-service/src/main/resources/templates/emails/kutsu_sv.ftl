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
            Hej ${kutsu.etunimi} ${kutsu.sukunimi},
        </p>
        <p>
            du har fått en inbjudan att fungera som administratör inom tjänstehelheten för undervisningsförvaltningen. Du har fått de användarrättigheter som beskrivs nedan.
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
        <#if kutsu.saate??><p>Följebrev: ${kutsu.saate}</p></#if>
        <p>
            För att få tillgång till tjänsten, bör du registrera dig via länken nedan genom att med mobilcertifikat, bankkoder eller elektroniskt identifieringskort identifiera dig starkt.
        </p>
        <p>
            <a href="${linkki}">Länk till registrering</a>
        </p>
        <p>
             Efter registreringen loggar man in i tjänsten på adressen <a href="https://virkailija.opintopolku.fi">https://virkailija.opintopolku.fi</a>.
        </p>
        <p>
            Avsändare ${kutsuja}
        </p>
        <p>
            Inbjudan är i kraft till ${voimassa}
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>
