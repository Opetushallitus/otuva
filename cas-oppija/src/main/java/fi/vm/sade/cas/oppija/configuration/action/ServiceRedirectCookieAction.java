package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class ServiceRedirectCookieAction extends AbstractAction {

    static final String SERVICE_COOKIE = "_oph_service";
    /*
     * Keksin eliniän tarvitsee kattaa vain SAML-logout ja sitä seuraavat redirectit;
     * virkailijan tunnistautumisessa joka tapauksessa vain minuutin aikaikkuna.
     */
    static final int SERVICE_COOKIE_MAX_AGE = 60;

    @Override
    protected Event doExecute(RequestContext context) {
        var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        var service = getServiceRedirectCookie(request);
        if (service != null) {
            WebUtils.putLogoutRedirectUrl(context, service);
            clearServiceRedirectCookie(response);
        }
        return null;
    }

    static String getServiceRedirectCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(
                (cookie) -> SERVICE_COOKIE.equals(cookie.getName())
        ).findFirst().map(Cookie::getValue).orElse(null);
    }

    /*
     * Parametrit, ml. SAMLin läpi kuljetettava RelayState, hukataan matkalla. Koska
     * myös sessio tuhotaan, hillotaan tieto paluuosoitteesta keksiin.
     */
    static void setServiceRedirectCookie(HttpServletResponse response, String value) {
        response.addCookie(serviceRedirectCookie(value));
    }

    static void clearServiceRedirectCookie(HttpServletResponse response) {
        response.addCookie(serviceRedirectCookie(null));
    }

    private static Cookie serviceRedirectCookie(String value) {
        Cookie serviceRedirectCookie = new Cookie(SERVICE_COOKIE, value);
        serviceRedirectCookie.setHttpOnly(true);
        // tyhjä arvo -> keksi poistetaan
        serviceRedirectCookie.setMaxAge(value == null ? 0 : SERVICE_COOKIE_MAX_AGE);
        serviceRedirectCookie.setSecure(true);
        return serviceRedirectCookie;
    }
}
