package fi.vm.sade;

import java.util.Optional;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.TransactionOperations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Monitorable
public class OtuvaJpaTicketRegistry extends JpaTicketRegistry {
    private final TransactionOperations transactionTemplate;

    @PersistenceContext(unitName = "jpaTicketRegistryContext")
    private EntityManager entityManager;

    public OtuvaJpaTicketRegistry(final CipherExecutor cipherExecutor,
                             final TicketSerializationManager ticketSerializationManager,
                             final TicketCatalog ticketCatalog,
                             final ConfigurableApplicationContext applicationContext,
                             final JpaBeanFactory jpaBeanFactory,
                             final TransactionOperations transactionTemplate,
                             final CasConfigurationProperties casProperties) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext, jpaBeanFactory, transactionTemplate, casProperties);
        this.transactionTemplate = transactionTemplate;
    }

    public Optional<String> getTicketIdWithSessionindex(String sessionindex) {
        return transactionTemplate.execute(status -> {
            val factory = getJpaTicketEntityFactory();
            val sql = "SELECT t.id FROM cas_tickets t WHERE t.type = :type AND (t.attributes->'sessionindex')::jsonb ? :sessionindex";
            LOGGER.info("Executing SQL query [{}]", sql);
            val query = entityManager.createNativeQuery(sql, factory.getType());
            query.setParameter("type", getTicketTypeName(TicketGrantingTicket.class));
            query.setParameter("sessionindex", sessionindex);
            return (Optional<String>) query.getResultStream().findFirst();
        });
    }
}
