package fi.vm.sade.kayttooikeus.aspects;

import fi.vm.sade.auditlog.Operation;

public enum KayttooikeusOperation implements Operation {

    PASSIVOI_HENKILO,
    CHANGE_PASSWORD,
    UPDATE_HAKATUNNISTEET,
    CREATE_KAYTTAJATIEDOT,
    UPDATE_KAYTTAJATIEDOT,
    CREATE_HENKILO_BY_KUTSU,
    APPROVE_OR_REJECT_KAYTTOOIKEUSANOMUS,
    SEND_KAYTTOOIKEUSANOMUS_NOTIFICATION,
    REMOVE_USER_FROM_KAYTTOOIKEUSANOMUS,
    CREATE_KAYTTOOIKEUSANOMUS,
    ADD_KAYTTOOIKEUSRYHMA_TO_HENKILO,
    SEND_KAYTTOOIKEUS_EXPIRATION_REMINDER,
    REMOVE_EXPIRED_KAYTTOOIKEUDET,
    CREATE_KAYTTOOIKEUSRYHMA,
    CREATE_KAYTTOIKEUS,
    UPDATE_KAYTTOOIKEUSRYHMA,
    CREATE_KUTSU,
    DELETE_KUTSU,
    UPDATE_ORGANISAATIO_CACHE,
    CREATE_OR_UPDATE_ORGANISAATIO_HENKILO,
    FIND_OR_CREATE_ORGANISAATIO_HENKILOT,
    PASSIVOI_ORGANISAATIO_HENKILO,
    ENABLE_MFA_GAUTH,
    DISABLE_MFA_GAUTH,

}
