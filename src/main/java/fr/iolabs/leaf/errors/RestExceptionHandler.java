package fr.iolabs.leaf.errors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    public static class ApiError {
        private int status;
        private String message;

        public ApiError(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    @ExceptionHandler(WebException.class)
    protected ResponseEntity<ApiError> handleEntityNotFound(WebException ex) {
        ApiError apiError = new ApiError(ex.getCode(), ex.getName());
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

}