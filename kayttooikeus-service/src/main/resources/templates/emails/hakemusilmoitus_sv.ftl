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
            Hej ${henkiloDto.kutsumanimi!henkiloDto.etunimet},
        </p>
        <p>
            Du har fått nya anhållanden om användarrättigheter.
        </p>
        <p>
            Fortsätt till tjänsten: <a href="${linkki}">${linkki}</a>
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>
