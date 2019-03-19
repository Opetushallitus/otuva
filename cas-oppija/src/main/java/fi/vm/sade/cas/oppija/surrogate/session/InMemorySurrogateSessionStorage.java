package fi.vm.sade.cas.oppija.surrogate.session;

import fi.vm.sade.cas.oppija.surrogate.SurrogateSession;
import fi.vm.sade.cas.oppija.surrogate.SurrogateSessionStorage;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySurrogateSessionStorage implements SurrogateSessionStorage {

    private final Map<String, SurrogateSession> data = new ConcurrentHashMap<>();

    @Override
    public void add(String token, SurrogateSession session) {
        data.put(token, session);
    }

    @Override
    public SurrogateSession remove(String token) {
        return data.remove(token);
    }

    @Override
    public synchronized long clean(Instant instant) {
        int size = data.size();
        data.entrySet().removeIf(entry -> entry.getValue().created.isBefore(instant));
        return size - data.size();
    }

}
