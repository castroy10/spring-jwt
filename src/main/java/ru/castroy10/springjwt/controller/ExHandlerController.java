package ru.castroy10.springjwt.controller;

import com.fasterxml.jackson.core.JsonParseException;
import io.jsonwebtoken.security.SignatureException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.castroy10.springjwt.model.dto.ResponseDto;

@RestControllerAdvice
public class ExHandlerController {

    /**
     * Handles BadCredentialsException.
     *
     * @return a ResponseEntity with status 401 (Unauthorized) and an error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<@NonNull ResponseDto> badCredentials() {
        return ResponseEntity.status(401)
                             .body(new ResponseDto("Invalid username or password"));
    }

    /**
     * Handles IllegalArgumentException.
     *
     * @param e the exception
     * @return a ResponseEntity with status 400 (Bad Request) and the exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<@NonNull ResponseDto> badRequest(final Exception e) {
        return ResponseEntity.badRequest()
                             .body(new ResponseDto(e.getMessage()));
    }

    /**
     * Handles SignatureException (JWT signature validation failure).
     *
     * @param e the exception
     * @return a ResponseEntity with status 400 (Bad Request) and the exception message
     */
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<@NonNull ResponseDto> signatureException(final Exception e) {
        return ResponseEntity.badRequest()
                             .body(new ResponseDto(e.getMessage()));
    }

    /**
     * Handles JsonParseException (malformed JSON request).
     *
     * @param e the exception
     * @return a ResponseEntity with status 400 (Bad Request) and the exception message
     */
    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<@NonNull ResponseDto> JsonParseException(final Exception e) {
        return ResponseEntity.badRequest()
                             .body(new ResponseDto(e.getMessage()));
    }

    /**
     * Handles generic Exceptions.
     *
     * @param e the exception
     * @return a ResponseEntity with status 500 (Internal Server Error) and the exception message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull ResponseDto> exception(final Exception e) {
        return ResponseEntity.status(500)
                             .body(new ResponseDto(e.getMessage()));
    }

}
