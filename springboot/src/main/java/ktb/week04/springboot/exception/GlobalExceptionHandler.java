package ktb.week04.springboot.exception;

import ktb.week04.springboot.dto.error.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPassword(
            InvalidPasswordException exception
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        "INVALID_PASSWORD",
                        exception.getReason()
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception
    ) {
        HttpStatus status = HttpStatus.resolve(
                exception.getStatusCode().value()
        );
        String code = status == null
                ? "HTTP_" + exception.getStatusCode().value()
                : status.name();
        String message = exception.getReason() == null
                ? "요청을 처리하지 못했습니다."
                : exception.getReason();

        return ResponseEntity.status(exception.getStatusCode())
                .body(new ApiErrorResponse(code, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청값을 확인해주세요.");

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("VALIDATION_ERROR", message));
    }
}
