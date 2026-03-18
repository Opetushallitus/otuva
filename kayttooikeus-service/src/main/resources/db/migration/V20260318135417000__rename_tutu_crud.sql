UPDATE kayttooikeus SET rooli = 'CRUD' where rooli = 'TUTU_CRUD' AND palvelu_id = (SELECT id FROM palvelu WHERE name = 'TUTU');
