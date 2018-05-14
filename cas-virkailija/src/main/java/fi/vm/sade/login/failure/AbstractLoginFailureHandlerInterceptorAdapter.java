package fi.vm.sade.login.failure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.webflow.core.collection.MutableAttributeMap;

public abstract class AbstractLoginFailureHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoginFailureHandlerInterceptorAdapter.class);
    private static final String SUCCESSFUL_AUTHENTICATION_EVENT = "success";
    private static final String AUTHENTICATION_RESULT = "authenticationResult";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!isPostRequest(request)) {
            return true;
        }

        int loginDelay = getMinutesToAllowLogin(request);

        if(0 != loginDelay ) {
            response.sendError(409);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!isPostRequest(request)) {
            return;
        }
        if( hasSuccessfulAuthentication(request, response) ) {
            notifySuccessfullLogin(request);
        } else if (hasRedirectToLoginPage(request, response)) {
            LOGGER.debug("There is redirect to login page. Passing it through.");
        } else {
            notifyFailedLoginAttempt(request);
        }
    }

    private boolean isPostRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    private boolean hasSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response) {
        final RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        Event currentEvent = null == context ? null : context.getCurrentEvent();

        if (context == null || currentEvent == null) {
            LOGGER.debug("There is no current flow event in request. Checking redirect...");
            return hasRedirectToService(request, response);
        }

        LOGGER.debug("Current event ID is {}", currentEvent.getId());
        final MutableAttributeMap<Object> flowScope = context.getFlowScope();
        return flowScope != null && SUCCESSFUL_AUTHENTICATION_EVENT.equals(flowScope.get(AUTHENTICATION_RESULT));
    }

    private String getRedirectLocation(HttpServletResponse response) {
        if(!(response instanceof RedirectHttpServletResponseWrapper)) {
            LOGGER.warn("HttpServletResponse {} is not instance of RedirectHttpServletResponseWrapper. " +
                    "Filter configuration might be missing or wrong!", response.getClass().getName());
            return null;
        }

        RedirectHttpServletResponseWrapper responseWrapper = (RedirectHttpServletResponseWrapper)response;
        if( !responseWrapper.redirectSent() ) {
            return null;
        }

        return responseWrapper.getRedirectLocation();
    }

    private boolean hasRedirectToLoginPage(HttpServletRequest request, HttpServletResponse response) {
        String redirectLocation = getRedirectLocation(response);
        return null == redirectLocation ? false : redirectLocation.startsWith("/cas/login");
    }

    private boolean hasRedirectToService(HttpServletRequest request, HttpServletResponse response) {

        String serviceUri = request.getParameter("service");
        if(null == serviceUri || "".equals(serviceUri)) {
            LOGGER.debug("There is no service parameter in HttpServletRequest.");
            return false;
        }

        String redirectLocation = getRedirectLocation(response);
        LOGGER.debug("There is redirect to {} and service parameter is {}.", redirectLocation, serviceUri);
        return null == redirectLocation ? false : redirectLocation.startsWith(serviceUri);
    }

    public abstract int getMinutesToAllowLogin(HttpServletRequest request);

    public abstract void notifySuccessfullLogin(HttpServletRequest request);

    public abstract void notifyFailedLoginAttempt(HttpServletRequest request);

}
