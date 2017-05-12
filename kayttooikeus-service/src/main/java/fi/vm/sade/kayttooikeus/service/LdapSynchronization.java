package fi.vm.sade.kayttooikeus.service;

public interface LdapSynchronization {

    /* These priority levels define different behavior for LDAP update process
     * since night time batch updates are run in a different way than daytime
     * batch updates
     */
    int REALTIME_PRIORITY = -1;
    int BATCH_PRIORITY = 0;
    int ASAP_PRIORITY = 1;
    int NORMAL_PRIORITY = 2;
    int NIGHT_PRIORITY = 3;

    // This value is a trigger for running all users
    String RUN_ALL_BATCH = "runall";

    void updateAccessRightGroup(Long id);

    void updateHenkilo(String henkiloOid, int priority);
}
