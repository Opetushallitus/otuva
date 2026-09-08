package fi.vm.sade;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TomcatGate {
    private volatile Connector connector;

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent e) {
        if (e.getWebServer() instanceof TomcatWebServer tomcat) {
            LOGGER.info("Pausing Tomcat connector to delay incoming requests until application is ready");
            this.connector = tomcat.getTomcat().getConnector();
            this.connector.pause();
        }
    }

    @EventListener
    public void onAppReady(ContextRefreshedEvent e) {
        LOGGER.info("Resuming Tomcat connector as application is ready to serve requests");
        if (connector != null) connector.resume();
    }
}
