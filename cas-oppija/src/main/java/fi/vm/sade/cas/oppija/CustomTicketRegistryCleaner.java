package fi.vm.sade.cas.oppija;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.lock.LockRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Slf4j
@Component("ticketRegistryCleaner")
@Transactional(transactionManager = "ticketTransactionManager")
public class CustomTicketRegistryCleaner extends DefaultTicketRegistryCleaner {
    private static final long LOCK_ID = 3601127493855415L;
    private final JdbcTemplate jdbcTemplate;

    public CustomTicketRegistryCleaner(
            LockRepository lockRepository,
            LogoutManager logoutManager,
            TicketRegistry ticketRegistry,
            DataSource dataSource
    ) {
        super(lockRepository, logoutManager, ticketRegistry);
        LOGGER.info("Initializing [{}]", getClass().getSimpleName());
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int clean() {
        LOGGER.info("Running [{}]", getClass().getSimpleName());
        if (!tryAcquireTaskLock()) {
            LOGGER.info("Failed to acquire lock for ticket registry cleaner; it is already running");
            return 0;
        }
        return super.clean();
    }

    private boolean tryAcquireTaskLock() {
        var sql = "SELECT pg_try_advisory_xact_lock(?)";
        var result = jdbcTemplate.queryForObject(sql, Boolean.class, LOCK_ID);
        return Boolean.TRUE.equals(result);
    }
}
