package fi.vm.sade.cas.oppija.surrogate;

import java.time.Instant;

public interface SurrogateSessionStorage {

    void add(String token, SurrogateSession session);

    SurrogateSession remove(String token);

    long clean(Instant instant);

}
