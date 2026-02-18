package fi.vm.sade.cas.oppija.configuration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestIdFilter implements Filter {
    public static final String REQUEST_ID_ATTRIBUTE = RequestIdFilter.class.getName() + ".requestId";
    public static final String REQUEST_ID_HEADER = "x-amzn-trace-id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            var requestId = getFromTraceHeader(request).orElseGet(RequestIdFilter::generateRequestId);
            MDC.put(REQUEST_ID_ATTRIBUTE, requestId);
            request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_ATTRIBUTE);
        }
    }

    public static Optional<String> getFromTraceHeader(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            var httpRequest = (HttpServletRequest) request;
            return Optional.ofNullable(httpRequest.getHeader(REQUEST_ID_HEADER));
        }
        return Optional.empty();
    }

    public static String generateRequestId() {
        return java.util.UUID.randomUUID().toString();
    }
}
