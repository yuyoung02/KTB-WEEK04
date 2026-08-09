package ktb.week04.springboot.dto.error;

import lombok.Getter;

@Getter
public class ApiErrorResponse {

    private final String code;
    private final String message;

    public ApiErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
