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
        <h3>${subject}</h3>
        <p>
            Hej ${henkiloDto.kutsumanimi} ${henkiloDto.sukunimi}
        </p>
        <p>
            Din anhållan om användarrättigheter har behandlats.
        </p>
        <#assign tilat = {"HYLATTY" : "Underkänd", "MYONNETTY" : "Beviljad"}>
        <p>
            ${rooli.nimi} ${tilat[rooli.tila]} <#if rooli.hylkaysperuste??>- ${rooli.hylkaysperuste}</#if>
        </p>
        <p>
            Du kan fortsätta till tjänsten: <a href="${linkki}">${linkki}</a>.
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>

