package sprest.exception;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpStatusCodeException;

import jakarta.validation.ConstraintViolationException;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidReferenceException.class)
    @ResponseBody
    public Map<String, String> handleInvalidReference(InvalidReferenceException e) {
        log.error("Invalid entity reference error: {}", e.getMessage());
        return Map.of("message", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserInputValidationException.class)
    @ResponseBody
    public Map<String, String> handleInvalidUserInput(UserInputValidationException e) {
        log.trace("User input validation failed: {}", e.getMessage());
        return Map.of("message", e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataInconsistencyException.class)
    @ResponseBody
    public Map<String, String> handleDataInconsistencyException(DataInconsistencyException e) {
        log.error("Data inconsistency.", e);
        return Map.of("message", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnsupportedEncodingException.class)
    @ResponseBody
    public Map<String, String> handleInvalidUserInput(UnsupportedEncodingException e) {
        log.error("Unsupported encoding: {}", e.getMessage());
        return Map.of("message", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Map<String, String> handleConstraintViolation(ConstraintViolationException e) {
        log.error(": {}", e.getMessage());
        StringBuilder stringBuilder = new StringBuilder();
        var violations = e.getConstraintViolations();
        for (var constraintViolation : violations) {
            stringBuilder
                .append(constraintViolation.getPropertyPath())
                .append(" ")
                .append(constraintViolation.getMessage())
                .append(", ");
        }
        String missingFieldsString = stringBuilder.substring(0, stringBuilder.length() - 2);

        return Map.of("message", "Request validation failed: " + missingFieldsString);
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleHttpStatusCodeException(HttpStatusCodeException e) throws JsonProcessingException {
        var body = new ObjectMapper().readValue(e.getResponseBodyAsString(), Map.class);
        var originalMessage = (String) body.get("message");
        body.put("message", "External API error: " + originalMessage);

        return new ResponseEntity<>(body, e.getStatusCode());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SqlConstraintViolationException.class)
    @ResponseBody
    public Map<String, String> handleSqlConstraintViolationException(SqlConstraintViolationException e) {
        log.error("Data integrity violation error: {}", e.getMessage());
        return Map.of("message", e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundByUniqueKeyException.class)
    @ResponseBody
    public Map<String, String> handleNotFoundByUniqueKeyException(NotFoundByUniqueKeyException e) {
        log.trace("Item not found: {}", e.getMessage());
        return Map.of("message", e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Map<String, String> handleAccessDeniedException(AccessDeniedException e) {
        log.trace("Access denied: {}", e.getMessage());
        return Map.of("message", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
