package fi.vm.sade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
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

    public List<? extends Ticket> getSessionListWithAttributes(final Map<String, List<Object>> queryAttributes) {
        return transactionTemplate.execute(status -> {
            val factory = getJpaTicketEntityFactory();
            val criterias = queryAttributes.entrySet()
                .stream()
                .map(entry -> {
                    val criteriaValues = entry.getValue()
                        .stream()
                        .map(queryValue -> String.format("(t.attributes->'%s')\\:\\:jsonb \\?\\? '%s'", digestIdentifier(entry.getKey()), digestIdentifier(queryValue.toString())))
                        .collect(Collectors.joining(" OR "));
                    return String.format("(%s)", criteriaValues);
                })
                .collect(Collectors.joining(" AND "));
            val selectClause = new StringBuilder(String.format("SELECT t.* FROM %s t ", factory.getTableName()));
            val sql = String.format("%s WHERE t.type='%s' AND %s", selectClause,
                getTicketTypeName(TicketGrantingTicket.class), criterias);
            LOGGER.info("Executing SQL query [{}]", sql);

            val query = entityManager.createQuery(sql, factory.getType());
            return query
                .getResultStream()
                .map(BaseTicketEntity.class::cast)
                .map(factory::toTicket)
                .map(this::decodeTicket)
                .filter(ticket -> !ticket.isExpired())
                .collect(Collectors.toList());
        });
    }
}
