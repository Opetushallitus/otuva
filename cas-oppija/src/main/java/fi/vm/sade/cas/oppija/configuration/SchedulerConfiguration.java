package fi.vm.sade.cas.oppija.configuration;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.SchedulerBuilder;
import com.github.kagkarlsson.scheduler.task.OnStartup;
import com.github.kagkarlsson.scheduler.task.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Configuration
public class SchedulerConfiguration {

    private static final Predicate<Task> ON_STARTUP = OnStartup.class::isInstance;

    private final DataSource dataSource;
    private final List<Task<?>> tasks;

    public SchedulerConfiguration(DataSource dataSource, List<Task<?>> tasks) {
        this.dataSource = dataSource;
        this.tasks = tasks;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Scheduler scheduler() {
        Task[] knownTasks = tasks.stream().filter(not(ON_STARTUP)).toArray(Task[]::new);
        SchedulerBuilder builder = Scheduler.create(configureDataSource(dataSource), knownTasks);
        tasks.stream().filter(ON_STARTUP).map(task -> (Task & OnStartup) task).forEach(builder::startTasks);
        builder.threads(1);
        return builder.build();
    }

    private static DataSource configureDataSource(DataSource dataSource) {
        if (dataSource instanceof TransactionAwareDataSourceProxy) {
            return dataSource;
        }
        return new TransactionAwareDataSourceProxy(dataSource);
    }

}
