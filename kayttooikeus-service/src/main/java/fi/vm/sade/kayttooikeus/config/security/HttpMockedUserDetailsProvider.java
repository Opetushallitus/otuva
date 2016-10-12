package fi.vm.sade.kayttooikeus.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fi.vm.sade.authentication.ldap.SadeUserDetailsWrapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.util.Optional.ofNullable;

/**
 * User: tommiratamaa
 * Date: 10/10/2016
 * Time: 11.06
 */
public class HttpMockedUserDetailsProvider implements UserDetailsService {
    private final String url;
    private final ObjectMapper objectMapper;
    
    public HttpMockedUserDetailsProvider(String url, ObjectMapper objectMapper) {
        this.url = url;
        this.objectMapper = objectMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            URL getUrl = new URL(this.url + username);
            HttpURLConnection con = (HttpURLConnection) getUrl.openConnection();
            con.setRequestMethod("GET");
            String responseBody;
            try (InputStream in = con.getInputStream()) {
                responseBody = new String(ByteStreams.toByteArray(in), "UTF-8");
            }
            con.getInputStream().close();
            if (con.getResponseCode() != 200) {
                throw new UsernameNotFoundException(responseBody);
            }
            MockedUserDetails details = objectMapper.readerFor(MockedUserDetails.class).readValue(responseBody);
            return new SadeUserDetailsWrapper(details, ofNullable(details.getLang()).orElse("FI"));
        } catch (IOException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
}
