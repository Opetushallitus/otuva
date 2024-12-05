SELECT insertpalvelu('OVARA-VIRKAILIJA', 'Opiskelijavalintojen raportoinnin virkailijakäyttöliittymä');
SELECT insertkayttooikeus('OVARA-VIRKAILIJA', 'OPO', 'Saa muodostaa Ovaran Opo-raportin');
SELECT insertkayttooikeus('OVARA-VIRKAILIJA', '2ASTE', 'Saa muodostaa Ovaran toisen asteen raportit');
SELECT insertkayttooikeus('OVARA-VIRKAILIJA', 'KK', 'Saa muodostaa Ovaran korkeakouluraportit');
SELECT insertkayttooikeus('OVARA-VIRKAILIJA', 'OPH_PAAKAYTTAJA', 'Saa käyttää Ovara-virkailijan kaikkea toiminnallisuutta ilman rajoituksia');
SELECT insertkayttooikeus('OVARA-VIRKAILIJA', 'HAKIJATIEDOT', 'Saa muodostaa henkilötietoja sisältäviä raportteja');