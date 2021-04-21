package fi.vm.sade.cas.oppija.configuration.action;

import org.springframework.webflow.action.AbstractAction;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public abstract class AbstractServiceParamAction extends AbstractAction {
    protected static final String SERVICE_COOKIE = "_oph_service";
    /*
     * Keksin eliniän tarvitsee kattaa vain SAML-logout ja sitä seuraavat redirectit;
     * virkailijan tunnistautumisessa joka tapauksessa vain minuutin aikaikkuna.
     */
    protected static final int SERVICE_COOKIE_MAX_AGE = 60;

    /*
     * Parametrit, ml. SAMLin läpi kuljetettava RelayState, hukataan matkalla. Koska
     * myös sessio tuhotaan, hillotaan tieto paluuosoitteesta keksiin.
     */
    protected void setServiceRedirectCookie(HttpServletResponse response, String value) {
        response.addCookie(serviceRedirectCookie(value));
    }

    protected String getServiceRedirectCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(
                (cookie) -> SERVICE_COOKIE.equals(cookie.getName())
        ).findFirst().map(Cookie::getValue).orElse(null);
    }

    protected void clearServiceRedirectCookie(HttpServletResponse response) {
        response.addCookie(serviceRedirectCookie(null));
    }

    private Cookie serviceRedirectCookie(String value) {
        Cookie serviceRedirectCookie = new Cookie(SERVICE_COOKIE, value);
        serviceRedirectCookie.setHttpOnly(true);
        // tyhjä arvo -> keksi poistetaan
        serviceRedirectCookie.setMaxAge(value == null ? 0 : SERVICE_COOKIE_MAX_AGE);
        serviceRedirectCookie.setSecure(true);
        return serviceRedirectCookie;
    }

}
