package fi.vm.sade.kayttooikeus.service.external;

import fi.vm.sade.kayttooikeus.service.AbstractServiceTest;
import net.jadler.junit.rule.JadlerRule;
import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Rule;

import static fi.vm.sade.kayttooikeus.util.FreePortUtil.portNumberBySystemPropertyOrFree;

public abstract class AbstractClientTest extends AbstractServiceTest {
    protected static final int OK = HttpStatus.SC_OK;
    protected static final int MOCK_SERVER_PORT = portNumberBySystemPropertyOrFree("test.port");
    @Rule
    public final JadlerRule jalder = new JadlerRule(new JdkStubHttpServer(MOCK_SERVER_PORT));
}
