package fi.vm.sade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final JpaBeanFactory jpaBeanFactory;

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
        this.jpaBeanFactory = jpaBeanFactory;
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        LOGGER.info("Executing getSessionsWithAttributes");
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

        val query = entityManager.createNativeQuery(sql, factory.getType());
        return jpaBeanFactory.streamQuery(query)
            .map(BaseTicketEntity.class::cast)
            .map(factory::toTicket)
            .map(this::decodeTicket)
            .filter(ticket -> !ticket.isExpired());
    }
}
