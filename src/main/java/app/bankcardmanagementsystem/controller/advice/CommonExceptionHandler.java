package app.bankcardmanagementsystem.controller.advice;


import app.bankcardmanagementsystem.controller.advice.annotation.CustomExceptionHandler;
import app.bankcardmanagementsystem.exception.CreateException;
import app.bankcardmanagementsystem.exception.DeleteException;
import app.bankcardmanagementsystem.exception.NotFoundException;
import app.bankcardmanagementsystem.exception.UpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(annotations = CustomExceptionHandler.class)
public class CommonExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleException(Exception e) {
        return new Response(e.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new Response(errors.toString(), Instant.now().toString());
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response handleAuthenticationServiceException(AuthenticationServiceException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response handleAccessDeniedException(SecurityException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(CreateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleCreateException(CreateException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(UpdateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response handlerUpdateException(UpdateException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handlerNotFoundException(NotFoundException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(DeleteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response handlerDeleteException(DeleteException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

}
