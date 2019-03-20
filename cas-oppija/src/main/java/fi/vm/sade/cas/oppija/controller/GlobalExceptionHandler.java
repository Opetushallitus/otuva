package fi.vm.sade.cas.oppija.controller;

import fi.vm.sade.cas.oppija.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Void> handle(BadRequestException exception, HttpServletRequest request) {
        return handle(exception, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Void> handle(UnauthorizedException exception, HttpServletRequest request) {
        return handle(exception, request, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({UserException.class, SystemException.class, ApplicationException.class})
    public ResponseEntity<Void> handle(ApplicationException exception, HttpServletRequest request) {
        return handle(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Void> handle(UserException exception, HttpServletRequest request, HttpStatus status) {
        LOGGER.warn("Request to '{}' threw '{}', response is '{}'", toRequestLine(request), exception, status);
        return ResponseEntity.status(status).build();
    }

    private ResponseEntity<Void> handle(ApplicationException exception, HttpServletRequest request, HttpStatus status) {
        LOGGER.error("Request to '{}' threw '{}', response is '{}'", toRequestLine(request), exception, status, exception);
        return ResponseEntity.status(status).build();
    }

    private String toRequestLine(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }

}
